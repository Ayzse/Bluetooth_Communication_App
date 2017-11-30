package land.macner.bluetooth;

/**
 * This is the callback to be used by the service
 * Created by doug on 12/13/16.
 */

public interface IRemoteServiceCallback {
    void valueChanged(String action, String address, String returnData);
}
