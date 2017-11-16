package me.macnerland.bluetooth;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

/**
 * displays a list of connected sensors
 * Created by Doug on 7/23/2016.
 */

public class SensorFragment extends Fragment {
    private SensorAdapter sensorAdapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.sensor_interface, container, false);

        if(sensorAdapter == null) {
            sensorAdapter = new SensorAdapter(getActivity());
        }
        ExpandableListView elv = (ExpandableListView)rootView.findViewById(R.id.elv);
        elv.setAdapter(sensorAdapter);

        setRetainInstance(true);
        return rootView;
    }

    void addSensor(BluetoothDevice device){
        if(sensorAdapter != null){
            sensorAdapter.addSensor(getActivity(), device);
        }
    }

    void enableWrite(){
        if(sensorAdapter != null){
            sensorAdapter.enableWrite();
        }
    }

    void connectSensor(String address, BluetoothDevice device){
        if(sensorAdapter != null){
            sensorAdapter.connectSensor(address, device);
        }
    }

    void updateNotification(String address){
        if(sensorAdapter != null){
            sensorAdapter.updateNotification(address);
        }
    }

    void updateTemperature(String address){
        if(sensorAdapter != null){
            sensorAdapter.updateTemperature(address);
        }
    }

    void updateHumidity(String address){
        if(sensorAdapter != null){
            sensorAdapter.updateHumidity(address);
        }
    }

    void disconnect(String address){
        if(sensorAdapter != null) {
            sensorAdapter.disconnect(address);
        }
    }

    void deliverData(String address, String data){
        if(sensorAdapter != null) {
            sensorAdapter.deliverData(address, data);
        }
    }

    void notifyDSO(){
        if(sensorAdapter != null) {
            sensorAdapter.notifyDSO();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if(sensorAdapter != null) {
            sensorAdapter.onPause();
        }
    }

    SensorAdapter getSensorAdapter(){
        return sensorAdapter;
    }
}
