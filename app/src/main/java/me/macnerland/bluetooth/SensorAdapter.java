package me.macnerland.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;

import java.io.File;
import java.util.Hashtable;
import java.util.UUID;
import java.util.Vector;

/**
 * Stores SensorData instances in a vector. Is also responsible for delivering data to the
 * SensorData objects and restoring from csv files
 * Created by Doug on 7/31/2016.
 */
class SensorAdapter implements ExpandableListAdapter {
    private static final String TAG = "SensorAdapter";

    private final Vector<DataSetObserver> DSO;

    /*
    * The data structure requirements are as follows:
    *   1. given a String (sensor address), the structure must be able to return
    *   the associated SensorData object
    *
    *   2. The data structure must retain an order, so that it an be iterated through
    * */
    private final Hashtable<String, Integer> sensorIndex;
    private final Vector<SensorData> sensors;
    private final Context context;


    //private static final UUID sensorServiceGattUUID = UUID.fromString("0000ece1-0000-1000-8000-00805f9b34fb");
    private static final UUID sensorCharacteristicUUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    //these bytes are used to command the sensor to return values
    private static final byte[] TempCommand = {(byte)'1', (byte)'\n', (byte)'\0'};
    private static final byte[] HumidCommand = {(byte)'2', (byte)'\n', (byte)'\0'};

    private static boolean can_write;

    SensorAdapter(Context c){
        context = c;
        sensors = new Vector<>();
        DSO = new Vector<>();
        sensorIndex = new Hashtable<>();
    }

    //this should only be called once in this activity's lifecycle,
    //calling it extra times will add duplicate data to the sensors, appearance, but not the CSVs
    void enableWrite(){
        can_write = true;

        //discover old data in the app directory
        File sensorDir = context.getExternalFilesDir(null);
        if(sensorDir == null){
            Log.e(TAG, "failed to retrieve external directory path");
            return;
        }
        File parent = new File(sensorDir.toString() + "/sensors/");
        if(!parent.exists()) {
            boolean made_dir = parent.mkdir();
            if(!made_dir){
                Log.e(TAG, "Failed to make directory");
            }
        }

        File[] files = parent.listFiles();
        for(File f : files){
            String name = f.toString();
            Log.i(TAG, "Found file " + name);
            int name_start = name.indexOf("/sensors/") + "/sensors/".length();
            int name_end = name.indexOf("_", name_start);
            String nameless = name.substring(name_start, name_end);
            int address_start = name_end + 1;
            int address_end = name.indexOf("_", address_start);
            String address = name.substring(address_start, address_end);

            Log.i(TAG, "File name: " + nameless + " Found address: " + address);

            if(sensorIndex.containsKey(address)){
                Log.i(TAG, "There is old sensor data");
                SensorData data = sensors.get(sensorIndex.get(address));
                data.enableWrite();
            }else{
                Log.i(TAG, "making a new sensor");
                SensorData data = new SensorData(address, nameless, context, true);
                sensors.add(data);
                sensorIndex.put(address, sensors.size() - 1);
            }
        }

        //enable write on all current sensors with no current data in the filesystem
        for(SensorData s : sensors){
            s.enableWrite();
        }
        notifyDSO();
    }

    void addSensor(BluetoothGatt bg, Context c){
        String address = bg.getDevice().getAddress();
        Log.d(TAG, "adding sensor with address" + address);
        //If this address has never been seen before, add it into the list.
        if(!sensorIndex.keySet().contains(address)) {
            SensorData data = new SensorData(bg, c, can_write);
            sensors.add(data);
            //enter the address to get the index into the vector
            sensorIndex.put(address, sensors.size() - 1);
        }else{
            Log.e(TAG, "Trying to add in sensor that already has data");
            if(bg.getService(Constant.sensorServiceGattUUID) != null) {
                sensors.get(sensorIndex.get(address)).connectGatt(bg);
            }
        }
        notifyDSO();
    }

