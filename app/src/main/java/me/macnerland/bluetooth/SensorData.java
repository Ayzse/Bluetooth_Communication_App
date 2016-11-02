package me.macnerland.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.os.Parcel;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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

    LineGraphSeries<DataPoint> temperatureSeries;
    LineGraphSeries<DataPoint> humiditySeries;

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

    SensorData(Parcel in){

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
        humidityGraph = new GraphView(context);
        humidityGraph.setTitle("Humidity");

        temperatureSeries = new LineGraphSeries<>();
        humiditySeries = new LineGraphSeries<>();

        temperatureGraph.addSeries(temperatureSeries);
        humidityGraph.addSeries(humiditySeries);

        BluetoothDevice bd = bg.getDevice();

        name = bd.getName();
        address = bd.getAddress();

        if(can_write) {
            String filename_temp = context.getExternalFilesDir(null).toString() + "/sensors/" + name + "_" + address + "_temp.csv";
            String filename_humid = context.getExternalFilesDir(null).toString() + "/sensors/" + name + "_" + address + "_humid.csv";
            File out_temp = new File(filename_temp);
            File out_humid = new File(filename_humid);
            boolean temp_exists = out_temp.exists();
            boolean humid_exists = out_humid.exists();
            out_temp.mkdirs();
            out_humid.mkdirs();

            try{
                osw_temp = new OutputStreamWriter(new FileOutputStream(out_temp, true));
                if(!temp_exists){
                    osw_temp.write(header_temp);
                }
            }catch (IOException io){
                Log.e(TAG, io.toString());
            }

            try{
                osw_humid = new OutputStreamWriter(new FileOutputStream(out_temp, true));
                if(!humid_exists) {
                    osw_humid.write(header_humid);
                }
            }catch (IOException io){
                Log.e(TAG, io.toString());
            }
        }
        else{
            osw_humid = null;
            osw_temp = null;
        }
    }

    public boolean enableWrite(){
        String filename_temp = context.getExternalFilesDir(null).toString() + "/sensors/" + name + "_" + address + "_temp.csv";
        String filename_humid = context.getExternalFilesDir(null).toString() + "/sensors/" + name + "_" + address + "_humid.csv";
        File out_temp = new File(filename_temp);
        File out_humid = new File(filename_humid);

        try{
            osw_temp = new OutputStreamWriter(new FileOutputStream(out_temp, true));
            osw_temp.write(header_temp);
        }catch (IOException io){
            Log.e(TAG, io.toString());
        }

        try{
            osw_humid = new OutputStreamWriter(new FileOutputStream(out_temp, true));
            osw_humid.write(header_humid);
        }catch (IOException io){
            Log.e(TAG, io.toString());
        }
        return true;
    }


    public boolean isConnected(){
        return connected;
    }

    public void Connect(){
        connected = true;
    }

    public void Disconnect(){
        connected = false;
        if(osw_humid != null){
            try {
                osw_humid.close();
            }catch(IOException io){
                Log.e(TAG, io.toString());
            }
        }
        if(osw_temp != null){
            try {
                osw_temp.close();
            }catch(IOException io){
                Log.e(TAG, io.toString());
            }
        }
    }

    public void insertTemp(String T, long ){

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
        return bluetooth;
    }

    public View getParentView(boolean isExpanded, View convertView, ViewGroup parent){
        View v;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.sensor_group, parent, false);

        TextView groupTitle = (TextView)v.findViewById(R.id.sensor_group_text);
        groupTitle.setText(bluetooth.getDevice().getName());

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
        v = inflater.inflate(R.layout.humidity_graph, parent, false);

        switch(child){
            case 0:
                temperatureGraph = (GraphView)v.findViewById(R.id.temperature_graph);
                temperatureGraph.addSeries(temperatureSeries);

                humidityGraph = (GraphView)v.findViewById(R.id.humidity_graph);
                humidityGraph.addSeries(humiditySeries);
                return v;
            case 1:
                Log.i(TAG, "returning humidity");
                return humidityGraph;
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
