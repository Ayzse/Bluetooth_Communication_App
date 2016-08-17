package me.macnerland.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;

import java.util.Calendar;

/**
 * Provides the backing data on a connected sensor
 * Created by Doug on 7/31/2016.
 */
public class SensorData {

    Context context;
    GraphView graph;
    private BluetoothGatt bluetooth;
    private boolean connected;
    private boolean expanded;
    Float currTemp;
    Float currHumid;

    public int dataState;
    public static final int NO_DATA_PENDING = 0;
    public static final int TEMPERATURE_DATA_PENDING = 1;
    public static final int HUMIDITY_DATA_PENDING = 2;

    SensorData(BluetoothGatt bg, Context c){
        connected = true;
        bluetooth = bg;
        context = c;
        expanded = false;

        currHumid = 0.0f;
        currTemp = 0.0f;
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
    }

    public void updateHumid(String rH){
        currHumid = Float.parseFloat(rH);
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

    public View getChildView(int child, View convertView, ViewGroup parent){

        switch(child){

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
