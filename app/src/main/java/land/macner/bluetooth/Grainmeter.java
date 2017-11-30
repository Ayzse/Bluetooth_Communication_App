package land.macner.bluetooth;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.parse.Parse;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by doug on 6/23/17.
 * This class contains functions to communicate to the parse server
 */

public class Grainmeter extends Application {

    private static final String TAG = "Grainmeter";
    private static final String HOST = "https://grainportalv2.herokuapp.com";

    public void onCreate(){
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("GrainPortalv2")
                .clientKey(null)
                .server(HOST)
                .build());
    }


}
