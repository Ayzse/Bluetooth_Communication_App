package land.macner.bluetooth;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;

import java.util.Vector;

/**
 * Created by doug on 12/4/17.
 *
 * Adapter that stores instances of collected data, and displays it for the user
 */

public class DataDisplayAdapter implements ExpandableListAdapter {

    private Vector<DataSetObserver> DSO;
    private SensorAdapter sensorAdapter;


    DataDisplayAdapter(SensorAdapter s, MainActivity i){
        DSO = new Vector();
        sensorAdapter = i.getSensorAdapter();
    }

    /* Android expandable list adapter functions */

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        DSO.add(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if(DSO.contains(observer)) DSO.remove(observer);
    }

    @Override
    public int getGroupCount() {
        return sensorAdapter.getGroupCount();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return ((SensorData)sensorAdapter.getGroup(groupPosition)).dChildren();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return sensorAdapter.getGroup(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void onGroupExpanded(int groupPosition) {

    }

    @Override
    public void onGroupCollapsed(int groupPosition) {

    }

    @Override
    public long getCombinedChildId(long groupId, long childId) {
        return 0;
    }

    @Override
    public long getCombinedGroupId(long groupId) {
        return 0;
    }
}
