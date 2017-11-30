package land.macner.bluetooth;

import android.bluetooth.BluetoothDevice;
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

    private HubAdapter hubAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.hub_interface, container, false);

        //HubAdapter mHubAdapter = MainActivity.getHubAdapter();
        hubAdapter = new HubAdapter(getActivity());
        ListView mHubList = (ListView)rootView.findViewById(R.id.hubList);
        mHubList.setAdapter(hubAdapter);

        setRetainInstance(true);
        return rootView;
    }

    void addHub(BluetoothDevice device){
        if(hubAdapter != null){
            hubAdapter.addHub(getActivity(), device);
        }
    }

    HubData getHub(String address){
        if(hubAdapter != null){
            return hubAdapter.getHub(address);
        }
        return null;
    }

    void initialize(String address){
        if(hubAdapter != null){
            hubAdapter.initialize(address);
        }
    }

    boolean deliverData(String address, String data){
        // intellij thougt this was a good idea
        return hubAdapter == null || hubAdapter.deliverData(address, data);
    }

    void notifyDSO(){
        hubAdapter.notifyDSO();
    }
}
