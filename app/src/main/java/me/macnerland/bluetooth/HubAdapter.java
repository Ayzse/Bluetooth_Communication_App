package me.macnerland.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Created by Doug on 8/15/2016.
 * This contains a list of all the connected hubs
 */
class HubAdapter implements ListAdapter {

    private Hashtable<String, Integer> hubIndex;
    private Vector<HubData> hubs;
    private Vector<DataSetObserver> DSO;
    private Context context;

    HubAdapter(Context c){
        hubIndex = new Hashtable<>();
        hubs = new Vector<>();
        DSO = new Vector<>();
        context = c;
    }

    void notifyDSO(){
        for(DataSetObserver dso : DSO){
            dso.onChanged();
        }
    }

    void addHub(BluetoothGatt bg){
        hubs.add(new HubData(bg));
        hubIndex.put(bg.getDevice().getAddress(), hubs.size() - 1);
    }

    HubData getHub(String MAC){
        if(hubIndex.keySet().contains(MAC)){
            return hubs.get(hubIndex.get(MAC));
        }
        return null;
    }

    boolean deliverData(String address, String data){
        if(hubIndex.keySet().contains(address)){
            return hubs.get(hubIndex.get(address)).receiveData(data);
        }
        return false;
    }

    void initialize(String address){
        if(hubIndex.keySet().contains(address)){
            hubs.get(hubIndex.get(address)).initialize();
        }
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

        if(convertView != null){
            v = convertView;
        }else {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.hub_item, parent, false);
        }

        HubData hub = hubs.get(position);

        String hubAlertNumber = "Alert number: " + hub.getAlertNumber();
        String hubPortalNumber = "Portal number: " + hub.getPortalNumber();
        String hubPortalFreq = "Portal Frequency: " + hub.getPortalFreq();
        String hubLogFreq = "Logging Frequency: " + hub.getLogFrequency();
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
        EditText editLogFreq = (EditText)v.findViewById(R.id.editLogFreq);
        Button send = (Button)v.findViewById(R.id.sendButton);
        /*send.setOnClickListener(new View.OnClickListener(){
            public void onClick(){

            }
        });*/


        alertNumber.setText(hubAlertNumber);
        portalNumber.setText(hubPortalNumber);
        portalFreq.setText(hubPortalFreq);
        logFreq.setText(hubLogFreq);
        Time.setText(hubTime);
        Date.setText(hubDate);
        critTemp.setText(hubCritTemp);
        critHum.setText(hubCritHum);

        return v;
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
