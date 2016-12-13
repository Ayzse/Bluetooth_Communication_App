package me.macnerland.bluetooth;

/**
 * Created by doug on 12/13/16.
 */

public interface IRemoteServiceCallback {
    void valueChanged(String action, String address, String returnData);
}
