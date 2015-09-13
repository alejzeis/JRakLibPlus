package io.github.jython234.jraklibplus.protocol.raknet;

import io.github.jython234.jraklibplus.protocol.RakNetPacket;
import io.github.jython234.jraklibplus.nio.Buffer;
import static io.github.jython234.jraklibplus.JRakLibPlus.*;

/**
 * ID_INCOMPATIBLE_PROTOCOL_VERSION Packet implementation.
 *
 * @author RedstoneLamp Team
 */
public class IncompatibleProtocolVersionPacket extends RakNetPacket {

    public byte protocolVersion;
    public long serverID;

    @Override
    protected void _encode(Buffer buffer) {
        buffer.putByte(protocolVersion);
        buffer.put(RAKNET_MAGIC);
        buffer.putLong(serverID);
    }

    @Override
    protected void _decode(Buffer buffer) {
        protocolVersion = buffer.getByte();
        buffer.skip(16); //MAGIC
        serverID = buffer.getLong();
    }

    @Override
    public byte getPID() {
        return ID_INCOMPATIBLE_PROTOCOL_VERSION;
    }

    @Override
    public int getSize() {
        return 26;
    }
}
