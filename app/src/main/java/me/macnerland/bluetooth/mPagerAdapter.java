package me.macnerland.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.widget.SimpleAdapter;

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

    private Context context;

    private SensorAdapter sensorAdapter;


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

        fragments = new Fragment[2];
        titles = res.getStringArray(R.array.page_titles);

        fragments[0] = new HubFragment();

        SensorFragment sensorFragment = new SensorFragment();
        //sensorFragment.setListAdapter(sensorAdapter);
        fragments[1] = (Fragment)sensorFragment;
    }

    public void addSensor(BluetoothDevice bd, Context c){
        sensorAdapter.addSensor(bd, c);
        sensorAdapter.notifyDSO();
    }

    public void addHub(){

    }

    @Override
    public CharSequence getPageTitle(int position){
        return titles[position];
    }
}
