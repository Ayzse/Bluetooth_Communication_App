package me.macnerland.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.graphics.Color;
import android.os.Parcel;
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
 * Provides the backing data on a connected sensor
 * This class represents a sesnor and all of the data associated with it.
 * Created by Doug on 7/31/2016.
 */
public class SensorData{
    private static final String TAG = "SensorData";

    Context context;
    private BluetoothGatt bluetooth;
    private boolean connected;
    private boolean expanded;
    Float currTemp;
    Float currHumid;

    GraphView temperatureGraph;
    GraphView humidityGraph;

    PointsGraphSeries<DataPoint> temperatureSeries;
    PointsGraphSeries<DataPoint> humiditySeries;

    public int dataState;
    public static final int NO_DATA_PENDING = 0;
    public static final int TEMPERATURE_DATA_PENDING = 1;
    public static final int HUMIDITY_DATA_PENDING = 2;

    private boolean write_enabled;
    private String name;
    private String address;

    OutputStreamWriter osw_temp;
    OutputStreamWriter osw_humid;

    private static final String header_temp = "time, temperature\n";
    private static final String header_humid = "time, humid\n";

    private File out_temp;
    private File out_humid;

    SensorData(Parcel in){

    }

    //create a disconnected sensor
    SensorData(String addr, String nme, Context c, boolean can_write){
        connected = false;
        context = c;
        expanded = false;

        currHumid = 0.0f;
        currTemp = 0.0f;

        temperatureGraph = new GraphView(context);
        temperatureGraph.setTitle("Temperature");

        temperatureSeries = new PointsGraphSeries<>();
        temperatureSeries.setColor(Color.BLUE);
        humiditySeries = new PointsGraphSeries<>();
        humiditySeries.setColor(Color.RED);

        name = nme;
        address = addr;

        if(can_write) {
            if(can_write) {
                Log.i(TAG, "Creating sensor with write enabled");
                enableWrite();
            }
        }
        else{
            osw_humid = null;
            osw_temp = null;
        }
    }

    SensorData(BluetoothGatt bg, Context c, boolean can_write){
        connected = false;
        bluetooth = bg;
        context = c;
        expanded = false;

        currHumid = 0.0f;
        currTemp = 0.0f;

        temperatureGraph = new GraphView(context);
        temperatureGraph.setTitle("Temperature");

        temperatureSeries = new PointsGraphSeries<>();
        temperatureSeries.setColor(Color.BLUE);
        humiditySeries = new PointsGraphSeries<>();
        humiditySeries.setColor(Color.RED);

        BluetoothDevice bd = bg.getDevice();

        name = bd.getName();
        address = bd.getAddress();

        if(can_write) {
            Log.i(TAG, "Creating sensor with write enabled");
            enableWrite();
        }
        else{
            osw_humid = null;
            osw_temp = null;
        }
    }

    public boolean enableWrite(){
        if(write_enabled){
            return true;
        }
        String filename_temp = context.getExternalFilesDir(null).toString() + "/sensors/" + name + "_" + address + "_temp.csv";
        String filename_humid = context.getExternalFilesDir(null).toString() + "/sensors/" + name + "_" + address + "_humid.csv";
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
        Log.i(TAG, "Inserting old data");
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
        Log.e(TAG, "Type: " + type);

        if(type.equals("temp")){
            try {
                osw_temp.close();
            }catch (IOException io){

            }

            //read in all of the temp data
            try {
                String row = "";
                BufferedReader br = new BufferedReader(new FileReader(out_temp));
                while((row = br.readLine()) != null){
                    //split the rows up.
                    String[] args = row.split(",");
                    String timestamp = args[0];
                    String temp = args[1];

                    if(timestamp.charAt(0) > 58 || temp.charAt(0) > 58){
                        continue;
                    }

                    Log.e(TAG, "inserting data: time: " + timestamp + " temp: " + temp);

                    long time = Long.parseLong(timestamp);
                    insertTemp(temp, time);
                }
            }catch (IOException io){
                Log.e(TAG, "temp 2:" + io.toString());
            }

            try {
                osw_temp = new OutputStreamWriter(new FileOutputStream(out_temp, true));
            }catch (IOException io){

            }
        }
        if(type.equals("humid")){
            try {
                osw_humid.close();
            }catch (IOException io){

            }

            //read in all of the humidity data
            try {
                String row = "";
                BufferedReader br = new BufferedReader(new FileReader(out_humid));
                while((row = br.readLine()) != null){
                    //split the rows up.
                    String[] args = row.split(",");
                    String timestamp = args[0];
                    String humid = args[1];

                    if(timestamp.charAt(0) > 58 || humid.charAt(0) > 58){
                        continue;
                    }

                    Log.e(TAG, "inserting data: time: " + timestamp + " humid: " + humid);

                    long time = Long.parseLong(timestamp);
                    insertHumid(humid, time);
                }
            }catch (IOException io){
                Log.e(TAG, "Humid 2:" + io.toString());
            }

            try {
                osw_humid = new OutputStreamWriter(new FileOutputStream(out_humid, true));
            }catch (IOException io){

            }
        }


    }

