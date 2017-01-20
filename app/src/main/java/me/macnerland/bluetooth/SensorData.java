package me.macnerland.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Provides the data and interface for a sensor
 * This class represents a sensor and all of the data associated with it.
 * Created by Doug on 7/31/2016.
 */
class SensorData{
    private static final String TAG = "SensorData";
    private static final int numberChildren = 1;

    private final Context context;
    private BluetoothGatt bluetooth;
    private boolean connected;
    private Float currTemp;
    private Float currHumid;

    //data series for the graph view
    private PointsGraphSeries<DataPoint> temperatureSeries;
    private PointsGraphSeries<DataPoint> humiditySeries;

    //the data returned from the sensor does not indicate whether it is humidity or temperature,
    //so this class must keep track of the data it is waiting for.
    int dataState;
    static final int NO_DATA_PENDING = 0;
    static final int TEMPERATURE_DATA_PENDING = 1;
    static final int HUMIDITY_DATA_PENDING = 2;
    static final int HUMIDITY_THEN_TEMP = 3;


    private boolean write_enabled;
    private final String name;
    private final String address;

    //files representing the csv file outputs. These are initialized when the object is constructed
    private File out_temp;
    private File out_humid;

    //output stream that writes to the csv files.
    //Having two OutputStreams helps prevent race conditions
    private OutputStreamWriter osw_temp;
    private OutputStreamWriter osw_humid;

    //csv file headers that indicate the data columns
    private static final String header_temp = "time, temperature\n";
    private static final String header_humid = "time, humid\n";

    //create a disconnected sensor. This is called when restoring a sensor from csv
    SensorData(String address, String nme, Context c, boolean can_write){
        connected = false;
        context = c;

        currHumid = 0.0f;
        currTemp = 0.0f;

        temperatureSeries = new PointsGraphSeries<>();
        temperatureSeries.setColor(Color.BLUE);
        humiditySeries = new PointsGraphSeries<>();
        humiditySeries.setColor(Color.RED);

        name = nme;
        this.address = address;

        dataState = NO_DATA_PENDING;

        if(can_write) {
            enableWrite();
        }
        else{
            osw_humid = null;
            osw_temp = null;
        }
    }

    //create a sensor. This is called when seeing a new sensor for the first time
    SensorData(BluetoothGatt bg, Context c, boolean can_write){
        connected = false;
        bluetooth = bg;
        context = c;

        currHumid = 0.0f;
        currTemp = 0.0f;

        temperatureSeries = new PointsGraphSeries<>();
        temperatureSeries.setColor(Color.BLUE);
        humiditySeries = new PointsGraphSeries<>();
        humiditySeries.setColor(Color.RED);

        BluetoothDevice bd = bg.getDevice();

        name = bd.getName();
        address = bd.getAddress();

        if(can_write) {
            enableWrite();
        }
        else{
            osw_humid = null;
            osw_temp = null;
        }
    }

    //This is called when write access is suddenly granted, as in the case of Marshmallow and later
    boolean enableWrite(){

        //In the case that this has been previously called, return.
        if(write_enabled){
            return true;
        }

        File external_dir = context.getExternalFilesDir(null);
        if(external_dir == null){
            return false;
        }
        String filename_temp = external_dir.toString() + "/sensors/" + name + "_" + address + "_temp.csv";
        String filename_humid = external_dir.toString() + "/sensors/" + name + "_" + address + "_humid.csv";

        out_temp = new File(filename_temp);
        out_humid = new File(filename_humid);

        boolean temp_init = !out_temp.exists();
        boolean humid_init = !out_humid.exists();


        write_enabled = true;

        try{
            osw_temp = new OutputStreamWriter(new FileOutputStream(out_temp, true));
            if(temp_init){
                osw_temp.write(header_temp);
            }
        }catch (IOException io){
            Log.e(TAG, io.toString());
            osw_temp = null;
        }

        try{
            osw_humid = new OutputStreamWriter(new FileOutputStream(out_humid, true));
            if(humid_init){
                osw_humid.write(header_humid);
            }
        }catch (IOException io){
            Log.e(TAG, io.toString());
            osw_humid = null;
        }
        if(osw_humid == null || osw_temp == null){
            osw_humid = null;
            osw_temp = null;
            return false;
        }
        insertOldData(out_humid);
        insertOldData(out_temp);
        return true;
    }

