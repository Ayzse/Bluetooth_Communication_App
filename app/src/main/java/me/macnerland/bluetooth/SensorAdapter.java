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

    //private Hashtable<String, Boolean> sensorConnect;

    /*
    * The data structure requirements are as follows:
    *   1. given a String (sensor address), the structure must be able to return
    *   the associated SensorData object
    *
    *   2. The data structure must retain an order, so that it an be iterated through
    * */
    private Hashtable<String, Integer> sensorIndex;
    private Vector<SensorData> sensors;
    private Vector<String> connectedAddresses;
    private Context context;


    private static final UUID sensorServiceGattUUID = UUID.fromString("0000feed-0000-1000-8000-00805f9b34fb");
    private static final UUID sensorCharacteristicUUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    //these bytes are used to command the sensor to return values
    private static final byte[] TempCommand = {(byte)'1', (byte)'\n', (byte)'\0'};
    private static final byte[] HumidCommand = {(byte)'2', (byte)'\n', (byte)'\0'};

    SensorAdapter(Context c){
        context = c;
        sensors = new Vector<>();
        DSO = new Vector<>();
        connectedAddresses = new Vector<>();
        sensorIndex = new Hashtable<>();
    }

    public void addSensor(BluetoothGatt bg, Context c){
        String address = bg.getDevice().getAddress();

        //If this address has never been seen before, add it into the list.
        if(!sensorIndex.keySet().contains(address)) {
            //sensorIndex.put(address, false);
            SensorData data = new SensorData(bg, c);
            data.Disconnect();
            sensors.add(data);

            //enter the address into the indexor
            sensorIndex.put(address, sensors.size() - 1);
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
            for (SensorData con : sensors) {
                if (con.isConnected()) {
                    BluetoothGatt gatt = con.getGATT();
                    BluetoothGattCharacteristic bgc =
                            gatt.getService(sensorServiceGattUUID).getCharacteristic(sensorCharacteristicUUID);
                    bgc.setValue(TempCommand);
                    gatt.writeCharacteristic(bgc);
                }
            }
            for (SensorData con : sensors) {
                if (con.isConnected()) {
                    BluetoothGatt gatt = con.getGATT();
                    BluetoothGattCharacteristic bgc =
                            gatt.getService(sensorServiceGattUUID).getCharacteristic(sensorCharacteristicUUID);
                    gatt.readCharacteristic(bgc);
                    con.dataState = SensorData.TEMPERATURE_DATA_PENDING;
                }
            }

    }

    public void updateHumidity(){
            for (SensorData con : sensors) {
                if (con.isConnected()) {
                    BluetoothGatt gatt = con.getGATT();
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
                        con.dataState = con.HUMIDITY_DATA_PENDING;
                    }
                }
            }
            for (SensorData con : sensors) {
                if (con.isConnected()) {
                    BluetoothGatt gatt = con.getGATT();
                    BluetoothGattService bs = gatt.getService(sensorServiceGattUUID);
                    if (bs == null) break;
                    BluetoothGattCharacteristic bgc =
                            bs.getCharacteristic(sensorCharacteristicUUID);
                    gatt.readCharacteristic(bgc);
                    con.dataState = SensorData.HUMIDITY_DATA_PENDING;
                }
            }
    }

    public void deliverData(String address, String value){
        SensorData sensor = sensors.get(sensorIndex.get(address));
        switch(sensor.dataState){
            case SensorData.TEMPERATURE_DATA_PENDING:
                sensor.updateTemp(value);
                break;
            case SensorData.HUMIDITY_DATA_PENDING:
                sensor.updateHumid(value);
                break;
            default:
                Log.e(TAG, "Bad data state");
                break;
        }
        sensor.dataState = SensorData.NO_DATA_PENDING;
    }

    public void connectSensor(String address){
        if(sensorIndex.keySet().contains(address)){
            sensors.get(sensorIndex.get(address)).Connect();
        }
    }

    public void disconnectSensor(String address){
        if(sensorIndex.keySet().contains(address)){
            sensors.get(sensorIndex.get(address)).Disconnect();
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
        /*View v;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.sensor_group, parent, false);*/

        SensorData sensor = sensors.get(groupPosition);
        return sensor.getParentView(isExpanded, convertView, parent);
        /*
        TextView groupTitle = (TextView)v.findViewById(R.id.sensor_group_text);
        groupTitle.setText(sensor.getGATT().getDevice().getName());

        Float t = sensor.getTemp();
        Float rH = sensor.getHumid();

        TextView temp = (TextView)v.findViewById(R.id.temperature);
        TextView humid = (TextView)v.findViewById(R.id.humidity);

        temp.setText("temperature: " + t.toString() + " C");
        humid.setText("humidity: " + rH.toString()+ "%");

        return v;*/
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
