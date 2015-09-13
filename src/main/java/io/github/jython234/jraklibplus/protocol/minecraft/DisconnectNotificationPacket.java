package io.github.jython234.jraklibplus.protocol.minecraft;

import io.github.jython234.jraklibplus.nio.Buffer;
import io.github.jython234.jraklibplus.protocol.RakNetPacket;
import static io.github.jython234.jraklibplus.JRakLibPlus.*;

/**
 * MC_DISCONNECT_NOTIFICATION Packet implementation.
 *
 * @author RedstoneLamp Team
 */
public class DisconnectNotificationPacket extends RakNetPacket {

    @Override
    protected void _encode(Buffer buffer) {

    }

    @Override
    protected void _decode(Buffer buffer) {

    }

    @Override
    public byte getPID() {
        return MC_DISCONNECT_NOTIFICATION;
    }

    @Override
    public int getSize() {
        return 1;
    }
}
