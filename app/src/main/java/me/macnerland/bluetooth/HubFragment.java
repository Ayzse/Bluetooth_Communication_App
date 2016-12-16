package me.macnerland.bluetooth;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
/**
 * Created by Doug on 7/23/2016.
 * This class connects the hub adapter (a ListAdapter) to the ListView
 */
public class HubFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.hub_interface, container, false);

        HubAdapter mHubAdapter = MainActivity.getHubAdapter();
        ListView mHubList = (ListView)rootView.findViewById(R.id.hubList);
        mHubList.setAdapter(mHubAdapter);
        return rootView;
    }
}
