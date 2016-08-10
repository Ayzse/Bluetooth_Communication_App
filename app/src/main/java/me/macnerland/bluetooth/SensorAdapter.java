package me.macnerland.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.TextView;

import java.util.Hashtable;
import java.util.Vector;

/**
 * controls and stores data for the sensors
 * Created by Doug on 7/31/2016.
 */
public class SensorAdapter implements ExpandableListAdapter {

    private Vector<DataSetObserver> DSO;

    private Vector<SensorData> data;

    private Hashtable<String, Boolean> sensorConnect;
    private Hashtable<String, Integer> sensorIndex;

    private Vector<SensorData> sensors;
    private Vector<SensorData> expandedSensors;
    private Context context;

    SensorAdapter(Context c){
        context = c;
        sensors = new Vector<>();
        DSO = new Vector<>();
        sensorConnect = new Hashtable<>();
        sensorIndex = new Hashtable<>();
    }

    public void addSensor(BluetoothGatt bg, Context c){
        String address = bg.getDevice().getAddress();
        if(!sensorConnect.keySet().contains(address)) {
            sensorConnect.put(address, true);
            sensors.add(new SensorData(bg, c));
        }else{
            sensorConnect.put(address, true);
        }
        notifyDSO();
    }

    public void connectSensor(String address){

    }

    public void disconnectSensor(String address){
        if(sensorConnect.keySet().contains(address)){
            sensorConnect.put(address, false);
        }
    }

    public void notifyDSO(){
        for(DataSetObserver dso: DSO){
            dso.onChanged();
        }
    }


    /*BEGIN android methods*/
    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        DSO.add(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if(DSO.contains(observer)) DSO.remove(observer);
    }

    @Override
    public int getGroupCount() {
        return sensors.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return (sensors.get(groupPosition)).nChildren();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return sensors.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View v;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.sensor_group, parent, false);
        TextView groupTitle = (TextView)v.findViewById(R.id.sensor_group_text);
        groupTitle.setText(sensors.get(groupPosition).getGATT().getDevice().getName());


        return v;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        return sensors.get(groupPosition).getChildView(childPosition, convertView, parent);
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        sensors.get(groupPosition).expand();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        sensors.get(groupPosition).collapse();
    }

    @Override
    public long getCombinedChildId(long groupId, long childId) {
        return 0;
    }

    @Override
    public long getCombinedGroupId(long groupId) {
        return 0;
    }
}
