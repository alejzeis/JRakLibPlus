package io.github.jython234.jraklibplus.protocol.raknet;

import io.github.jython234.jraklibplus.protocol.RakNetPacket;
import io.github.jython234.jraklibplus.nio.Buffer;
import static io.github.jython234.jraklibplus.JRakLibPlus.*;

/**
 * ID_OPEN_CONNECTION_REQUEST_1 Packet implementation
 *
 * @author RedstoneLamp Team
 */
public class OpenConnectionRequest1Packet extends RakNetPacket {

    public byte protocolVersion;
    public int nullPayloadLength;

    @Override
    protected void _encode(Buffer buffer) {
        buffer.put(RAKNET_MAGIC);
        buffer.putByte(protocolVersion);
        buffer.put(new byte[nullPayloadLength - 18]);
    }

    @Override
    protected void _decode(Buffer buffer) {
        buffer.skip(16); //MAGIC
        protocolVersion = buffer.getByte();
        nullPayloadLength = buffer.getRemainingBytes() + 18;
    }

    @Override
    public byte getPID() {
        return ID_OPEN_CONNECTION_REQUEST_1;
    }

    @Override
    public int getSize() {
        return nullPayloadLength; //The payload length should be the entire length of the packet
    }
}
