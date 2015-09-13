package io.github.jython234.jraklibplus.protocol.raknet;

import static io.github.jython234.jraklibplus.JRakLibPlus.*;

/**
 * ID_ADVERTISE_SYSTEM Packet implementation.
 *
 * @author RedstoneLamp Team
 */
public class AdvertiseSystemPacket extends UnconnectedPongOpenConnectionsPacket {

    @Override
    public byte getPID() {
        return ID_ADVERTISE_SYSTEM;
    }
}