    //insert all of the old data from the csv into the sensor
    //only called after enabling write on any of the sensors
    private void insertOldData(File f){
        if(!write_enabled){
            return;
        }
        String n = f.getName();
        int start = n.indexOf("_") + 1;
        start = n.indexOf("_", start) + 1;
        int end = n.indexOf(".", start);
        String type = n.substring(start, end);

        if(type.equals("temp")){
            try {
                osw_temp.close();
            }catch (IOException io){
                Log.e(TAG, io.toString());
            }

            //read in all of the temp data
            try {
                String row;
                BufferedReader br = new BufferedReader(new FileReader(out_temp));
                while((row = br.readLine()) != null){
                    //split the rows up.
                    String[] args = row.split(",");

                    //sometimes there is an extra row, just skip over it
                    if(args.length < 2){
                        continue;
                    }

                    String timestamp = args[0];
                    String temp = args[1];

                    if(timestamp.charAt(0) > 58 || temp.charAt(0) > 58){
                        continue;
                    }

                    long time = Long.parseLong(timestamp);
                    insertTemp(temp, time);
                }
            }catch (IOException io){
                Log.e(TAG, "temp 2:" + io.toString());
            }

            try {
                osw_temp = new OutputStreamWriter(new FileOutputStream(out_temp, true));
            }catch (IOException io){
                Log.e(TAG, io.toString());
            }
        }
        if(type.equals("humid")){
            try {
                osw_humid.close();
            }catch (IOException io){
                Log.e(TAG, io.toString());
            }

            //read in all of the humidity data
            try {
                String row;
                BufferedReader br = new BufferedReader(new FileReader(out_humid));
                while((row = br.readLine()) != null){
                    //split the rows up.
                    String[] args = row.split(",");
                    String timestamp = args[0];
                    String humid = args[1];

                    if(timestamp.charAt(0) > 58 || humid.charAt(0) > 58){
                        continue;
                    }

                    long time = Long.parseLong(timestamp);
                    insertHumid(humid, time);
                }
            }catch (IOException io){
                Log.e(TAG, "Humid 2:" + io.toString());
            }

            try {
                osw_humid = new OutputStreamWriter(new FileOutputStream(out_humid, true));
            }catch (IOException io){
                Log.e(TAG, io.toString());
            }
        }


    }

    //This is called when connecting to sensors that have been previously connected to. (Those that
    // already contain csv files.)
    void connectGatt(BluetoothGatt b){
        bluetooth = b;
    }

    boolean isConnected(){
        return connected;
    }

    void Connect(){
        connected = true;
    }

    void Disconnect(){
        connected = false;
    }

    //this does not write to the csv
    private void insertTemp(String T, long time){
        float temp = Float.parseFloat(T);
        temperatureSeries.appendData(new DataPoint(new Date(time), (double)temp), true, 10);
    }

    //temperature series must be thread-safe
    private void updateTemp(float T){
        if(connected){

        }
    }

    private void insertHumid(String rH, long time){
        float humid = Float.parseFloat(rH);
        humiditySeries.appendData(new DataPoint(new Date(time), (double)humid), true, 10);
    }

    private void updateHumid(float rH){
        currHumid = rH;
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        humiditySeries.appendData(
                new DataPoint(gregorianCalendar.getTime(), (double) currHumid),
                true, 10);
        if(osw_humid != null) {
            try {
                osw_humid.write("" + gregorianCalendar.getTimeInMillis() + "," + currHumid + "\n");
            } catch (IOException io) {
                Log.e(TAG, io.toString());
            }
        }
    }

