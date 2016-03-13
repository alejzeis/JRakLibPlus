package io.github.jython234.jraklibplus.server;

import io.github.jython234.jraklibplus.JRakLibPlus;
import io.github.jython234.jraklibplus.protocol.RakNetPacket;
import io.github.jython234.jraklibplus.protocol.raknet.AdvertiseSystemPacket;
import io.github.jython234.jraklibplus.protocol.raknet.ConnectedPingOpenConnectionsPacket;
import io.github.jython234.jraklibplus.protocol.raknet.UnconnectedPingOpenConnectionsPacket;
import io.github.jython234.jraklibplus.protocol.raknet.UnconnectedPongOpenConnectionsPacket;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * An implementation of a RakNet game server. This implementation
 * runs on a single thread.
 *
 * @author jython234
 */
public class RakNetServer {
    @Getter private boolean running = false;
    /**
     * If true then the server has stopped and finished
     * it's last tick.
     */
    @Getter private boolean stopped = true;
    @Getter(AccessLevel.PROTECTED) private Logger logger;

    @Getter @Setter private String broadcastName;
    @Getter private int maxPacketsPerTick;
    @Getter private int receiveBufferSize;
    @Getter private int sendBufferSize;
    @Getter private boolean portChecking;
    @Getter private boolean disconnectInvalidProtocols;
    @Getter private long serverID;
    @Getter private boolean warnOnCantKeepUp;

    @Getter private InetSocketAddress bindAddress;
    private DatagramSocket socket;
    private final Queue<DatagramPacket> sendQueue = new ArrayDeque<>();

    private final List<Runnable> shutdownTasks = new ArrayList<>();
    private final Map<TaskInfo, Runnable> tasks = new HashMap<>();

    public RakNetServer(InetSocketAddress bindAddress, ServerOptions options) {
        this.bindAddress = bindAddress;

        this.broadcastName = options.broadcastName;
        this.maxPacketsPerTick = options.maxPacketsPerTick;
        this.receiveBufferSize = options.recvBufferSize;
        this.sendBufferSize = options.sendBufferSize;
        this.portChecking = options.portChecking;
        this.disconnectInvalidProtocols = options.disconnectInvalidProtocol;
        this.serverID = options.serverID;

        this.logger = LoggerFactory.getLogger("JRakLibPlus Server");

        addTask(0, this::handlePackets);
        addShutdownTask(() -> this.socket.close());
    }

    /**
     * Starts the server in the current thread. This method will block
     * as long as the server is running.
     */
    public void start() {
        this.running = true;
        this.stopped = false;
        run();
    }

    /**
     * Stops the server. This method will not block, to check if
     * the server has finished it's last tick use <code>isStopped()</code>
     */
    public void stop() {
        this.running = false;
    }

    protected void run() {
        this.logger.info("Server starting...");
        if(bind()) {
            this.logger.info("RakNetServer bound to "+bindAddress+", running on RakNet protocol "+ JRakLibPlus.RAKNET_PROTOCOL);
            try {
                while (running) {
                    long start = System.currentTimeMillis();
                    tick();
                    long elapsed = System.currentTimeMillis() - start;
                    if (elapsed >= 50) {
                        if (this.warnOnCantKeepUp)
                            this.logger.warn("Can't keep up, did the system time change or is the server overloaded? (" + elapsed + ">50)");
                    } else {
                        Thread.sleep(50 - elapsed);
                    }
                }
            } catch (Exception e) {
                this.logger.error("Fatal Exception, server has crashed! "+e.getClass().getName()+": "+e.getMessage());
                e.printStackTrace();
                stop();
            }
        }

        this.shutdownTasks.stream().forEach(Runnable::run);

        this.stopped = true;
        this.logger.info("Server has stopped.");
    }

    private void tick() {
        if(this.tasks.isEmpty()) return;
        synchronized (this.tasks) {
            List<TaskInfo> remove = new ArrayList<>();
            Map<TaskInfo, Runnable> tasks = new HashMap<>(this.tasks);
            tasks.keySet().stream().filter(ti -> (System.currentTimeMillis() - ti.registeredAt) >= ti.runIn).forEach(ti -> {
                this.tasks.get(ti).run();
                remove.add(ti);
            });
            remove.stream().forEach(this.tasks::remove);
        }
    }

