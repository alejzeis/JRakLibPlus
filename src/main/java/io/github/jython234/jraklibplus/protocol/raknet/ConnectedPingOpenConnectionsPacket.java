package io.github.jython234.jraklibplus.protocol.raknet;

import io.github.jython234.jraklibplus.protocol.RakNetPacket;
import io.github.jython234.jraklibplus.nio.Buffer;
import static io.github.jython234.jraklibplus.JRakLibPlus.*;

/**
 * ID_CONNECTED_PING_OPEN_CONNECTIONS Packet implementation
 *
 * @author RedstoneLamp Team
 */
public class ConnectedPingOpenConnectionsPacket extends RakNetPacket {

    public long pingID;

    @Override
    protected void _encode(Buffer buffer) {
        buffer.putLong(pingID);
        buffer.put(RAKNET_MAGIC);
    }

    @Override
    protected void _decode(Buffer buffer) {
        pingID = buffer.getLong();
        //MAGIC
    }

    @Override
    public byte getPID() {
        return ID_CONNECTED_PING_OPEN_CONNECTIONS;
    }

    @Override
    public int getSize() {
        return 25;
    }
}
