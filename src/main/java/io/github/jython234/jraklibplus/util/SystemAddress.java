package io.github.jython234.jraklibplus.util;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Represents an address of a client or server. This class holds the IP address
 * and port.
 *
 * @author RedstoneLamp Team
 */
public class SystemAddress {
    private final String ipAddress;
    private final int port;
    private final int version;

    public SystemAddress(String ipAddress, int port, int version) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.version = version;
    }

    public static SystemAddress fromSocketAddress(SocketAddress address) {
        if(address instanceof InetSocketAddress) {
            return new SystemAddress(((InetSocketAddress) address).getHostString(), ((InetSocketAddress) address).getPort(), 4);
        }
        return null;
    }

    public int getPort() {
        return port;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return ipAddress+":"+port;
    }
}
