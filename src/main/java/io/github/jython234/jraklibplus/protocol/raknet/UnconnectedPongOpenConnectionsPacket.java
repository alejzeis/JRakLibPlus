package io.github.jython234.jraklibplus.protocol.raknet;

import io.github.jython234.jraklibplus.protocol.RakNetPacket;
import io.github.jython234.jraklibplus.nio.Buffer;
import static io.github.jython234.jraklibplus.JRakLibPlus.*;

/**
 * ID_UNCONNECTED_PONG_OPEN_CONNECTIONS Packet implementation
 *
 * @author RedstoneLamp Team
 */
public class UnconnectedPongOpenConnectionsPacket extends RakNetPacket {

    public long pingID;
    public long serverID;
    public String identifier;

    @Override
    protected void _encode(Buffer buffer) {
        buffer.putLong(pingID);
        buffer.putLong(serverID);
        buffer.put(RAKNET_MAGIC);
        buffer.putString(identifier);
    }

    @Override
    protected void _decode(Buffer buffer) {
        pingID = buffer.getLong();
        serverID = buffer.getLong();
        buffer.skip(16); //MAGIC
        identifier = buffer.getString();
    }

    @Override
    public byte getPID() {
        return ID_UNCONNECTED_PONG_OPEN_CONNECTIONS;
    }

    @Override
    public int getSize() {
        return 35 + identifier.getBytes().length;
    }
}
