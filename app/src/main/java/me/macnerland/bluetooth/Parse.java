package me.macnerland.bluetooth;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by doug on 6/23/17.
 * This class contains functions to communicate to the parse server
 */

class Parse {

    private static final String TAG = "parse";

    static void create_user(String username, String password){
        String post_body = "{\"username\":\""+username+"\", \"password\":\""+password+"\" }";

        byte[] post_bytes = post_body.getBytes();


        try {
            URL create_user_endpoint = new URL("https", "grainportalv2.herokuapp.com", "parse/users");
            try {
                HttpURLConnection connection = (HttpURLConnection) create_user_endpoint.openConnection();
                try{
                    connection.getDoOutput();
                    connection.setChunkedStreamingMode(0);

                    OutputStream out = new BufferedOutputStream(connection.getOutputStream());

                    out.write(post_bytes);

                } finally {
                    connection.disconnect();
                }
            }   catch(IOException e){
                Log.e(TAG, "IO Exception" + e.toString());
            }
        } catch(MalformedURLException mue){
            Log.e(TAG, "Bad URL" + mue.toString());
        }
    }

    static void login(){


    }
}
