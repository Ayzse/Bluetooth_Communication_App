package me.macnerland.bluetooth;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by Doug on 8/10/2016.
 */
public class BluetoothService extends Service {
    private static final String TAG = "BluetoothService";
    public final static String EXTRA_DATA = "EXTRA";
    public final static String ADDRESS_DATA = "ADDRESS";

    private static final UUID sensorServiceGattUUID = UUID.fromString("0000feed-0000-1000-8000-00805f9b34fb");
    private static final UUID sensorCharacteristicUUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    private static UUID[] sensorUUID = {sensorServiceGattUUID};

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

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

    private static final byte[] TempCommand = {(byte)'2', (byte)'\n'};
    private static final byte[] HumidCommand = {(byte)'1', (byte)'\n'};

    private static BluetoothManager bluetoothManager;
    private static BluetoothAdapter bluetoothAdapter;

    //A hash that relates MAC addresses to sensordata
    private HashMap<String, SensorData> sensors;

    public class LocalBinder extends Binder {
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
        bluetoothManager = (BluetoothManager)this.getSystemService(Service.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        return;
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

    public final BluetoothGattCallback sensorGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = SENSOR_ACTION_GATT_CONNECTED;
                intentAction = intentAction + " " + gatt.getDevice().getAddress();
                //broadcastUpdate(intentAction, gatt.getDevice().getAddress());
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        gatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = SENSOR_ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction, gatt.getDevice().getAddress());
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(SENSOR_ACTION_GATT_SERVICES_DISCOVERED, gatt.getDevice().getAddress());
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        /*@Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            //gatt.readCharacteristic(characteristic);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Successful read");
                broadcastUpdate(SENSOR_ACTION_DATA_AVAILABLE, gatt.getDevice().getAddress(), characteristic);
            }else{
                Log.e(TAG, "read was not a success");
            }
        }*/

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.e(TAG, "Writing characteristic status: " + status + " " + new String(characteristic.getValue()));
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.e(TAG, "New data, changed characteristic");
            broadcastUpdate(SENSOR_ACTION_DATA_AVAILABLE, gatt.getDevice().getAddress(), characteristic);
        }
    };

    public final BluetoothGattCallback hubGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = HUB_ACTION_GATT_CONNECTED;
                intentAction = intentAction + " " + gatt.getDevice().getAddress();
                //broadcastUpdate(intentAction, gatt.getDevice().getAddress());
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        gatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = HUB_ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction, gatt.getDevice().getAddress());
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(HUB_ACTION_GATT_SERVICES_DISCOVERED, gatt.getDevice().getAddress());
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            //gatt.readCharacteristic(characteristic);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Successful read");
                broadcastUpdate(HUB_ACTION_DATA_AVAILABLE, gatt.getDevice().getAddress(), characteristic);
            }else{
                Log.e(TAG, "read was not a success");
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.e(TAG, "Writing characteristic status: " + status + " " + new String(characteristic.getValue()));
            //gatt.readCharacteristic(characteristic);
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

    private void broadcastUpdate(final String action,
                                 final String address,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml

        // For all other profiles, writes the data formatted in HEX.
        byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            intent.putExtra(EXTRA_DATA, new String(data));// + "\n" + stringBuilder.toString());
            Log.w(TAG, "data in service:" + stringBuilder.toString());
        }
        intent.putExtra(ADDRESS_DATA, address);
        sendBroadcast(intent);
    }
}
