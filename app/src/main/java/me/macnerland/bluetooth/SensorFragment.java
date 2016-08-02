package me.macnerland.bluetooth;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

/**
 * displays a list of connected sensors
 * Created by Doug on 7/23/2016.
 */

public class SensorFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.sensor_interface, container, false);
        ExpandableListView elv = (ExpandableListView)rootView.findViewById(R.id.elv);
        return rootView;
    }
}
