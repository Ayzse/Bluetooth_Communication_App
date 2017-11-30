package land.macner.bluetooth;

import java.util.UUID;

/**
 * Constants used across several classes
 * Created by doug on 5/10/17.
 */

class Constant {
    static final UUID sensorCharacteristicUUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    static final UUID hubServiceGattUUID =      new UUID(0x0000ece000001000L, 0x800000805f9b34fbL);
    static UUID[] hubUUID = {hubServiceGattUUID};
    static final UUID sensorServiceGattUUID =   new UUID(0x0000ece100001000L, 0x800000805f9b34fbL);
    static UUID[] sensorUUID = {sensorServiceGattUUID};

    static final UUID defaultServiceGattUUID =   new UUID(0x0000180100001000L, 0x800000805f9b34fbL);
    static UUID[] defaultUUID = {defaultServiceGattUUID};
    static final UUID hubCharacteristicGattUUID =   new UUID(0x0000ffe100001000L, 0x800000805f9b34fbL);

}
