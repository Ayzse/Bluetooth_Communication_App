package me.macnerland.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.jjoe64.graphview.GraphView;

import java.util.Calendar;

/**
 * Provides the backing data on a connected sensor
 * Created by Doug on 7/31/2016.
 */
public class SensorData {

    Context context;
    GraphView graph;
    public BluetoothDevice device;

    SensorData(BluetoothDevice bd, Context c){
        context = c;
        device = bd;
    }

    public void update(){

    }

    public boolean isExpanded(){
        return false;
    }

    public int nChildren(){
        return 0;
    }
}
