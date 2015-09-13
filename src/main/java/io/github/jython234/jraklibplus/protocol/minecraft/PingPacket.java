package io.github.jython234.jraklibplus.protocol.minecraft;

import io.github.jython234.jraklibplus.nio.Buffer;
import io.github.jython234.jraklibplus.protocol.RakNetPacket;
import static io.github.jython234.jraklibplus.JRakLibPlus.*;

/**
 * MC_PING Packet implementation.
 *
 * @author RedstoneLamp Team
 */
public class PingPacket extends RakNetPacket {

    public long pingID;

    @Override
    protected void _encode(Buffer buffer) {
        buffer.putLong(pingID);
    }

    @Override
    protected void _decode(Buffer buffer) {
        pingID = buffer.getLong();
    }

    @Override
    public byte getPID() {
        return MC_PING;
    }

    @Override
    public int getSize() {
        return 9;
    }
}
