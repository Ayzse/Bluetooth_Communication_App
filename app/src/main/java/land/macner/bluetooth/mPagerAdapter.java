package land.macner.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.List;
import java.util.Vector;

/**
 * Pages populates the top level viewpager
 * Created by Doug on 7/21/2016.
 */
class mPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = "PagerAdapter";

    private static final String dataDisplayFClass = "me.macnerland.bluetooth.DataDisplayFragment";
    private static final String sensorFClass = "me.macnerland.bluetooth.SensorFragment";
    private static final String hubFClass = "me.macnerland.bluetooth.HubFragment";

    private static final int sensor_rank = 0;
    private static final int hub_rank = 1;
    private static final int data_display_rank = 2;

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
        titles = r.getStringArray(R.array.page_titles);

        fragments = new Fragment[3];
        fragments[hub_rank] = null;
        fragments[sensor_rank] = null;
        fragments[data_display_rank] = null;

        // Try to restore previous fragments, if possible
        List<Fragment> lf = fm.getFragments();
        if(lf !=null) {
            for (Fragment f : lf) {
                if(f.getClass().getCanonicalName().equals(sensorFClass)){
                    fragments[sensor_rank] = f;
                }
                if(f.getClass().getCanonicalName().equals(hubFClass)){
                    fragments[hub_rank] = f;
                }
                if(f.getClass().getCanonicalName().equals(dataDisplayFClass)){
                    fragments[data_display_rank] = f;
                }
            }
        }

        // If no fragments can be recovered, make new ones.
        if(fragments[hub_rank] == null) {
            fragments[hub_rank] = new HubFragment();
        }
        if(fragments[sensor_rank] == null) {
            fragments[sensor_rank] = new SensorFragment();
        }
        if(fragments[data_display_rank] == null){
            //fragments[data_display_rank] = new DataDisplayFragment();
        }
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

    void addHub(BluetoothDevice device){
        HubFragment hf = (HubFragment)fragments[hub_rank];
        hf.addHub(device);
    }

    HubData getHub(String address){
        HubFragment hf = (HubFragment)fragments[hub_rank];
        return hf.getHub(address);
    }

    void initializeHub(String address){
        HubFragment hf = (HubFragment)fragments[hub_rank];
        hf.initialize(address);
    }

    boolean deliverHubData(String address, String data){
        HubFragment hf = (HubFragment)fragments[hub_rank];
        return hf.deliverData(address, data);
    }

    void notifyHubDSO(){
        HubFragment hf = (HubFragment)fragments[hub_rank];
        hf.notifyDSO();
    }

    void enableWrite(){
        SensorFragment sf = (SensorFragment)fragments[sensor_rank];
        sf.enableWrite();
    }

    void addSensor(BluetoothDevice device){
        SensorFragment sf = (SensorFragment)fragments[sensor_rank];
        sf.addSensor(device);
    }

    void connectSensor(String address, BluetoothDevice device){
        SensorFragment sf = (SensorFragment)fragments[sensor_rank];
        sf.connectSensor(address, device);
    }

    void updateSensorNotification(String address){
        SensorFragment sf = (SensorFragment)fragments[sensor_rank];
        sf.updateNotification(address);
    }

    void updateSensorTemperature(String address){
        SensorFragment sf = (SensorFragment)fragments[sensor_rank];
        sf.updateTemperature(address);
    }

    void updateSensorHumidity(String address){
        SensorFragment sf = (SensorFragment)fragments[sensor_rank];
        sf.updateHumidity(address);
    }

    void deliverSensorData(String address, String data){
        SensorFragment sf = (SensorFragment)fragments[sensor_rank];
        sf.deliverData(address, data);
    }

    void notifySensorDSO(){
        SensorFragment sf = (SensorFragment)fragments[sensor_rank];
        sf.notifyDSO();
    }

    void disconnectSensor(String address){
        SensorFragment sf = (SensorFragment)fragments[sensor_rank];
        sf.disconnect(address);
    }

    void clearGraph(String address){
        SensorFragment sf = (SensorFragment)fragments[sensor_rank];
        sf.clearGraph(address);
        sf.notifyDSO();
    }
}
