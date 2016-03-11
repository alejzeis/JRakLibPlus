package io.github.jython234.jraklibplus.server;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;

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

    @Getter private InetSocketAddress bindAddress;
    private DatagramSocket socket;

    public RakNetServer(InetSocketAddress bindAddress, ServerOptions options) {
        this.bindAddress = bindAddress;

        this.broadcastName = options.broadcastName;
        this.maxPacketsPerTick = options.maxPacketsPerTick;
        this.receiveBufferSize = options.recvBufferSize;
        this.sendBufferSize = options.sendBufferSize;
        this.portChecking = options.portChecking;
        this.disconnectInvalidProtocols = options.disconnectInvalidProtocol;

        this.logger = LoggerFactory.getLogger("JRakLibPlus Server");
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

    private void run() {
        this.logger.info("Server starting...");

        this.stopped = true;
    }

    /**
     * Options the server uses to setup
     */
    public class ServerOptions {
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
    }
}