    void updateNotification(String address){
        BluetoothGatt bg = sensors.get(sensorIndex.get(address)).getGATT();
        if(bg != null) {
            BluetoothGattService bgs = bg.getService(Constant.sensorServiceGattUUID);
            if(bgs != null) {
                BluetoothGattCharacteristic bgc = bgs.getCharacteristic(sensorCharacteristicUUID);
                if (bgc != null){
                    int properties = bgc.getProperties();
                    if ((properties | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        bg.setCharacteristicNotification(bgc, true);
                    }
                }
            }
        }
    }

    //write command to fetch the temps from all connected sensors
    void updateTemperature(){
        //write command
            for (SensorData con : sensors) {
                if (con.isConnected() && con.dataState == SensorData.NO_DATA_PENDING) {
                    BluetoothGatt gatt = con.getGATT();
                    if(gatt == null){
                        break;
                    }
                    BluetoothGattService bs = gatt.getService(Constant.sensorServiceGattUUID);
                    if (bs == null) {
                        //services have not ben discovered
                        break;
                    }
                    BluetoothGattCharacteristic bgc =
                            bs.getCharacteristic(sensorCharacteristicUUID);
                    if(bgc == null) {
                        Log.i(TAG, "null characteristic");
                        break;
                    } else {
                        bgc.setValue(TempCommand);
                        gatt.writeCharacteristic(bgc);
                        con.dataState = SensorData.TEMPERATURE_DATA_PENDING;
                    }
                }
            }
            //read value

            for (SensorData con : sensors) {
                if (con.isConnected()) {
                    BluetoothGatt gatt = con.getGATT();
                    BluetoothGattService bs = gatt.getService(Constant.sensorServiceGattUUID);
                    if(bs == null) break;
                    BluetoothGattCharacteristic bgc = bs.getCharacteristic(sensorCharacteristicUUID);
                    if(bgc == null) break;
                    gatt.readCharacteristic(bgc);
                    if(con == null) break;
                    con.dataState = SensorData.TEMPERATURE_DATA_PENDING;
                }
            }
    }

    void updateTemperature(String sensor){
        int index = sensorIndex.get(sensor);
        SensorData sd = sensors.get(index);
        if(sd.dataState != SensorData.NO_DATA_PENDING){
            return;
        }

        BluetoothGatt gatt = sd.getGATT();
        if(gatt == null){
            Log.e(TAG, "Call to update disconnected sensor");
            return;
        }

        if(!gatt.connect()){
            return;
        }

        BluetoothGattService bs = gatt.getService(Constant.sensorServiceGattUUID);
        if(bs == null){
            Log.e(TAG, "Bad Service");
            return;
        }

        BluetoothGattCharacteristic bgc =
                bs.getCharacteristic(Constant.sensorCharacteristicUUID);
        if (bgc == null){
            Log.e(TAG, "Bad Characteristic");
            return;
        }

        bgc.setValue(TempCommand);
        gatt.writeCharacteristic(bgc);
        sd.dataState = SensorData.TEMPERATURE_DATA_PENDING;
    }

    void updateHumidity(String sensor){
        int index = sensorIndex.get(sensor);
        SensorData sd = sensors.get(index);
        if(sd.dataState != SensorData.NO_DATA_PENDING){
            return;
        }

        BluetoothGatt gatt = sd.getGATT();
        if(gatt == null){
            Log.e(TAG, "Call to update disconnected sensor");
            return;
        }

        if(!gatt.connect()){
            return;
        }
        BluetoothGattService bs = gatt.getService(Constant.sensorServiceGattUUID);
        if(bs == null){
            Log.e(TAG, "Bad Service");
            return;
        }

        BluetoothGattCharacteristic bgc =
                bs.getCharacteristic(Constant.sensorCharacteristicUUID);
        if (bgc == null){
            Log.e(TAG, "Bad Characteristic");
            return;
        }

        bgc.setValue(HumidCommand);
        gatt.writeCharacteristic(bgc);
        sd.dataState = SensorData.HUMIDITY_DATA_PENDING;
    }

    void updateHumidity(){
        for (SensorData con : sensors) {
            if (con.isConnected() && con.dataState == SensorData.NO_DATA_PENDING) {
                BluetoothGatt gatt = con.getGATT();
                if(gatt == null){
                    break;
                }
                BluetoothGattService bs = gatt.getService(Constant.sensorServiceGattUUID);
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
                    con.dataState = SensorData.HUMIDITY_DATA_PENDING;
                }
            }
        }
        for (SensorData con : sensors) {
            if (con.isConnected()) {
                BluetoothGatt gatt = con.getGATT();
                if(gatt == null){
                    break;
                }
                BluetoothGattService bs = gatt.getService(Constant.sensorServiceGattUUID);
                if (bs == null) break;
                BluetoothGattCharacteristic bgc =
                        bs.getCharacteristic(sensorCharacteristicUUID);
                gatt.readCharacteristic(bgc);
                con.dataState = SensorData.HUMIDITY_DATA_PENDING;
            }
        }
    }

