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

    SensorData(BluetoothGatt bg, Context c){
        connected = true;
        bluetooth = bg;
        context = c;
        expanded = false;
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

    public void update(){

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
