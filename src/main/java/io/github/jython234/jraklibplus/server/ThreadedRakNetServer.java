package io.github.jython234.jraklibplus.server;

import java.net.InetSocketAddress;

/**
 * A RakNetServer that runs in a separate thread.
 *
 * @author jython234
 */
public class ThreadedRakNetServer extends RakNetServer {
    private Thread thread;

    public ThreadedRakNetServer(InetSocketAddress bindAddress, ServerOptions options) {
        super(bindAddress, options);
        this.thread = new Thread(super::run, "RakNetServer");
    }

    /**
     * Starts the server in a new Thread.
     */
    @Override
    public void start() {
        super.start();
    }

    @Override
    protected void run() {
        this.thread.start();
    }
}
