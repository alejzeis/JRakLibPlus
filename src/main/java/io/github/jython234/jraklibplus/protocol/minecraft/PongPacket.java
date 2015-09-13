package io.github.jython234.jraklibplus.protocol.minecraft;

import static io.github.jython234.jraklibplus.JRakLibPlus.*;

/**
 * MC_PONG Packet implementation.
 *
 * @author RedstoneLamp Team
 */
public class PongPacket extends PingPacket {

    @Override
    public byte getPID() {
        return MC_PONG;
    }
}
