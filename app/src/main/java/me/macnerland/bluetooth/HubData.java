package me.macnerland.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.UUID;

/**
 * Created by Doug on 8/15/2016.
 * This represents a hub, and provides the underlying interface and data.
 */
class HubData implements HubInterface{
    private static final String TAG = "HubData";

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
    private int selected_command;

    private static final int HUB_NO_DATA_PENDING = 0;
    private static final int HUB_ALERT_PHONE_NUMBER_PENDING = 1;
    private static final int HUB_PORTAL_PHONE_NUMBER_PENDING = 2;
    private static final int HUB_PORTAL_FREQ_PENDING = 3;
    private static final int HUB_LOG_FREQ_PENDING = 4;
    private static final int HUB_TIME_PENDING = 5;
    private static final int HUB_DATE_PENDING = 6;
    private static final int HUB_CRIT_TEMP_PENDING = 7;
    private static final int HUB_CRIT_HUM_PENDING = 8;
    private int datastate;

    //From Luis's command list. Commands 1, 6, 7, 27 have not been implemented and are
    //indices 0, 5, 6, and 26
    //each command is the index into the command_formats array
    static final int getHubId = 1;
    static final int setHubId = 2;
    static final int getNumberOfSensors = 3;
    static final int getListOfSensors = 4;
    static final int getASensorID = 0;
    static final int setASensorID = 0;
    static final int addASensor = 7;
    static final int removeASensor = 8;
    static final int removeAllSensors = 9;
    static final int getAlertPhoneNumber = 10;
    static final int setAlertPhoneNumber = 11;
    static final int getPortalPhoneNumber = 12;
    static final int setPortalPhoneNumber = 13;
    static final int getPortalNotificationFreq = 14;
    static final int setPortalNotificationFreq = 15;
    static final int getLoggingFrequency = 16;
    static final int setLoggingFrequency = 17;
    static final int getHubTime = 18;
    static final int setHubTime = 19;
    static final int getHubDate = 20;
    static final int setHubDate = 21;
    static final int getCriticalTemperature = 22;
    static final int setCriticalTemperature = 23;
    static final int getCriticalHumidity = 24;
    static final int setCriticalHumidity =25;

    private static final String[] command_formats = {"",
    "2 \n",
    "3 %s\n",
    "4 \n",
    "5 \n",
    "",
    "",
    "8 %s\n",
    "9 %s\n",
    "10 \n",
    "11 \n",
    "12 %s\n",
    "13 \n",
    "14 %s\n",
    "15 \n",
    "16 %s\n",
    "17 \n",
    "18 %s\n",
    "19 \n",
    "20 %s\n",
    "21 \n",
    "22 %s\n",
    "23 \n",
    "24 %s\n",
    "25 \n",
    "26 %s\n",
    ""};

    //this array must be in order.
    private int[] display_to_command = {setPortalPhoneNumber, setPortalNotificationFreq, setLoggingFrequency,
    setHubTime, setHubDate, setCriticalTemperature, setCriticalHumidity};


    private static final UUID hubServiceGattUUID = new UUID(0x0000ece000001000L, 0x800000805f9b34fbL);
    private static final UUID hubCharacteristicGattUUID =   new UUID(0x0000ffe100001000L, 0x800000805f9b34fbL);

    private boolean initializing;

    HubData(BluetoothGatt bg){
        datastate = HUB_NO_DATA_PENDING;
        gatt = bg;
        selected_command = 0;

        hubAlertNumber = "No Hub Connected";
        hubPortalNumber = "No Hub Connected";
        hubPortalFreq = "No Hub Connected";
        hubLogFreq = "No Hub Connected";
        hubTime = "No Hub Connected";
        hubDate = "No Hub Connected";
        hubCritTemp = "No Hub Connected";
        hubCritHum = "No Hub Connected";

        isConnected = false;
        initializing = false;
        //initialize();
    }

    //dummy constructer
    HubData(String han, String hpn, String hpf, String hlf, String ht, String hd, String hct, String hch){
        datastate = HUB_NO_DATA_PENDING;
        gatt = null;
        selected_command = 0;

        hubAlertNumber = han;
        hubPortalNumber = hpn;
        hubPortalFreq = hpf;
        hubLogFreq = hlf;
        hubTime = ht;
        hubDate = hd;
        hubCritTemp = hct;
        hubCritHum = hch;

        isConnected = false;
        initializing = false;
    }

