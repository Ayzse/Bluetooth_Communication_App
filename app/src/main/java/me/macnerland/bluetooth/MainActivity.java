package me.macnerland.bluetooth;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListAdapter;

import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "Main";
    private final static int REQUEST_ENABLE_BT = 1;
    private static mPagerAdapter adapter;
    private static Context context;
    private ActionBar actionBar;

    //Identify a sensor from its MAC address
    Hashtable<String, SensorData> sensors;

    private static UUID hubGattUUID =      new UUID(0x0000ece000001000L, 0x800000805f9b34fbL);
    private static UUID[] hubUUID = {hubGattUUID};
    private static final UUID sensorGattUUID =   new UUID(0x0000feed00001000L, 0x800000805f9b34fbL);
    private static UUID[] sensorUUID = {sensorGattUUID};
    private static final UUID sensorCharacterGattUUID =   new UUID(0x0000ffe100001000L, 0x800000805f9b34fbL);
    private static UUID commonSerial =     new UUID(0x0000110100001000L, 0x800000805f9b34fbL);

    private static SensorAdapter sensorAdapter;

    private static BluetoothManager bluetoothManager;
    private static BluetoothAdapter bluetoothAdapter;

    private static BluetoothService bluetooth;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_pager);
        sensors = new Hashtable<>();
        context = this;
        bluetooth = null;
        sensorAdapter = new SensorAdapter(context);

        adapter = new mPagerAdapter(this.getSupportFragmentManager(), getResources(), context);
        ViewPager vp = (ViewPager) findViewById(R.id.pager);
        vp.setAdapter(adapter);


        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        ServiceConnection con = new conn();
        Intent btIntent = new Intent(this, BluetoothService.class);
        startService(btIntent);
        Log.i(TAG, "Binding");
        bindService(btIntent, con, BIND_AUTO_CREATE);

        registerReceiver(mGattUpdateReceiver, getGATTFilters());
    }

    public static SensorAdapter getSensorAdapter(){
        return sensorAdapter;
    }

    public static void scanForHub(View Null){
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity)context).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        bluetooth.hubScan();
    }

    public void scanForSensor(View Null){
        //if bluetooth is disabled, launch an intent to enable it.
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity)this).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        bluetoothAdapter.startLeScan(sensorUUID, sensorScanCallback);
    }

    public BluetoothAdapter.LeScanCallback sensorScanCallback = new BluetoothAdapter.LeScanCallback(){
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord){
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "Connecting to GATT server");
            BluetoothGatt sensorGatt = device.connectGatt(context, true, bluetooth.sensorGattCallback);
            sensorAdapter.addSensor(sensorGatt, context);
        }
    };

    public class conn implements ServiceConnection{
        @Override
        public void onServiceConnected(ComponentName name, IBinder service){
            Log.i(TAG, "Connected!");
            boolean firstConnect = bluetooth == null;
            bluetooth = ((BluetoothService.LocalBinder) service).getService();
            if(firstConnect){
                scanForHub(null);
                scanForSensor(null);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bluetooth = null;
        }
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothService.ACTION_GATT_CONNECTED.equals(action)) {
                String address = intent.getStringExtra(BluetoothService.EXTRA_DATA);
                sensorAdapter.connectSensor(address);
            } else if (BluetoothService.ACTION_GATT_DISCONNECTED.equals(action)) {
                String address = intent.getStringExtra(BluetoothService.EXTRA_DATA);
                sensorAdapter.disconnectSensor(address);
            } else if (BluetoothService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                String address = intent.getStringExtra(BluetoothService.EXTRA_DATA);
                sensorAdapter.connectSensor(address);
            } else if (BluetoothService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.e(TAG, "New DATA" + intent.getAction());
            }
        }
    };

    private IntentFilter getGATTFilters(){
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}