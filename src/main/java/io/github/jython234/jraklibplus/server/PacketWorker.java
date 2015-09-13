package io.github.jython234.jraklibplus.server;

import io.github.jython234.jraklibplus.util.SystemAddress;

import java.net.SocketAddress;

/**
 * Created by jython234 on 9/12/2015.
 *
 * @author RedstoneLamp Team
 */
public class PacketWorker implements Runnable {
    private final RakNetServer server;
    private final SocketAddress address;
    private final byte[] packet;

    public PacketWorker(RakNetServer server, SocketAddress address, byte[] packet) {
        this.server = server;
        this.address = address;
        this.packet = packet;
    }

    @Override
    public void run() {
        server.getLogger().debug("("+address+") PACKET IN: "+packet);
        NioSession session = server.getSession(SystemAddress.fromSocketAddress(address));
        if(session == null) {
            session = server.openSession(address);
        }
        server.addToQueue(RakNetServer.getAndDecodePacket(packet), session);
    }
}
