package me.macnerland.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Provides the backing data on a connected sensor
 * This class represents a sesnor and all of the data associated with it.
 * Created by Doug on 7/31/2016.
 */
public class SensorData{

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

    SensorData(Parcel in){

    }

    SensorData(BluetoothGatt bg, Context c){
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
    }

    public boolean isConnected(){
        return connected;
    }

    public void Connect(){
        connected = true;
    }

    public void Disconnect(){
        connected = false;
    }

    public void updateTemp(String T){
        currTemp = Float.parseFloat(T);
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        temperatureSeries.appendData(
                new DataPoint(gregorianCalendar.getTime(), (double) currTemp),
                true, 10);
    }

    public void updateHumid(String rH){
        currHumid = Float.parseFloat(rH);
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        temperatureSeries.appendData(
                new DataPoint(gregorianCalendar.getTime(), (double)currHumid),
                true, 10);
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

        switch(child){
            case 0:
                return temperatureGraph;
            case 1:
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
        if(expanded){
            return 2;
        }else{
            return 0;
        }
    }

}
