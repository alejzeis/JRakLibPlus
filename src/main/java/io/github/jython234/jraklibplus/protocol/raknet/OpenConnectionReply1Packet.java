package io.github.jython234.jraklibplus.protocol.raknet;

import io.github.jython234.jraklibplus.protocol.RakNetPacket;
import io.github.jython234.jraklibplus.nio.Buffer;
import static io.github.jython234.jraklibplus.JRakLibPlus.*;

/**
 * ID_OPEN_CONNECTION_REPLY_1 Packet implementation.
 *
 * @author RedstoneLamp Team
 */
public class OpenConnectionReply1Packet extends RakNetPacket {

    public long serverID;
    /**
     * uint16 (unsigned short)
     */
    public int mtuSize;

    @Override
    protected void _encode(Buffer buffer) {
        buffer.put(RAKNET_MAGIC);
        buffer.putLong(serverID);
        buffer.putByte((byte) 0); //Security
        buffer.putUnsignedShort(mtuSize);
    }

    @Override
    protected void _decode(Buffer buffer) {
        buffer.skip(16);
        serverID = buffer.getLong();
        buffer.getByte(); //security
        mtuSize = buffer.getUnsignedShort();
    }

    @Override
    public byte getPID() {
        return ID_OPEN_CONNECTION_REPLY_1;
    }

    @Override
    public int getSize() {
        return 28;
    }
}
