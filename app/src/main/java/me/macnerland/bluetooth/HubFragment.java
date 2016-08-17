package me.macnerland.bluetooth;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Doug on 7/23/2016.
 */
public class HubFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.hub_interface, container, false);
        String hubAlertNumber = MainActivity.getHubAlertNumber();
        String hubPortalNumber = MainActivity.getHubPortalNumber();
        String hubPortalFreq = MainActivity.getHubPortalFreq();
        String hubLogFreq = MainActivity.getHubLogFreq();
        String hubTime = MainActivity.getHubTime();
        String hubDate = MainActivity.getHubDate();
        String hubCritTemp = MainActivity.getHubCritTemp();
        String hubCritHum = MainActivity.getHubCritHum();

        TextView alertNumber = (TextView)rootView.findViewById(R.id.alertNumber);
        alertNumber.setText("Alert phone number: " + hubAlertNumber);
        TextView portalNumber = (TextView)rootView.findViewById(R.id.portalNumber);
        portalNumber.setText("Portal number: " + hubPortalNumber);
        TextView portalFreq = (TextView)rootView.findViewById(R.id.portalFreq);
        portalFreq.setText("Portal message frequency: " + hubPortalFreq);
        TextView logFreq = (TextView)rootView.findViewById(R.id.logFreq);
        logFreq.setText("Sensor logging frequency: " + hubLogFreq);
        TextView time = (TextView)rootView.findViewById(R.id.hubTime);
        time.setText("Hub time: " + hubTime);
        TextView date = (TextView)rootView.findViewById(R.id.hubDate);
        date.setText("Hub date: " + hubDate);
        TextView critTemp = (TextView)rootView.findViewById(R.id.critTemp);
        critTemp.setText("Critical temperature threshold: " + hubCritTemp);
        TextView critHum = (TextView)rootView.findViewById(R.id.critHumid);
        critHum.setText("Critical humidity threshold: " + hubCritHum);
        return rootView;
    }
}