    //call this to retrieve all of the hub info
    void initialize(){
        initializing = true;
        Log.i(TAG, "initializing the hub data");
        sendCommand(getAlertPhoneNumber, "");
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

    void send(String parameter){
        sendCommand(display_to_command[selected_command], parameter);
    }

    //Issue a command to the hub. command must be one of the enumerated commands.
    //The parameter is the argument for the command. Some commands will not have parameters
    boolean sendCommand(int command, String parameter){
        //if the parameter is greater than the maximum parameter length, return.
        //if bad command, return. If currently waiting for data, return.
        if(parameter.length() > 16 || command == 0 || datastate != HUB_NO_DATA_PENDING){
            return false;
        }

        //different commands have different options
        String str = command_formats[command];
        switch(command){
            case 2:
                str = String.format(str, parameter);
                Log.v(TAG, "Adding in parameter: " + parameter);
                break;
            case 5:
                str = String.format(str, parameter);
                Log.v(TAG, "Adding in parameter: " + parameter);
                break;
            case 6:
                str = String.format(str, parameter);
                Log.v(TAG, "Adding in parameter: " + parameter);
                break;
            case 7:
                str = String.format(str, parameter);
                Log.v(TAG, "Adding in parameter: " + parameter);
                break;
            case 8:
                str = String.format(str, parameter);
                Log.v(TAG, "Adding in parameter: " + parameter);
                break;
            case 10://get alert phone number
                datastate = HUB_ALERT_PHONE_NUMBER_PENDING;
                break;
            case 11:
                str = String.format(str, parameter);
                Log.v(TAG, "Adding in parameter: " + parameter);
                break;
            case 12://get portal phone number
                datastate = HUB_PORTAL_PHONE_NUMBER_PENDING;
                break;
            case 13://set portal phone number
                str = String.format(str, parameter);
                Log.v(TAG, "Adding in parameter: " + parameter);
                break;
            case 14://get portal notification frequency
                datastate = HUB_PORTAL_FREQ_PENDING;
                break;
            case 15:
                str = String.format(str, parameter);
                Log.v(TAG, "Adding in parameter: " + parameter);
                break;
            case 16://get logging frequency
                datastate = HUB_LOG_FREQ_PENDING;
                break;
            case 17:
                str = String.format(str, parameter);
                Log.v(TAG, "Adding in parameter: " + parameter);
                break;
            case 18://get hub time
                datastate = HUB_TIME_PENDING;
                break;
            case 19:
                str = String.format(str, parameter);
                Log.v(TAG, "Adding in parameter: " + parameter);
                break;
            case 20://get hub date
                datastate = HUB_DATE_PENDING;
                break;
            case 21:
                str = String.format(str, parameter);
                Log.v(TAG, "Adding in parameter: " + parameter);
                break;
            case 22://get critical temperature
                datastate = HUB_CRIT_TEMP_PENDING;
                break;
            case 23:
                str = String.format(str, parameter);
                Log.v(TAG, "Adding in parameter: " + parameter);
                break;
            case 24://get critical humidity
                datastate = HUB_CRIT_HUM_PENDING;
                break;
            case 25:
                str = String.format(str, parameter);
                Log.v(TAG, "Adding in parameter: " + parameter);
                break;
            default:
                break;
        }
        Log.v(TAG, "sending hub command: " + str);

        BluetoothGattService bgs = gatt.getService(hubServiceGattUUID);
        if(bgs != null) {
            BluetoothGattCharacteristic bgc = bgs.getCharacteristic(hubCharacteristicGattUUID);
            if(bgc != null){

                char[] strAsCharacters = str.toCharArray();
                byte[] strAsBytes = new byte[strAsCharacters.length];
                for(int i = 0; i < strAsCharacters.length; ++i){
                    strAsBytes[i] = (byte)strAsCharacters[i];
                }


                bgc.setValue(strAsBytes);
                gatt.writeCharacteristic(bgc);
                return true;
            }else{
                Log.e(TAG, "Hub's Bluetooth gatt Characteristic is null");
            }
        }else{
            Log.e(TAG, "Hub's Bluetooth Gatt Service is null");
        }
        return false;
    }

    //recieve the data for this hub. The data will not include the type of information
    //so it is important that the hub keeps track of what type data it is asking for
    boolean receiveData(String dat){
        String data = dat.trim();
        boolean ret = false;
        int temp = datastate;
        switch(temp){
            case HUB_ALERT_PHONE_NUMBER_PENDING:
                hubAlertNumber = data;
                datastate = HUB_NO_DATA_PENDING;
                if(initializing){
                    sendCommand(getPortalPhoneNumber, "");
                }
                break;
            case HUB_PORTAL_PHONE_NUMBER_PENDING:
                hubPortalNumber = data;
                datastate = HUB_NO_DATA_PENDING;
                if(initializing){
                    sendCommand(getPortalNotificationFreq, "");
                }
                break;
            case HUB_PORTAL_FREQ_PENDING:
                hubPortalFreq = data;
                datastate = HUB_NO_DATA_PENDING;
                if(initializing){
                    sendCommand(getLoggingFrequency, "");
                }
                break;
            case HUB_LOG_FREQ_PENDING:
                hubLogFreq = data;
                datastate = HUB_NO_DATA_PENDING;
                if(initializing){
                    sendCommand(getHubTime, "");
                }
                break;
            case HUB_TIME_PENDING:
                hubTime = data;
                datastate = HUB_NO_DATA_PENDING;
                if(initializing){
                    sendCommand(getHubDate, "");
                }
                break;
            case HUB_DATE_PENDING:
                hubDate = data;
                datastate = HUB_NO_DATA_PENDING;
                if(initializing){
                    sendCommand(getCriticalTemperature, "");
                }
                break;
            case HUB_CRIT_TEMP_PENDING:
                hubCritTemp = data;
                datastate = HUB_NO_DATA_PENDING;
                if(initializing){
                    sendCommand(getCriticalHumidity, "");
                }
                break;
            case HUB_CRIT_HUM_PENDING:
                hubCritHum = data;
                datastate = HUB_NO_DATA_PENDING;
                if(initializing){
                    initializing = false;
                    ret = true;
                }
                break;
            case HUB_NO_DATA_PENDING:
            default:
                Log.e(TAG, "Bad Data State");
        }
        return ret;
    }

    //on item selected
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)

        String command = (String)parent.getItemAtPosition(pos);
        Log.v(TAG, "Item selected:" + command);

    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        Log.v(TAG, "Failure to select anything");

    }

    public void onClick(View v){
        LinearLayout o = (LinearLayout)v.getParent();
        EditText et = (EditText)o.findViewById(R.id.command);
        String command = et.getText().toString();
        Log.e(TAG, "Sending command" + command);
        send(command);
    }
}
