package land.macner.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "Main";
    private final static int REQUEST_ENABLE_BT = 1;
    private static mPagerAdapter adapter;
    private Context context;


    private static final String[] marshmallow_permissions = {BluetoothService.PERMISSION, android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.BLUETOOTH};

    private static WebAdapter webAdapter;

    private static BluetoothAdapter bluetoothAdapter;

    //used for scanning for devices when API level >= 23
    private static BluetoothLeScanner bluetoothLeScanner;
    private static ScanSettings scanSettings;
    private static List<ScanFilter> HubScanFilter;
    private static List<ScanFilter> SensorScanFilter;
    private static ParcelUuid parcelHubService = new ParcelUuid(Constant.hubServiceGattUUID);
    private static ParcelUuid parcelSensorService = new ParcelUuid(Constant.sensorServiceGattUUID);

    private AppCompatImageView bluetoothStatus;

    //private static final String[] marshmallow_permissions = new String(){android.Manifest.permission.ACCESS_FINE_LOCATION};
    private static final String sensorAdapterSISkey = "SENSOR_ADAPT";
    private static final String hubAdapterSISkey = "HUB_ADAPT";
    private static final String pagerAdapterSISkey = "PAGER_ADAPT";
    private static final String bluetoothSISkey = "BLUETOOTH";

    private static Hashtable<View, String> view_to_address;

    //keys are addresses
    private HashMap<String, BluetoothService> callbacks;
    private Hashtable<String, BluetoothDevice> devices;
    private Hashtable<String, Boolean> device_type;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_pager);
        view_to_address = new Hashtable<>();
        devices = new Hashtable<>();
        device_type = new Hashtable<>();
        callbacks = new HashMap<>();
        bluetoothStatus = (AppCompatImageView)findViewById(R.id.bluetooth_status);

        registerReceiver(mGattUpdateReceiver, getGATTFilters());

        //This is critical to set here, future object instances may call getContext()
        context = this;

        adapter = new mPagerAdapter(this.getSupportFragmentManager(), getResources());

        ViewPager vp = (ViewPager) findViewById(R.id.pager);
        if(vp != null) {
            vp.setAdapter(adapter);
        }

        //start listening in on intents broadcast by the bluetooth adapter
        registerReceiver(bluetoothStateReceiver, getBluetoothStateFilter());

        //previous builds do not support bluetooth
        switch(Build.VERSION.SDK_INT){
            case 18:
            case 19:
            case 20:
                initBluetoothManAdpt();
                adapter.enableWrite();
                break;
            case 21:
            case 22:
                initBluetoothManAdpt();
                adapter.enableWrite();
                initLollipopBluetooth();
                break;

            case 23:
            default:
                //initBluetoothManAdpt is called after the initMarshmallowBluetooth();
                initMarshmallowBluetooth();
                adapter.enableWrite();
                break;
        }

    }

    static mPagerAdapter getAdapter(){
        return adapter;
    }

    static void register_view_to_address(View v, String a){
        view_to_address.put(v, a);
    }

    void initBluetoothManAdpt(){
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Intent for marshmallow bluetooth");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        if(bluetoothAdapter != null){
            int state = bluetoothAdapter.getState();
            switch(state){
                case BluetoothAdapter.STATE_ON:
                case BluetoothAdapter.STATE_TURNING_ON:
                    bluetoothStatus.setVisibility(View.GONE);
                    break;
                case BluetoothAdapter.STATE_OFF:
                case BluetoothAdapter.STATE_TURNING_OFF:
                    bluetoothStatus.setVisibility(View.VISIBLE);
                    break;
            }
        }

    }

    @TargetApi(21)
    public void initLollipopBluetooth(){
        scanSettings = new ScanSettings.Builder()
                .setReportDelay(0)
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build();
        ScanFilter hubsf = new ScanFilter.Builder()
                .setServiceUuid(parcelHubService).build();
        ScanFilter sensorsf = new ScanFilter.Builder()
                .setServiceUuid(parcelSensorService).build();

        HubScanFilter = new ArrayList<>();
        SensorScanFilter = new ArrayList<>();
        HubScanFilter.add(hubsf);
        SensorScanFilter.add(sensorsf);
    }

    @TargetApi(23)
    public void initMarshmallowBluetooth(){
        /*
        * TODO: needs some playtesting.
        * Right now, in order:
        * Return everything that matches the filters
        * Connect aggressively,
        * Connect after one receiving one advertisement match
        * set delay to 0 to connect as quickly as possible
        * Balanced scan duty cycle, settle for a lighter power consumption, with moderate speed
        * */
        scanSettings = new ScanSettings.Builder()
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
                .setReportDelay(0)
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build();
        ScanFilter hubsf = new ScanFilter.Builder()
                .setServiceUuid(parcelHubService).build();
        ScanFilter sensorsf = new ScanFilter.Builder()
                .setServiceUuid(parcelSensorService).build();
        HubScanFilter = new ArrayList<>();
        SensorScanFilter = new ArrayList<>();
        HubScanFilter.add(hubsf);
        SensorScanFilter.add(sensorsf);

        ActivityCompat.requestPermissions(this, marshmallow_permissions, 1);
    }

    //used in Marshmallow and above
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults){
        for(int i = 0; i < permissions.length; i++) {
            Log.i(TAG, "permission returned " + permissions[i] + " " + grantResults[i]);
            if(permissions[i].equals(android.Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                initBluetoothManAdpt();
            }
            else if(permissions[i].equals(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[i] == PackageManager.PERMISSION_GRANTED){
                adapter.enableWrite();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ENABLE_BT){
            if(resultCode == RESULT_OK){
                bluetoothStatus.setVisibility(View.GONE);
            }else {
                Log.w(TAG, "Bluetooth has not been enabled");
                bluetoothStatus.setVisibility(View.VISIBLE);
            }
        }
    }

    Context getContext(){
        return context;
    }

    public static WebAdapter getWebAdapter(){
        return webAdapter;
    }

    @TargetApi(23)
    void stopscan(){
        if(bluetoothLeScanner != null){
            bluetoothLeScanner.stopScan(new ScanCallback() {
                @Override
                @TargetApi(23)
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    Log.i(TAG, "Scan stopped");
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results){
                    super.onBatchScanResults(results);
                    Log.i(TAG, "Scan delivered");
                }
            });
        }
    }

    public void specific_temp(View button){
        View target = (View)button.getParent();
        String address = view_to_address.get(target);
        adapter.updateSensorTemperature(address);
    }

    public void specific_humid(View button){
        View target = (View)button.getParent();
        String address = view_to_address.get(target);
        adapter.updateSensorHumidity(address);
    }

    public void disconnect_sensor(View button){
        View target = (View)button.getParent();
        String address = view_to_address.get(target);
        adapter.disconnectSensor(address);
    }

    public void clear_graph(View button){
        View target = (View)button.getParent();
        String address = view_to_address.get(target);
        adapter.clearGraph(address);
    }

    public SensorAdapter getSensorAdapter(){
        return adapter.getSensorAdapter();
    }

    /****Hub scanning methods****/
    // called when scanning for hubs
    @SuppressWarnings("deprecation")
    public void scanForHub(View Null){
        switch(Build.VERSION.SDK_INT) {
            case 18:
            case 19:
            case 20:
                bluetoothAdapter.startLeScan(Constant.hubUUID, hubScanCallback);
                break;
            case 21:
            case 22:
                LollipopHubScan();
                break;
            case 23:
                LollipopHubScan();
                break;
            default:

                break;
        }
    }

    @TargetApi(21)
    void LollipopHubScan() {
        if (bluetoothLeScanner == null) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }
        bluetoothLeScanner.startScan(HubScanFilter, scanSettings, new ScanCallback() {
            @Override
            @TargetApi(23)
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice device = result.getDevice();
                //BluetoothGatt hubGatt = device.connectGatt(context, true, bluetooth.hubGattCallback);
                adapter.addHub(device);
                //hubAdapter.addHub(context, device);
            }
        });
    }

    //Called when a hub (with UUID) is detected, only used beneath marshmallow
    public BluetoothAdapter.LeScanCallback hubScanCallback = new BluetoothAdapter.LeScanCallback(){
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord){
            adapter.addHub(device);
            // hubAdapter.addHub(context, device);
        }
    };



    /***begin sensor scanning methods***/

    //the view argument allows the method to be called from view resources
    @SuppressWarnings("deprecation")
    public void scanForSensor(View Null){
        switch(Build.VERSION.SDK_INT) {
            case 18:
            case 19:
            case 20:
                bluetoothAdapter.startLeScan(Constant.sensorUUID, sensorScanCallback);
                break;
            case 21:
            case 22:
            case 23:
            default:
                LollipopSensorScan();
                break;
        }
    }

    @TargetApi(21)
    private void LollipopSensorScan(){
        if(bluetoothLeScanner == null){
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }
        bluetoothLeScanner.startScan(SensorScanFilter, scanSettings, new ScanCallback() {
            @Override
            @TargetApi(23)
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice device = result.getDevice();
                adapter.addSensor(device);
            }});
    }


    //Called when a nearby sensor (With the correct UUID) is detected.
    public BluetoothAdapter.LeScanCallback sensorScanCallback = new BluetoothAdapter.LeScanCallback(){
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord){
            adapter.addSensor(device);
        }
    };



    /**IPC functions to receive data from the services.**/
    // IPC functions
    // Handles various events fired by the Service.
    //
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    //This receiver handles all of the actions from the service.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final String address = intent.getStringExtra(BluetoothService.ADDRESS_DATA);
            if (BluetoothService.SENSOR_ACTION_GATT_CONNECTED.equals(action)) {
                final BluetoothDevice bd = intent.getParcelableExtra(BluetoothService.DEVICE_DATA);
                adapter.connectSensor(address, bd);
            } else if (BluetoothService.SENSOR_ACTION_GATT_DISCONNECTED.equals(action)) {
                final BluetoothDevice bd = intent.getParcelableExtra(BluetoothService.DEVICE_DATA);
                adapter.connectSensor(address, bd);
            } else if (BluetoothService.SENSOR_ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                final BluetoothDevice bd = intent.getParcelableExtra(BluetoothService.DEVICE_DATA);
                adapter.connectSensor(address, bd);
                adapter.updateSensorNotification(address);
            } else if (BluetoothService.SENSOR_ACTION_DATA_AVAILABLE.equals(action)) {
                String data = intent.getStringExtra(BluetoothService.EXTRA_DATA);
                if("Invalid Command\n".equals(data)){
                    Log.w(TAG, "Invalid Command");
                }else {
                    adapter.deliverSensorData(address, data);
                    adapter.notifySensorDSO();
                }
            } else if (BluetoothService.HUB_ACTION_GATT_SERVICES_DISCOVERED.equals(action)){
                //BluetoothGatt bg = hubAdapter.getHub(address).getGATT();
                BluetoothGatt bg = adapter.getHub(address).getGATT();
                BluetoothGattService bgs = bg.getService(Constant.hubServiceGattUUID);
                BluetoothGattCharacteristic bgc;
                if(bgs !=null) {
                    bgc = bgs.getCharacteristic(Constant.hubCharacteristicGattUUID);
                    if(bgc != null) {
                        int properties = bgc.getProperties();
                        if ((properties | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            bg.setCharacteristicNotification(bgc, true);
                        }
                        Log.i(TAG, "begin initialization");
                        adapter.initializeHub(address);
                    }else{//the characteristic is wrong
                        UUID id = bgs.getUuid();
                        // Toast.makeText(context, "Correct Hub service, bad Characteristic" + id.toString(), Toast.LENGTH_LONG).show();
                    }
                }else{// the service is wrong
                    Toast.makeText(context, "Bad Hub Service", Toast.LENGTH_LONG).show();
                }
            } else if (BluetoothService.HUB_ACTION_DATA_AVAILABLE.equals(action)){
                String value = intent.getStringExtra(BluetoothService.EXTRA_DATA);
                if(adapter.deliverHubData(address, value)){
                    adapter.notifyHubDSO();
                }
            }
        }
    };

    private BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            final String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                switch(state) {
                    case BluetoothAdapter.STATE_ON:
                        Log.i(TAG, "state change detected");
                        bluetoothStatus.setVisibility(View.GONE);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.i(TAG, "Turning off");
                        bluetoothStatus.setVisibility(View.VISIBLE);
                        break;
                }

            }
        }
    };

    private IntentFilter getBluetoothStateFilter(){
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return intentFilter;
    }

    //Used to filter out intents
    private IntentFilter getGATTFilters(){
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.SENSOR_ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothService.SENSOR_ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothService.SENSOR_ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothService.SENSOR_ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothService.HUB_ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothService.HUB_ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothService.HUB_ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothService.HUB_ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    //android activity lifecycle overrides
    /* pretty sure these are no longer needed because of fragment changes
    @Override
    public void onPause(){
        super.onPause();
        adapter.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();
        sensorAdapter.onResume();
    }

    @Override
    public void onDestroy(){
        //parcel currently held data
        super.onDestroy();
    }
    */
}