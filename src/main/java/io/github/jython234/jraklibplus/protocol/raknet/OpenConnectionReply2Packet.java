package io.github.jython234.jraklibplus.protocol.raknet;

import io.github.jython234.jraklibplus.protocol.RakNetPacket;
import io.github.jython234.jraklibplus.nio.Buffer;
import io.github.jython234.jraklibplus.util.SystemAddress;
import static io.github.jython234.jraklibplus.JRakLibPlus.*;

/**
 * ID_OPEN_CONNECTION_REPLY_2 Packet implementation.
 *
 * @author RedstoneLamp Team
 */
public class OpenConnectionReply2Packet extends RakNetPacket {

    public long serverID;
    public SystemAddress clientAddress;
    /**
     * uint16 (unsigned short)
     */
    public int mtuSize;

    @Override
    protected void _encode(Buffer buffer) {
        buffer.put(RAKNET_MAGIC);
        buffer.putLong(serverID);
        buffer.putAddress(clientAddress);
        buffer.putUnsignedShort(mtuSize);
        buffer.putByte((byte) 0); //security
    }

    @Override
    protected void _decode(Buffer buffer) {
        buffer.skip(16); //MAGIC
        serverID = buffer.getLong();
        clientAddress = buffer.getAddress();
        mtuSize = buffer.getUnsignedShort();
        //security
    }

    @Override
    public byte getPID() {
        return ID_OPEN_CONNECTION_REPLY_2;
    }

    @Override
    public int getSize() {
        return 30;
    }
}
