package me.macnerland.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Created by Doug on 8/15/2016.
 */
public class HubAdapter implements ListAdapter {

    private Hashtable<String, Integer> hubIndex;
    private Vector<HubData> hubs;
    private Vector<DataSetObserver> DSO;

    public HubAdapter(){
        hubIndex = new Hashtable<>();
        hubs = new Vector<>();
        DSO = new Vector<>();
    }

    public void notifyDSO(){
        for(DataSetObserver dso : DSO){
            dso.onChanged();
        }
    }

    public void addHub(BluetoothGatt bg){
        hubs.add(new HubData(bg));
        hubIndex.put(bg.getDevice().getAddress(), hubs.size() - 1);
    }

    public HubData getHub(String MAC){
        if(hubIndex.keySet().contains(MAC)){
            return hubs.get(hubIndex.get(MAC));
        }
        return null;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        DSO.add(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if(DSO.contains(observer)) DSO.remove(observer);
    }

    @Override
    public int getCount() {
        return hubs.size();
    }

    @Override
    public Object getItem(int position) {
        return hubs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return hubs.isEmpty();
    }
}
