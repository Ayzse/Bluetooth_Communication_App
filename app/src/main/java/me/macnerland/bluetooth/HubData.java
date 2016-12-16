package me.macnerland.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;

/**
 * Created by Doug on 8/15/2016.
 * This represents a hub, and provides the underlying interface and data.
 */
class HubData {

    private BluetoothGatt gatt;

    private String hubAlertNumber;
    private String hubPortalNumber;
    private String hubPortalFreq;
    private String hubLogFreq;
    private String hubTime;
    private String hubDate;
    private String hubCritTemp;
    private String hubCritHum;

    private boolean isConnected;

    private static final byte[] removeASensor = {(byte)'9', (byte)' ', (byte)'\n'};
    private static final byte[] terminator = {(byte)'\n'};


    private static final byte[] removeAllSensors = {(byte)'1', (byte)'0', (byte)' ', (byte)'\n'};
    private static final byte[] getAlertNumber = {(byte)'1', (byte)'1', (byte)' ', (byte)'\n'};
    private static final byte[] getPortalNumber = {(byte)'1', (byte)'3', (byte)' ', (byte)'\n'};
    private static final byte[] getPortalFreq = {(byte)'1', (byte)'5', (byte)' ', (byte)'\n'};
    private static final byte[] getLogFreq = {(byte)'1', (byte)'7', (byte)' ', (byte)'\n'};
    private static final byte[] getHubTime = {(byte)'1', (byte)'9', (byte)' ', (byte)'\n'};
    private static final byte[] getHubDate = {(byte)'2', (byte)'1', (byte)' ', (byte)'\n'};
    private static final byte[] getCritTemp = {(byte)'2', (byte)'3', (byte)' ', (byte)'\n'};
    private static final byte[] getCritHum = {(byte)'2', (byte)'5', (byte)' ', (byte)'\n'};

    private static UUID hubServiceGattUUID =      new UUID(0x0000ffe000001000L, 0x800000805f9b34fbL);
    private static final UUID hubCharacteristicGattUUID =   new UUID(0x0000ffe100001000L, 0x800000805f9b34fbL);

    HubData(BluetoothGatt bg){
        gatt = bg;

        hubAlertNumber = "No Hub Connected";
        hubPortalNumber = "No Hub Connected";
        hubPortalFreq = "No Hub Connected";
        hubLogFreq = "No Hub Connected";
        hubTime = "No Hub Connected";
        hubDate = "No Hub Connected";
        hubCritTemp = "No Hub Connected";
        hubCritHum = "No Hub Connected";

        isConnected = false;
    }

    public void setAlertNumber(String value){
        hubAlertNumber = value;
    }

    public void setPortalNumber(String value){
        hubPortalNumber = value;
    }

    public void setPortalFreq(String value){
        hubPortalFreq = value;
    }

    public void setLogFrequency(String value){
        hubLogFreq = value;
    }

    public void setTime(String value){
        hubTime = value;
    }

    public void setDate(String value){
        hubDate = value;
    }

    public void setCriticalTemperature(String value){
        hubCritTemp = value;
    }

    public void setCriticalHumidity(String value){
        hubCritHum = value;
    }

    public void connect(){
        isConnected = true;
    }

    public void disconnect(){
        isConnected = false;
    }

    public boolean isConnected() { return isConnected;}

    public BluetoothGatt getGATT(){
        return gatt;
    }

    public String getAlertNumber(){
        return hubAlertNumber;
    }

    public String getPortalNumber(){
        return hubPortalNumber;
    }

    public String getPortalFreq(){
        return hubPortalFreq;
    }

    public String getLogFrequency(){
        return hubLogFreq;
    }

    public String getTime(){
        return hubTime;
    }

    public String getDate(){
        return hubDate;
    }

    public String getCriticalTemperature(){
        return hubCritTemp;
    }

    public String getCriticalHumidity(){
        return hubCritHum;
    }

    /* */

    public void fetchPortalNumber(){
        BluetoothGattService bgs = gatt.getService(hubServiceGattUUID);
        BluetoothGattCharacteristic bgc = bgs.getCharacteristic(hubCharacteristicGattUUID);
        bgc.setValue(getPortalNumber);
        gatt.writeCharacteristic(bgc);
    }

    public void fetchAlertNumber(){
        BluetoothGattService bgs = gatt.getService(hubServiceGattUUID);
        BluetoothGattCharacteristic bgc = bgs.getCharacteristic(hubCharacteristicGattUUID);
        bgc.setValue(getAlertNumber);
        gatt.writeCharacteristic(bgc);
    }

    public void fetchPortalFreq(){
        BluetoothGattService bgs = gatt.getService(hubServiceGattUUID);
        BluetoothGattCharacteristic bgc = bgs.getCharacteristic(hubCharacteristicGattUUID);
        bgc.setValue(getPortalFreq);
        gatt.writeCharacteristic(bgc);
    }

    public void fetchLogFreq(){
        BluetoothGattService bgs = gatt.getService(hubServiceGattUUID);
        BluetoothGattCharacteristic bgc = bgs.getCharacteristic(hubCharacteristicGattUUID);
        bgc.setValue(getLogFreq);
        gatt.writeCharacteristic(bgc);
    }

    public void fetchTime(){
        BluetoothGattService bgs = gatt.getService(hubServiceGattUUID);
        BluetoothGattCharacteristic bgc = bgs.getCharacteristic(hubCharacteristicGattUUID);
        bgc.setValue(getHubTime);
        gatt.writeCharacteristic(bgc);
    }

    public void fetchDate(){
        BluetoothGattService bgs = gatt.getService(hubServiceGattUUID);
        BluetoothGattCharacteristic bgc = bgs.getCharacteristic(hubCharacteristicGattUUID);
        bgc.setValue(getHubDate);
        gatt.writeCharacteristic(bgc);
    }

    public void fetchCritTemp(){
        BluetoothGattService bgs = gatt.getService(hubServiceGattUUID);
        BluetoothGattCharacteristic bgc = bgs.getCharacteristic(hubCharacteristicGattUUID);
        bgc.setValue(getCritTemp);
        gatt.writeCharacteristic(bgc);
    }

    public void fetchCritHumid(){
        BluetoothGattService bgs = gatt.getService(hubServiceGattUUID);
        BluetoothGattCharacteristic bgc = bgs.getCharacteristic(hubCharacteristicGattUUID);
        bgc.setValue(getCritHum);
        gatt.writeCharacteristic(bgc);
    }

    public void removeSensor(String name){
        BluetoothGattService bgs = gatt.getService(hubServiceGattUUID);
        BluetoothGattCharacteristic bgc = bgs.getCharacteristic(hubCharacteristicGattUUID);
        bgc.setValue(removeASensor);
        gatt.writeCharacteristic(bgc);

    }

    public void removeAllSensors(){
        BluetoothGattService bgs = gatt.getService(hubServiceGattUUID);
        BluetoothGattCharacteristic bgc = bgs.getCharacteristic(hubCharacteristicGattUUID);
        bgc.setValue(removeAllSensors);
        gatt.writeCharacteristic(bgc);
    }
    
}