    void updateBoth(){
        for (SensorData con : sensors) {
            if (con.isConnected() && con.dataState == SensorData.NO_DATA_PENDING) {
                BluetoothGatt gatt = con.getGATT();
                BluetoothGattService bs = gatt.getService(Constant.sensorServiceGattUUID);
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
                    con.dataState = SensorData.HUMIDITY_THEN_TEMP;
                }
            }
        }
        for (SensorData con : sensors) {
            if (con.isConnected()) {
                BluetoothGatt gatt = con.getGATT();
                if(gatt == null){
                    break;
                }
                BluetoothGattService bs = gatt.getService(Constant.sensorServiceGattUUID);
                if (bs == null) break;
                BluetoothGattCharacteristic bgc =
                        bs.getCharacteristic(sensorCharacteristicUUID);
                gatt.readCharacteristic(bgc);
                con.dataState = SensorData.HUMIDITY_THEN_TEMP;
            }
        }
    }

    void deliverData(String address, String value){
        SensorData sensor = sensors.get(sensorIndex.get(address));
        sensor.receiveData(value);
    }

    void connectSensor(String address){
        if(sensorIndex.keySet().contains(address)){
            sensors.get(sensorIndex.get(address)).Connect();
            notifyDSO();
        }else{
            Log.e(TAG, "Serious error: tried to connect non-existent sensor");
        }
    }

    boolean disconnectSensor(String address){
        if(sensorIndex.keySet().contains(address)){
            sensors.get(sensorIndex.get(address)).Disconnect();
            notifyDSO();
            return true;
        }else{
            Log.e(TAG, "Serious error: tried to disconnect a non-existent sensor");
            return false;
        }
    }

    void notifyDSO(){
        for(DataSetObserver dso: DSO){
            dso.onChanged();
        }
    }


    /*BEGIN android list View methods*/
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
        return sensors.get(groupPosition).nChildren();
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

        //let the sensor object handle it's own views
        SensorData sensor = sensors.get(groupPosition);
        return sensor.getParentView(isExpanded, convertView, parent);
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
        //sensors.get(groupPosition).expand();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        //sensors.get(groupPosition).collapse();
    }

    @Override
    public long getCombinedChildId(long groupId, long childId) {
        return 0;
    }

    @Override
    public long getCombinedGroupId(long groupId) {
        return 0;
    }

    //Android Life cycles
    void onPause(){
        for(SensorData sd : sensors){
            sd.onPause();
        }
    }

    void onResume(){
        for(SensorData sd : sensors){
            sd.onResume();
        }
    }

    public int describeContents(){
        return 0;
    }

    public void writeToParcel(Parcel out, int flags){

        for(SensorData sd : sensors){
            //parcel sensor data and write it into out
        }

    }

    public static final Parcelable.Creator<SensorAdapter> CREATOR
            = new Parcelable.Creator<SensorAdapter>(){
        public SensorAdapter createFromParcel(Parcel in){
            return null;
        }

        public SensorAdapter[] newArray(int size){
            return new SensorAdapter[size];
        }
    };

    private SensorAdapter(Parcel in){
        DSO = new Vector<>();
        sensorIndex = new Hashtable<>();
        sensors = new Vector<SensorData>();
        context = MainActivity.getContext();


    }

}
