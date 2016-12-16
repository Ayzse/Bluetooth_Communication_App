package me.macnerland.bluetooth;

import android.content.res.Resources;
import android.database.DataSetObserver;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.Vector;

/**
 * Pages populates the top level viewpager
 * Created by Doug on 7/21/2016.
 */
class mPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = "PagerAdapter";
    private Fragment[] fragments;
    private String[] titles;

    private Vector<DataSetObserver> DSO;


    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return 2;
    }

    mPagerAdapter(FragmentManager fm, Resources r){
        super(fm);
        DSO = new Vector<>();

        fragments = new Fragment[2];

        titles = r.getStringArray(R.array.page_titles);
        fragments[0] = new HubFragment();


        SensorFragment sensorFragment = new SensorFragment();
        fragments[1] = sensorFragment;

        Log.v(TAG, "New Pager Adapter");
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        super.unregisterDataSetObserver(observer);
        if(DSO.contains(observer)) DSO.remove(observer);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
        DSO.add(observer);

    }

    @Override
    public CharSequence getPageTitle(int position){
        return titles[position];
    }
}
