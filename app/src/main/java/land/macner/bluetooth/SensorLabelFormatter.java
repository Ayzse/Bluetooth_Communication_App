package land.macner.bluetooth;

import com.jjoe64.graphview.DefaultLabelFormatter;

import java.util.Locale;

/**
 * Created by Doug on 11/15/17.
 * This class takes data points for the GraphView and returns
 * Strings on how they should be represented.
 */

class SensorLabelFormatter extends DefaultLabelFormatter {

    private static final Locale location = new Locale("en");

    @Override
    public String formatLabel(double value, boolean isValueX){
        if(isValueX) {
            double v = value / 1000;
            int min = (int)(((v / 60) % 60));

            int sec = (int)(v % 60);
            return String.format(location, "%02d:%02d", min, sec);
        }
        return String.format(location, "%02d", (int)value);


    }


}
