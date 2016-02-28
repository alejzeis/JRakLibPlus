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

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof SystemAddress) {
            return ((SystemAddress) obj).getIpAddress().equals(ipAddress) && ((SystemAddress) obj).getPort() == port;
        }
        return obj.equals(this);
    }
}
