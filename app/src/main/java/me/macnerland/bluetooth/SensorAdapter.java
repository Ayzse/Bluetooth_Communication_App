package me.macnerland.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.TextView;

import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

/**
 * controls and stores data for the sensors
 * Created by Doug on 7/31/2016.
 */
public class SensorAdapter implements ExpandableListAdapter {
    private static final String TAG = "Sensor";

    private Vector<DataSetObserver> DSO;

    private Vector<SensorData> data;

    private Hashtable<String, Boolean> sensorConnect;
    private Hashtable<String, Integer> sensorIndex;

    private Vector<SensorData> sensors;
    private Vector<SensorData> expandedSensors;
    private Context context;

    private int dataState;
    private static final int NO_DATA_PENDING = 0;
    private static final int TEMPERATURE_DATA_PENDING = 1;
    private static final int HUMIDITY_DATA_PENDING = 2;

    private static final UUID sensorServiceGattUUID = UUID.fromString("0000feed-0000-1000-8000-00805f9b34fb");
    private static final UUID sensorCharacteristicUUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    private static final byte[] TempCommand = {(byte)'1', (byte)'\n', (byte)'\0'};
    private static final byte[] HumidCommand = {(byte)'2', (byte)'\n', (byte)'\0'};

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
            sensorConnect.put(address, false);
            sensors.add(new SensorData(bg, c));
            //enter the address into the indexor
            sensorIndex.put(address, sensors.size() - 1);
        }else{
            sensorConnect.put(address, true);
        }
        notifyDSO();
    }

    public void updateNotification(String address){
        BluetoothGatt bg = sensors.get(sensorIndex.get(address)).getGATT();
        BluetoothGattService bgs = bg.getService(sensorServiceGattUUID);
        BluetoothGattCharacteristic bgc = bgs.getCharacteristic(sensorCharacteristicUUID);
        int properties = bgc.getProperties();
        if ((properties | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            bg.setCharacteristicNotification(bgc, true);
        }
    }

    //write command to fetch the temps from all connected sensors
    public void updateTemperature(){
        if(dataState == NO_DATA_PENDING) {
            Log.i(TAG, "Getting data");
            Set<String> keys = sensorConnect.keySet();

            for (String con : keys) {
                if (sensorConnect.get(con)) {
                    BluetoothGatt gatt = sensors.get(sensorIndex.get(con)).getGATT();
                    BluetoothGattCharacteristic bgc =
                            gatt.getService(sensorServiceGattUUID).getCharacteristic(sensorCharacteristicUUID);
                    bgc.setValue(TempCommand);
                    gatt.writeCharacteristic(bgc);
                }
            }


            for (String con : keys) {
                if (sensorConnect.get(con)) {
                    BluetoothGatt gatt = sensors.get(sensorIndex.get(con)).getGATT();
                    BluetoothGattCharacteristic bgc =
                            gatt.getService(sensorServiceGattUUID).getCharacteristic(sensorCharacteristicUUID);
                    gatt.readCharacteristic(bgc);
                }
            }
        }
        dataState = TEMPERATURE_DATA_PENDING;
    }

    public void updateHumidity(){
        if(dataState == NO_DATA_PENDING) {
            Log.i(TAG, "Getting data");
            Set<String> keys = sensorConnect.keySet();
            Log.i(TAG, "Command: " + new String(HumidCommand));
            for (String con : keys) {
                if (sensorConnect.get(con)) {
                    Log.i(TAG, "asking humidity from connected sensor");
                    BluetoothGatt gatt = sensors.get(sensorIndex.get(con)).getGATT();
                    BluetoothGattService bs = gatt.getService(sensorServiceGattUUID);
                    if (bs == null) {
                        //services have not ben discovered
                        break;
                    }
                    BluetoothGattCharacteristic bgc =
                            bs.getCharacteristic(sensorCharacteristicUUID);
                    if (bgc == null) {
                        Log.i(TAG, "null characteristic");
                    } else {
                        bgc.setValue(HumidCommand);
                        gatt.writeCharacteristic(bgc);
                    }
                }
            }
            for (String con : keys) {
                if (sensorConnect.get(con)) {
                    Log.i(TAG, "retrieving from connected sensor");
                    BluetoothGatt gatt = sensors.get(sensorIndex.get(con)).getGATT();
                    BluetoothGattService bs = gatt.getService(sensorServiceGattUUID);
                    if (bs == null) break;
                    BluetoothGattCharacteristic bgc =
                            bs.getCharacteristic(sensorCharacteristicUUID);
                    gatt.readCharacteristic(bgc);
                }
            }
            dataState = HUMIDITY_DATA_PENDING;
        }
    }

    public void deliverTemp(String address, String value){
        SensorData sensor = sensors.get(sensorIndex.get(address));
        sensor.updateTemp(value);
    }

    public void deliverHumid(String address, String value){
        SensorData sensor = sensors.get(sensorIndex.get(address));
        sensor.updateHumid(value);
    }

    public void deliverData(String address, String value){
        SensorData sensor = sensors.get(sensorIndex.get(address));
        switch(dataState){
            case TEMPERATURE_DATA_PENDING:
                sensor.updateTemp(value);
                break;
            case HUMIDITY_DATA_PENDING:
                sensor.updateHumid(value);
                break;
            default:
                Log.wtf(TAG, "Bad date state");
        }
        dataState = NO_DATA_PENDING;
    }

    public void connectSensor(String address){
        if(sensorConnect.keySet().contains(address)){
            sensorConnect.put(address, true);
        }
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

        SensorData sensor = sensors.get(groupPosition);

        TextView groupTitle = (TextView)v.findViewById(R.id.sensor_group_text);
        groupTitle.setText(sensor.getGATT().getDevice().getName());




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
