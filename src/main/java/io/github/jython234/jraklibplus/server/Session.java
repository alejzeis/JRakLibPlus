package io.github.jython234.jraklibplus.server;

import io.github.jython234.jraklibplus.JRakLibPlus;
import io.github.jython234.jraklibplus.protocol.raknet.IncompatibleProtocolVersionPacket;
import io.github.jython234.jraklibplus.protocol.raknet.OpenConnectionReply1Packet;
import io.github.jython234.jraklibplus.protocol.raknet.OpenConnectionRequest1Packet;
import io.github.jython234.jraklibplus.util.SystemAddress;
import lombok.Getter;

/**
 * Represents a session opened by a client connected to the RakNetServer.
 *
 * @author jython234
 */
public class Session {
    public static final int DISCONNECTED = 0;
    public static final int CONNECTING_1 = 1;
    public static final int CONNECTING_2 = 2;
    public static final int HANDSHAKING = 3;
    public static final int CONNECTED = 4;

    @Getter private final RakNetServer server;

    @Getter private SystemAddress address;
    @Getter private int state;
    @Getter private int mtu;

    public Session(SystemAddress address, RakNetServer server) {
        this.address = address;
        this.server = server;

        this.state = CONNECTING_1;
    }

    public void handlePacket(byte[] data) {
        if(this.state == DISCONNECTED) return;
        switch (data[0]) {
            case JRakLibPlus.ID_OPEN_CONNECTION_REQUEST_1:
                if(this.state == CONNECTING_1) {
                    OpenConnectionRequest1Packet req1 = new OpenConnectionRequest1Packet();
                    req1.decode(data);

                    if(req1.protocolVersion != JRakLibPlus.RAKNET_PROTOCOL) {
                        IncompatibleProtocolVersionPacket ipvp = new IncompatibleProtocolVersionPacket();
                        ipvp.protocolVersion = JRakLibPlus.RAKNET_PROTOCOL;
                        ipvp.serverID = this.server.getServerID();
                        this.server.addPacketToQueue(ipvp, address.toSocketAddress());
                    }
                    this.mtu = req1.nullPayloadLength;
                }
                break;
        }
    }

    public void disconnect(String reason) {
        //TODO
    }
}
