package io.github.jython234.jraklibplus.protocol.raknet;

import io.github.jython234.jraklibplus.protocol.raknet.ConnectedPingOpenConnectionsPacket;

import static io.github.jython234.jraklibplus.JRakLibPlus.*;

/**
 * ID_UNCONNECTED_PING_OPEN_CONNECTIONS Packet implementation
 *
 * @author RedstoneLamp Team
 */
public class UnconnectedPingOpenConnectionsPacket extends ConnectedPingOpenConnectionsPacket {

    @Override
    public byte getPID() {
        return ID_UNCONNECTED_PING_OPEN_CONNECTIONS;
    }
}