    private boolean bind() {
        try {
            this.socket = new DatagramSocket(this.bindAddress);

            this.socket.setBroadcast(true);
            this.socket.setSendBufferSize(this.sendBufferSize);
            this.socket.setReceiveBufferSize(this.receiveBufferSize);
        } catch (SocketException e) {
            this.logger.error("Failed to bind "+e.getClass().getSimpleName()+": "+e.getMessage());
            stop();
            return false;
        }
        return true;
    }

    private void handlePackets() {
        try {
            this.socket.setSoTimeout(1);
            while(true) {
                DatagramPacket packet = new DatagramPacket(new byte[2048], 2048);
                try {
                    this.socket.receive(packet);
                    packet.setData(Arrays.copyOf(packet.getData(), packet.getLength()));
                    handlePacket(packet);
                } catch (SocketTimeoutException e) {
                    break;
                }
            }
        } catch (java.io.IOException e) {
            this.logger.warn("java.io.IOException while receiving packets: "+e.getMessage());
        }

        while(!this.sendQueue.isEmpty()) {
            DatagramPacket pkt = this.sendQueue.remove();
            try {
                this.socket.send(pkt);
            } catch (IOException e) {
                this.logger.warn("java.io.IOException while sending packet: "+e.getMessage());
            }
        }
        addTask(0, this::handlePackets); //Run next tick
    }

    private void handlePacket(DatagramPacket packet) {
        switch (packet.getData()[0]) { //Check for pings
            case JRakLibPlus.ID_UNCONNECTED_PING_OPEN_CONNECTIONS:
                UnconnectedPingOpenConnectionsPacket upocp = new UnconnectedPingOpenConnectionsPacket();
                upocp.decode(packet.getData());

                AdvertiseSystemPacket pong = new AdvertiseSystemPacket();
                pong.serverID = this.serverID;
                pong.pingID = upocp.pingID;
                pong.identifier = this.broadcastName;
                addPacketToQueue(pong, packet.getSocketAddress());
                break;
            case JRakLibPlus.ID_CONNECTED_PING_OPEN_CONNECTIONS:
                ConnectedPingOpenConnectionsPacket ping = new ConnectedPingOpenConnectionsPacket();
                ping.decode(packet.getData());

                UnconnectedPongOpenConnectionsPacket pong2 = new UnconnectedPongOpenConnectionsPacket();
                pong2.serverID = this.serverID;
                pong2.pingID = ping.pingID;
                pong2.identifier = this.broadcastName;
                addPacketToQueue(pong2, packet.getSocketAddress());
                break;
            default:

                break;
        }
    }

    public void addPacketToQueue(RakNetPacket packet, SocketAddress address) {
        synchronized (this.sendQueue) {
            byte[] buffer = packet.encode();
            this.sendQueue.add(new DatagramPacket(buffer, buffer.length, address));
        }
    }

    /**
     * Adds a task to be ran in <code>runIn</code> milliseconds.
     * @param runIn The amount of milliseconds from the current time
     *              to run the task.
     * @param r The task to be ran.
     */
    public void addTask(long runIn, Runnable r) {
        synchronized (this.tasks) {
            TaskInfo ti = new TaskInfo();
            ti.runIn = runIn;
            ti.registeredAt = System.currentTimeMillis();
            this.tasks.put(ti, r);
        }
    }

    /**
     * Adds a task to be ran when the server shuts down.
     * @param r The task to be ran.
     */
    public void addShutdownTask(Runnable r) {
        synchronized (shutdownTasks) {
            shutdownTasks.add(r);
        }
    }

    /**
     * Options the server uses to setup
     */
    public static class ServerOptions {
        public String broadcastName = "A JRakLibPlus Server.";
        /**
         * The maximum amount of packets to read and process per tick (20 ticks per second)
         */
        public int maxPacketsPerTick = 500;
        public int recvBufferSize = 4096;
        public int sendBufferSize = 4096;
        public boolean portChecking = true;
        /**
         * If this is true then the server will disconnect clients with invalid raknet protocols.
         * The server currently supports protocol 7
         */
        public boolean disconnectInvalidProtocol = true;
        /**
         * If to log warning messages when a tick takes longer than 50 milliseconds.
         */
        public boolean warnOnCantKeepUp = true;
        /**
         * The server's unique 64 bit identifier. This is usually generated
         * randomly at start.
         */
        public long serverID = new Random().nextLong();
    }

    private class TaskInfo {
        public long registeredAt;
        public long runIn;
    }
}