    void receiveData(String value){

        float valueAsFloat = 0.0f;
        if(value.equals("nan")){
            Log.e(TAG, "sensor error: check connection between temp/hum detector and sensor board");
            return;
        }

        try {
            valueAsFloat = Float.parseFloat(value);
        }catch(NumberFormatException nfe){
            Log.e(TAG, "Bad value: " + value);
            return;
        }

        switch(dataState){
            case SensorData.TEMPERATURE_DATA_PENDING:
                currTemp = valueAsFloat;
                GregorianCalendar gregorianCalendar = new GregorianCalendar();
                temperatureSeries.appendData(
                        new DataPoint(gregorianCalendar.getTime(), (double) currTemp),
                        true, 10);
                if(osw_temp != null) {
                    try {
                        osw_temp.write("" + gregorianCalendar.getTimeInMillis() + "," + currTemp + "\n");
                    } catch (IOException io) {
                        Log.e(TAG, io.toString());
                    }
                }
                break;
            case SensorData.HUMIDITY_DATA_PENDING:
                updateHumid(valueAsFloat);
                break;
            case SensorData.HUMIDITY_THEN_TEMP:
                updateHumid(valueAsFloat);
                SensorAdapter get = MainActivity.getSensorAdapter();
                get.updateTemperature();
                break;
            default:
                Log.e(TAG, "Bad data state");
                break;
        }
        dataState = SensorData.NO_DATA_PENDING;
    }

    BluetoothGatt getGATT(){
        if(connected) {
            return bluetooth;
        }
        return null;
    }

    View getParentView(boolean isExpanded, View convertView, ViewGroup parent){
        View v;
        if(convertView != null){
            v = convertView;
        }else {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.sensor_group, parent, false);
        }

        TextView groupTitle = (TextView)v.findViewById(R.id.sensor_group_text);
        groupTitle.setText(name);

        if(!connected) {
            int color = ResourcesCompat.getColor(context.getResources(), R.color.disconnected_color, null);
            v.setBackgroundColor(color);
        }else{
            int color = ResourcesCompat.getColor(context.getResources(), R.color.connected_color, null);
            v.setBackgroundColor(color);

            TextView temp = (TextView)v.findViewById(R.id.temperature);
            TextView humid = (TextView)v.findViewById(R.id.humidity);

            temp.setText("Temperature: " + currTemp.toString() + " C");
            humid.setText("Humidity: " + currHumid.toString() + "%");
        }

        return v;
    }

    View getChildView(int child, View convertView, ViewGroup parent){

        View v;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.sensor_graph, parent, false);

        switch(child){
            case 0:
                temperatureSeries.getHighestValueX();
                humiditySeries.getHighestValueX();

                GraphView graph = (GraphView)v.findViewById(R.id.graph);
                graph.addSeries(temperatureSeries);
                graph.addSeries(humiditySeries);
                graph.getViewport().setXAxisBoundsManual(true);
                graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(context));
                graph.getGridLabelRenderer().setNumHorizontalLabels(4);
                return v;

        }
        return null;
    }

    int nChildren(){
        return numberChildren;
    }

    void onPause(){
        if(osw_humid !=null){
            try{
                osw_humid.close();
            }catch(IOException io){
                Log.e(TAG, io.toString());
            }
        }
        if(osw_temp !=null){
            try{
                osw_temp.close();
            }catch(IOException io){
                Log.e(TAG, io.toString());
            }
        }
    }

    void onResume(){
        if(write_enabled) {
            if (osw_humid != null) {
                try {
                    osw_humid = new OutputStreamWriter(new FileOutputStream(out_humid, true));
                } catch (IOException io) {
                    Log.e(TAG, io.toString());
                }
            }
            if (osw_temp != null) {
                try {
                    osw_temp = new OutputStreamWriter(new FileOutputStream(out_temp, true));
                } catch (IOException io) {
                    Log.e(TAG, io.toString());
                }
            }
        }
    }

}
