/**
 * JRakLibPlus is not affiliated with Jenkins Software LLC or RakNet.
 * This software is an enhanced port of RakLib https://github.com/PocketMine/RakLib.

 * This file is part of JRakLibPlus.
 *
 * JRakLibPlus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JRakLibPlus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JRakLibPlus.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.jython234.jraklibplus.server;


import io.github.jython234.jraklibplus.JRakLibPlus;
import io.github.jython234.jraklibplus.nio.Buffer;
import io.github.jython234.jraklibplus.nio.JavaByteBuffer;
import io.github.jython234.jraklibplus.protocol.RakNetPacket;
import io.github.jython234.jraklibplus.protocol.minecraft.*;
import io.github.jython234.jraklibplus.protocol.raknet.*;

import static io.github.jython234.jraklibplus.JRakLibPlus.*;

import io.github.jython234.jraklibplus.util.SystemAddress;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.*;

/**
 * An implementation of a Minecraft: Pocket Edition RakNet server
 *
 * @author RedstoneLamp Team
 */
public class RakNetServer extends Thread {

    public final long serverID;
    private final ServerInterface server;

    private boolean running = false;

    protected DatagramChannel channel;
    protected ExecutorService workers;
    private ServerOptions options;
    private Logger logger;

    private long lastTick;

    private static Map<Byte, Class<? extends RakNetPacket>> packets = new ConcurrentHashMap<>();
    private Queue<UnknownPacket> packetQueue = new ConcurrentLinkedQueue<>();
    private Map<String, NioSession> sessions = new ConcurrentHashMap<>();

    /**
     * Creates a new RakNet server and binds to the specified address.
     * @param logger The Logger this server will use.
     * @param bindAddress The address the server will bind to.
     * @param options The options for this server.
     * @param impl The ServerInterface to communicate with the implementation.
     */
    public RakNetServer(Logger logger, SocketAddress bindAddress, ServerOptions options, ServerInterface impl) {
        serverID = new Random().nextLong();
        server = impl;
        try {
            channel = DatagramChannel.open();
            channel.socket().bind(bindAddress);
            channel.configureBlocking(false);
            workers = Executors.newFixedThreadPool(options.workerThreads, new NioThreadFactory());
            logger.debug("RakNetServer started on "+bindAddress+" with "+options.workerThreads+" workers.");
            this.logger = logger;
            this.options = options;
        } catch (SocketException e) {
            logger.error("*** FAILED TO BIND TO "+bindAddress+"! Perhaps another server is running on that port?");
            JRakLibPlus.printExceptionToLogger(logger, e);
        } catch (IOException e) {
            logger.error("*** FAILED TO OPEN CHANNEL! "+e.getClass().getName()+": "+e.getMessage());
            JRakLibPlus.printExceptionToLogger(logger, e);
        }
    }

    public void startup() {
        if(!running) {
            running = true;
            start();
        }
    }

    @Override
    public void run() {
        setName("ServerCommandThread");
        while (running) {
            long start = System.currentTimeMillis();
            try {
                tick();
            } catch (IOException e) {
                logger.warn("Exception in tick: "+e.getClass().getName()+": "+e.getMessage());
                JRakLibPlus.printExceptionToLogger(logger, e);
            }
            long elapsed = System.currentTimeMillis() - start;
            if(elapsed < 50) {
                try {
                    sleep(50 - elapsed);
                } catch (InterruptedException e) {
                    logger.error("Interrupted while sleeping in tick: "+e.getMessage());
                    JRakLibPlus.printExceptionToLogger(logger, e);
                }
            }
            lastTick = System.currentTimeMillis();
        }
    }

    private void tick() throws IOException {
        readPackets();
        int max = 1000;
        while(max-- > 0) {
            if(packetQueue.isEmpty()) break;
            UnknownPacket pkt = packetQueue.remove();
            pkt.session.handlePacket(pkt.packet);
        }
        sessions.values().forEach(session -> session.update(System.currentTimeMillis()));
    }

    private void readPackets() throws IOException {
        int max = options.maxPacketsPerTick;
        while(max > 0) {
            ByteBuffer bb = ByteBuffer.allocate(options.recvBufferSize);
            SocketAddress address = channel.receive(bb);
            if(address != null) {
                workers.execute(new PacketWorker(this, address, Arrays.copyOf(bb.array(), bb.position())));
                max--;
            } else {
                break;
            }
        }
    }

    public void shutdown() throws InterruptedException {
        if(running) {
            running = false;
            join();
        }
    }

    protected void addToQueue(RakNetPacket packet, NioSession session) {
        UnknownPacket pkt = new UnknownPacket();
        pkt.packet = packet;
        pkt.session = session;
        packetQueue.add(pkt);
    }

    protected NioSession openSession(SocketAddress address) {
        NioSession session = new NioSession(this, SystemAddress.fromSocketAddress(address));
        sessions.put(session.getAddress().toString(), session);
        return session;
    }

    protected void closeSession(final NioSession session, final String reason) {
        sessions.remove(session.getAddress().toString());
        workers.execute(() -> server.sessionClosed(session, reason));
    }

    protected void signalSessionConnected(final NioSession session) {
        workers.execute(() -> server.sessionOpened(session));
    }

    protected void signalEncapsulatedPacket(final NioSession session, final EncapsulatedPacket packet) {
        workers.execute(() -> server.handleEncapsulatedPacket(packet, session));
    }

    public Logger getLogger() {
        return logger;
    }

