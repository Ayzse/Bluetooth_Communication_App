package me.macnerland.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
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
import android.widget.ExpandableListView;
import android.widget.SimpleAdapter;

import java.io.InputStream;
import java.io.OutputStream;
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

    private byte[] getHubID = {0x02, 0x20, 0x0a};



    UUID commonSerial =     new UUID(0x0000110100001000L, 0x800000805f9b34fbL);


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
        fragments[1] = (Fragment)sensorFragment;


    }

    public void refreshUI(){
        fragments[0] = new HubFragment();
    }

    public void addHub(BluetoothDevice device){
        try {
            BluetoothSocket bs = device.createInsecureRfcommSocketToServiceRecord(commonSerial);
            Log.d(TAG, "Socket retrieved");
            bs.connect();
            Log.d(TAG, "Connecting");
            while(!bs.isConnected());
            OutputStream os = bs.getOutputStream();
            byte[] out = {0x0a};
            byte[] buf = new byte[300];
            os.write(getHubID);
            Log.d(TAG, "sent byte buffer");
            InputStream is = bs.getInputStream();
            is.read(buf);

        }catch(java.io.IOException e){
            Log.d(TAG, "Failed to create connection with exception:\n" + e.toString());
        }
    }

    public void sensorScan(){

    }

    @Override
    public CharSequence getPageTitle(int position){
        return titles[position];
    }
}
