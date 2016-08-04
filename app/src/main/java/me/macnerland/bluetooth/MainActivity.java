package me.macnerland.bluetooth;

import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "Main";
    private final static int REQUEST_ENABLE_BT = 1;
    private mPagerAdapter adapter;
    private Context context;
    private ActionBar actionBar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_pager);
        context = this;

        adapter = new mPagerAdapter(this.getSupportFragmentManager(), getResources(), context);
        ViewPager vp = (ViewPager) findViewById(R.id.pager);
        vp.setAdapter(adapter);


        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        //if bluetooth is disabled, launch an intent to enable it.
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        BluetoothLeScanner BLEscanner = bluetoothAdapter.getBluetoothLeScanner();


        UUID hubGattUUID =      new UUID(0x0000ece000001000L, 0x800000805f9b34fbL);
        UUID sensorGattUUID =   new UUID(0x0000feed00001000L, 0x800000805f9b34fbL);
        UUID[] gattServices = new UUID[2];
        gattServices[1] = sensorGattUUID;
        gattServices[0] = sensorGattUUID;
        Log.i(TAG, gattServices[0].toString());
        if(BLEscanner != null) {
            bluetoothAdapter.startLeScan(gattServices, new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    Log.i(TAG, device.toString());
                    StringBuilder str = new StringBuilder();
                    for(byte b : scanRecord) {
                        str.append((Byte) b);
                        str.append(' ');
                    }
                    Log.i(TAG, str.toString());
                    ParcelUuid[] p = device.getUuids();
                    Log.i(TAG, device.getAddress());
                    if(p != null) {
                        Log.i(TAG, device.getUuids().toString());
                    }

                    //adapter.addHub(device, context);
                    adapter.addSensor(device, context);
                }


            });
        }
    }
}