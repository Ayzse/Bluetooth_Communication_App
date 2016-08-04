package me.macnerland.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.widget.SimpleAdapter;

import java.util.UUID;

/**
 * Pages populates the top level viewpager
 * Created by Doug on 7/21/2016.
 */
public class mPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = "PagerAdapter";
    private Resources res;
    private Fragment[] fragments;
    private String[] titles;
    private HubFragment hubFragment;
    private SensorFragment sensorFragment;
    private final static int REQUEST_ENABLE_BT = 1;

    private Context context;
    private Activity activity;

    private SensorAdapter sensorAdapter;

    UUID hubGattUUID =      new UUID(0x0000ece000001000L, 0x800000805f9b34fbL);
    UUID sensorGattUUID =   new UUID(0x0000feed00001000L, 0x800000805f9b34fbL);
    UUID[] hubUUID = {hubGattUUID};
    UUID[] sensorUUID = {sensorGattUUID};

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return 2;
    }

    mPagerAdapter(FragmentManager fm, Resources r, Context c){
        super(fm);
        context = c;
        sensorAdapter = new SensorAdapter(context);
        res = r;
        activity = (Activity) context;


        //BluetoothLeScanner BLEscanner = bluetoothAdapter.getBluetoothLeScanner();
        Bundle args = new Bundle();

        fragments = new Fragment[2];
        titles = res.getStringArray(R.array.page_titles);

        fragments[0] = new HubFragment();

        SensorFragment sensorFragment = new SensorFragment();
        //sensorFragment.setListAdapter(sensorAdapter);
        fragments[1] = (Fragment)sensorFragment;


    }

    public void scanForHub(){
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        //if bluetooth is disabled, launch an intent to enable it.
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        bluetoothAdapter.startLeScan(hubUUID, hubScanCallback);
    }

    public void scanForSensor(){
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        //if bluetooth is disabled, launch an intent to enable it.
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        bluetoothAdapter.startLeScan(sensorUUID, sensorScanCallback);
    }

    public void addSensor(BluetoothDevice bd, Context c){
        sensorAdapter.addSensor(bd, c);
        sensorAdapter.notifyDSO();
    }

    public void addHub(){

    }

    public void sensorScan(){

    }

    public BluetoothAdapter.LeScanCallback hubScanCallback = new BluetoothAdapter.LeScanCallback(){
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord){
            Log.i(TAG, device.toString());
            StringBuilder str = new StringBuilder();
            for (byte b : scanRecord) {
                str.append((Byte) b);
                str.append(' ');
            }
            Log.i(TAG, str.toString());
            ParcelUuid[] p = device.getUuids();
            Log.i(TAG, device.getAddress());
            if (p != null) {
                Log.i(TAG, device.getUuids().toString());
            }
        }
    };

    public BluetoothAdapter.LeScanCallback sensorScanCallback = new BluetoothAdapter.LeScanCallback(){
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord){

        }

    };

    @Override
    public CharSequence getPageTitle(int position){
        return titles[position];
    }
}
