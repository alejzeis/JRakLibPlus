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
import io.github.jython234.jraklibplus.util.SystemAddress;

import java.net.SocketAddress;
import java.nio.BufferUnderflowException;

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
        try {
            server.addToQueue(RakNetServer.getAndDecodePacket(packet, session), session);
        } catch(BufferUnderflowException e) {
            server.getLogger().warn("("+address+") Decode Failed! "+e.getClass().getName()+": "+e.getMessage());
            server.getLogger().warn("BufferUnderFlow: "+ JRakLibPlus.printBytesAsHex(packet));
        }
    }
}