    public NioSession getSession(SystemAddress address) {
        if(sessions.containsKey(address.toString())) {
            return sessions.get(address.toString());
        }
        return null;
    }

    /**
     * Gets the name of this server that will show up in the server list.
     * @return The name of this server.
     */
    public String getBroadcastName() {
        return options.name;
    }

    public int getPort() {
        return channel.socket().getLocalPort();
    }

    public ServerOptions getOptions() {
        return options;
    }

    public void sendPacket(RakNetPacket packet, SystemAddress address) {
        try {
            channel.send(ByteBuffer.wrap(packet.encode()), new InetSocketAddress(address.getIpAddress(), address.getPort()));
        } catch (IOException e) {
            logger.error(e.getClass().getName()+" while sending packet "+packet+" to "+address.getIpAddress()+":"+address.getPort()+" "+e.getMessage());
            JRakLibPlus.printExceptionToLogger(logger, e);
        }
    }

    /**
     * These options contain specific values that the server uses to function.
     * Tweaking these may improve performance.
     *
     * @author jython234
     */
    public static class ServerOptions {
        public int workerThreads = 4;
        /**
         * The maximum amount of packets to read per tick (20 ticks per second)
         */
        public int maxPacketsPerTick = 5000;
        public int recvBufferSize = 4096;
        public int sendBufferSize = 4096;
        public String name = "MCPE;A JRakLibPlus server;45;0.14.0;-1;0";
        public boolean portChecking = true;
        /**
         * If this is true then the server will disconnect clients with invalid raknet protocols.
         * The server currently supports protocol 7
         */
        public boolean disconnectInvalidProtocol = true;
    }

    protected static class UnknownPacket {
        public RakNetPacket packet;
        public NioSession session;
    }

    public static RakNetPacket getAndDecodePacket(byte[] buffer) {
        if(packets.containsKey(buffer[0])) {
            try {
                RakNetPacket packet = packets.get(buffer[0]).newInstance();
                packet.decode(buffer);
                return packet;
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static RakNetPacket getAndDecodePacket(byte[] buffer, NioSession session) {
        if(packets.containsKey(buffer[0])) {
            try {
                RakNetPacket packet = packets.get(buffer[0]).newInstance();
                packet.decode(buffer);
                return packet;
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private static void registerPackets() {
        //RakNet
        packets.put(ID_CONNECTED_PING_OPEN_CONNECTIONS, ConnectedPingOpenConnectionsPacket.class);
        packets.put(ID_UNCONNECTED_PING_OPEN_CONNECTIONS, UnconnectedPingOpenConnectionsPacket.class);
        packets.put(ID_OPEN_CONNECTION_REQUEST_1, OpenConnectionRequest1Packet.class);
        packets.put(ID_OPEN_CONNECTION_REQUEST_2, OpenConnectionRequest2Packet.class);
        packets.put(ID_OPEN_CONNECTION_REPLY_1, OpenConnectionReply1Packet.class);
        packets.put(ID_OPEN_CONNECTION_REPLY_2, OpenConnectionReply2Packet.class);
        packets.put(ID_UNCONNECTED_PONG_OPEN_CONNECTIONS, UnconnectedPongOpenConnectionsPacket.class);
        packets.put(ID_ADVERTISE_SYSTEM, AdvertiseSystemPacket.class);
        packets.put(ID_INCOMPATIBLE_PROTOCOL_VERSION, IncompatibleProtocolVersionPacket.class);
        packets.put(CUSTOM_PACKET_0, CustomPackets.CustomPacket_0.class);
        packets.put(CUSTOM_PACKET_1, CustomPackets.CustomPacket_1.class);
        packets.put(CUSTOM_PACKET_2, CustomPackets.CustomPacket_2.class);
        packets.put(CUSTOM_PACKET_3, CustomPackets.CustomPacket_3.class);
        packets.put(CUSTOM_PACKET_4, CustomPackets.CustomPacket_4.class);
        packets.put(CUSTOM_PACKET_5, CustomPackets.CustomPacket_5.class);
        packets.put(CUSTOM_PACKET_6, CustomPackets.CustomPacket_6.class);
        packets.put(CUSTOM_PACKET_7, CustomPackets.CustomPacket_7.class);
        packets.put(CUSTOM_PACKET_8, CustomPackets.CustomPacket_8.class);
        packets.put(CUSTOM_PACKET_9, CustomPackets.CustomPacket_9.class);
        packets.put(CUSTOM_PACKET_A, CustomPackets.CustomPacket_A.class);
        packets.put(CUSTOM_PACKET_B, CustomPackets.CustomPacket_B.class);
        packets.put(CUSTOM_PACKET_C, CustomPackets.CustomPacket_C.class);
        packets.put(CUSTOM_PACKET_D, CustomPackets.CustomPacket_D.class);
        packets.put(CUSTOM_PACKET_E, CustomPackets.CustomPacket_E.class);
        packets.put(CUSTOM_PACKET_F, CustomPackets.CustomPacket_F.class);
        packets.put(ACK, ACKPacket.class);
        packets.put(NACK, NACKPacket.class);
        //Minecraft
        packets.put(MC_CLIENT_CONNECT, ClientConnectPacket.class);
        packets.put(MC_SERVER_HANDSHAKE, ServerHandshakePacket.class);
        packets.put(MC_CLIENT_HANDSHAKE, ClientHandshakePacket.class);
        packets.put(MC_PING, PingPacket.class);
        packets.put(MC_PONG, PongPacket.class);
    }

    static {
        registerPackets();
    }
}