    public void connectGatt(BluetoothGatt b){
        bluetooth = b;
    }

    public boolean isConnected(){
        return connected;
    }

    public void Connect(){
        if(bluetooth != null) {
            connected = true;
        }
    }

    public void Disconnect(){
        connected = false;
    }

    public void insertTemp(String T, long time){
        float temp = Float.parseFloat(T);
        temperatureSeries.appendData(new DataPoint(new Date(time), (double)temp), true, 10);
    }

    public void updateTemp(String T){
        currTemp = Float.parseFloat(T);
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        temperatureSeries.appendData(
                new DataPoint(gregorianCalendar.getTime(), (double) currTemp),
                true, 10);
        if(osw_temp != null) {
            try {
                osw_temp.write("" + gregorianCalendar.getTimeInMillis() + "," + T + "\n");
            } catch (IOException io) {
                Log.e(TAG, io.toString());
            }
        }
    }

    public void insertHumid(String rH, long time){
        float humid = Float.parseFloat(rH);
        humiditySeries.appendData(new DataPoint(new Date(time), (double)humid), true, 10);
    }

    public void updateHumid(String rH){
        currHumid = Float.parseFloat(rH);
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        humiditySeries.appendData(
                new DataPoint(gregorianCalendar.getTime(), (double) currHumid),
                false, 10);

        if(osw_humid != null) {
            try {
                osw_humid.write("" + gregorianCalendar.getTimeInMillis() + "," + currHumid + "\n");
            } catch (IOException io) {
                Log.e(TAG, io.toString());
            }
        }
    }

    public Float getTemp(){
        return currTemp;
    }

    public Float getHumid(){
        return currHumid;
    }

    public BluetoothGatt getGATT(){
        if(connected) {
            return bluetooth;
        }
        return null;
    }

    public View getParentView(boolean isExpanded, View convertView, ViewGroup parent){
        View v;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.sensor_group, parent, false);

        TextView groupTitle = (TextView)v.findViewById(R.id.sensor_group_text);
        groupTitle.setText(name);

        TextView temp = (TextView)v.findViewById(R.id.temperature);
        TextView humid = (TextView)v.findViewById(R.id.humidity);

        temp.setText("temperature: " + currTemp.toString() + " C");
        humid.setText("humidity: " + currHumid.toString() + "%");

        return v;
    }

    public View getChildView(int child, View convertView, ViewGroup parent){

        View v;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.sensor_graph, parent, false);

        switch(child){
            case 0:
                temperatureSeries.getHighestValueX();
                humiditySeries.getHighestValueX();

                temperatureGraph = (GraphView)v.findViewById(R.id.graph);
                temperatureGraph.addSeries(temperatureSeries);
                temperatureGraph.addSeries(humiditySeries);
                temperatureGraph.getViewport().setXAxisBoundsManual(true);
                temperatureGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(context));
                temperatureGraph.getGridLabelRenderer().setNumHorizontalLabels(4);
                return v;
        }
        return null;
    }

    public boolean isExpanded(){
        return expanded;
    }

    public void expand(){
        expanded = true;
    }

    public void collapse(){
        expanded = false;
    }

    public int nChildren(){
        return 1;
    }

}
