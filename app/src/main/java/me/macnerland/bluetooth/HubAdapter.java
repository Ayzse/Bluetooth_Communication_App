package me.macnerland.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Created by Doug on 8/15/2016.
 */
public class HubAdapter implements ListAdapter {

    private Hashtable<String, Integer> hubIndex;
    private Vector<HubData> hubs;
    private Vector<DataSetObserver> DSO;
    private Context context;

    public HubAdapter(Context c){
        hubIndex = new Hashtable<>();
        hubs = new Vector<>();
        DSO = new Vector<>();
        context = c;
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
        View v;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.sensor_group, parent, false);

        HubData hub = hubs.get(position);

        String hubAlertNumber = "Alert number: " + hub.getAlertNumber();
        String hubPortalNumber = "Portal number: " + hub.getPortalNumber();
        String hubPortalFreq = "Portal Frequency: " + hub.getPortalFreq();
        String hubLogFreq = "Logging Frequency: " + hub.getPortalFreq();
        String hubTime = "Time: " + hub.getTime();
        String hubDate = "Date: " + hub.getDate();
        String hubCritTemp = "Critical Temperature: " + hub.getCriticalTemperature();
        String hubCritHum = "Critical Humidity: " + hub.getCriticalHumidity();

        TextView alertNumber = (TextView)v.findViewById(R.id.alertNumber);
        TextView portalNumber = (TextView)v.findViewById(R.id.portalNumber);
        TextView portalFreq = (TextView)v.findViewById(R.id.portalFreq);
        TextView logFreq = (TextView)v.findViewById(R.id.logFreq);
        TextView Time = (TextView)v.findViewById(R.id.hubTime);
        TextView Date = (TextView)v.findViewById(R.id.hubDate);
        TextView critTemp = (TextView)v.findViewById(R.id.critTemp);
        TextView critHum = (TextView)v.findViewById(R.id.critHumid);

        alertNumber.setText(hubAlertNumber);
        portalNumber.setText(hubPortalNumber);
        portalFreq.setText(hubPortalFreq);
        logFreq.setText(hubLogFreq);
        Time.setText(hubTime);
        Date.setText(hubDate);
        critTemp.setText(hubCritTemp);
        critHum.setText(hubCritHum);


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
