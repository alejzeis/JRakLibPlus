package io.github.jython234.jraklibplus.protocol.minecraft;

import io.github.jython234.jraklibplus.protocol.RakNetPacket;
import io.github.jython234.jraklibplus.nio.Buffer;
import static io.github.jython234.jraklibplus.JRakLibPlus.*;

/**
 * MC_CLIENT_CONNECT Packet implementation
 *
 * @author RedstoneLamp Team
 */
public class ClientConnectPacket extends RakNetPacket {

    public long clientID;
    public long sendPing;
    public boolean useSecurity;

    @Override
    protected void _encode(Buffer buffer) {
        buffer.putLong(clientID);
        buffer.putLong(sendPing);
        buffer.putBoolean(useSecurity);
    }

    @Override
    protected void _decode(Buffer buffer) {
        clientID = buffer.getLong();
        sendPing = buffer.getLong();
        useSecurity = buffer.getBoolean();
    }

    @Override
    public byte getPID() {
        return MC_CLIENT_CONNECT;
    }

    @Override
    public int getSize() {
        return 18;
    }
}
