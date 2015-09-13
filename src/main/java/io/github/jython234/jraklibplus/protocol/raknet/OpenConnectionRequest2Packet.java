package io.github.jython234.jraklibplus.protocol.raknet;

import io.github.jython234.jraklibplus.protocol.RakNetPacket;
import io.github.jython234.jraklibplus.nio.Buffer;
import io.github.jython234.jraklibplus.util.SystemAddress;
import static io.github.jython234.jraklibplus.JRakLibPlus.*;

/**
 * ID_OPEN_CONNECTION_REQUEST_2 Packet Implementation
 *
 * @author RedstoneLamp Team
 */
public class OpenConnectionRequest2Packet extends RakNetPacket {

    public SystemAddress serverAddress;
    /**
     * uint16 (unsigned short)
     */
    public int mtuSize;
    public long clientID;

    @Override
    protected void _encode(Buffer buffer) {
        buffer.put(RAKNET_MAGIC);
        buffer.putAddress(serverAddress);
        buffer.putUnsignedShort(mtuSize);
        buffer.putLong(clientID);
    }

    @Override
    protected void _decode(Buffer buffer) {
        buffer.skip(16); //MAGIC
        serverAddress = buffer.getAddress();
        mtuSize = buffer.getUnsignedShort();
        clientID = buffer.getUnsignedShort();
    }

    @Override
    public byte getPID() {
        return ID_OPEN_CONNECTION_REQUEST_2;
    }

    @Override
    public int getSize() {
        return 34;
    }
}
