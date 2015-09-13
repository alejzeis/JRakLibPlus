package io.github.jython234.jraklibplus.protocol.minecraft;

import io.github.jython234.jraklibplus.nio.Buffer;
import io.github.jython234.jraklibplus.protocol.RakNetPacket;
import io.github.jython234.jraklibplus.util.SystemAddress;

import static io.github.jython234.jraklibplus.JRakLibPlus.*;

/**
 * MC_CLIENT_HANDSHAKE Packet implementation.
 *
 * @author RedstoneLamp Team
 */
public class ClientHandshakePacket extends RakNetPacket {

    public SystemAddress address;
    public SystemAddress[] systemAddresses;
    public long sendPing;
    public long sendPong;

    @Override
    protected void _encode(Buffer buffer) {
        buffer.putAddress(address);
        for(SystemAddress a : systemAddresses) {
            buffer.putAddress(a);
        }
        buffer.putLong(sendPing);
        buffer.putLong(sendPong);
    }

    @Override
    protected void _decode(Buffer buffer) {
        address = buffer.getAddress();
        systemAddresses = new SystemAddress[10];
        for(int i = 0; i < 10; i++) {
            systemAddresses[i] = buffer.getAddress();
        }
        sendPing = buffer.getLong();
        sendPong = buffer.getLong();
    }

    @Override
    public byte getPID() {
        return MC_CLIENT_HANDSHAKE;
    }

    @Override
    public int getSize() {
        return 94;
    }
}
