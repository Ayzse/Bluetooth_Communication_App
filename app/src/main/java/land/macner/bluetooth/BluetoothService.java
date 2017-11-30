package land.macner.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by Doug on 8/10/2016.
 *
 * This is a service that gets launched for each connected sensor and each connected hub.
 * This handles all of the incoming data from the bluetooth devices and broadcasts it in an
 * Intent to the MainActivity
 */
public class BluetoothService extends Service {
    private static final String TAG = "BluetoothService";
    public final static String EXTRA_DATA = "EXTRA";
    public final static String ADDRESS_DATA = "ADDRESS";
    public final static String DEVICE_DATA = "DEVICE";

    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    IRemoteServiceCallback mCallback;

    public final static String PERMISSION =
            "edu.uiuc.bluetooth.SERVICE_PERMISSION";
    public final static String SENSOR_ACTION_GATT_CONNECTED =
            "edu.uiuc.bluetooth.SENSOR_ACTION_GATT_CONNECTED";
    public final static String SENSOR_ACTION_GATT_DISCONNECTED =
            "edu.uiuc.bluetooth.SENSOR_ACTION_GATT_DISCONNECTED";
    public final static String SENSOR_ACTION_GATT_SERVICES_DISCOVERED =
            "edu.uiuc.bluetooth.SENSOR_ACTION_GATT_SERVICES_DISCOVERED";
    public final static String SENSOR_ACTION_DATA_AVAILABLE =
            "edu.uiuc.bluetooth.SENSOR_ACTION_DATA_AVAILABLE";

    public final static String HUB_ACTION_GATT_CONNECTED =
            "edu.uiuc.bluetooth.HUB_ACTION_GATT_CONNECTED";
    public final static String HUB_ACTION_GATT_DISCONNECTED =
            "edu.uiuc.bluetooth.HUB_ACTION_GATT_DISCONNECTED";
    public final static String HUB_ACTION_GATT_SERVICES_DISCOVERED =
            "edu.uiuc.bluetooth.HUB_ACTION_GATT_SERVICES_DISCOVERED";
    public final static String HUB_ACTION_DATA_AVAILABLE =
            "edu.uiuc.bluetooth.HUB_ACTION_DATA_AVAILABLE";

    private static BluetoothManager bluetoothManager;

    //A hash that relates MAC addresses to sensordata
    private HashMap<String, SensorData> sensors;

    class LocalBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }

        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch(code){
                case 2://scan for sensors
                    break;
                case 3://scan for hubs
                    break;

                default:
            }
            return true;// super.onTransact(code, data, reply, flags);
        }
    }

    private IBinder binder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "creating new service");
        bluetoothManager = (BluetoothManager)this.getSystemService(Service.BLUETOOTH_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    public void registerBluetoothCallback(IRemoteServiceCallback callback){
        mCallback = callback;
    }


    public final BluetoothGattCallback sensorGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
                intentAction = SENSOR_ACTION_GATT_CONNECTED;
                broadcastUpdate(intentAction, gatt.getDevice().getAddress(), gatt.getDevice());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = SENSOR_ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                broadcastUpdate(intentAction, gatt.getDevice().getAddress(), gatt.getDevice());
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(SENSOR_ACTION_GATT_SERVICES_DISCOVERED, gatt.getDevice().getAddress(), gatt.getDevice());
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(SENSOR_ACTION_DATA_AVAILABLE, gatt.getDevice().getAddress(), characteristic);
        }
    };

    public final BluetoothGattCallback hubGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
                intentAction = HUB_ACTION_GATT_CONNECTED;
                //intentAction = intentAction + " " + gatt.getDevice().getAddress();
                broadcastUpdate(intentAction, gatt.getDevice().getAddress(), gatt.getDevice());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = HUB_ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                broadcastUpdate(intentAction, gatt.getDevice().getAddress(), gatt.getDevice());
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(HUB_ACTION_GATT_SERVICES_DISCOVERED, gatt.getDevice().getAddress(), gatt.getDevice());
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(HUB_ACTION_DATA_AVAILABLE, gatt.getDevice().getAddress(), characteristic);
            }else{
                Log.e(TAG, "read was not a success");
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(HUB_ACTION_DATA_AVAILABLE, gatt.getDevice().getAddress(), characteristic);
        }
    };

    private void broadcastUpdate(final String action, final String address) {
        final Intent intent = new Intent(action);
        intent.putExtra(ADDRESS_DATA, address);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final String address, final BluetoothDevice device) {
        final Intent intent = new Intent(action);
        intent.putExtra(ADDRESS_DATA, address);
        intent.putExtra(DEVICE_DATA, device);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final String address,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml

        // For all other profiles, writes the data formatted in HEX.
        String returnData;
        byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            returnData = new String(data);
            intent.putExtra(EXTRA_DATA, returnData);
        }
        intent.putExtra(ADDRESS_DATA, address);

        sendBroadcast(intent);
    }
}
