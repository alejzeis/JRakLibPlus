/*
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
package io.github.jython234.jraklibplus.protocol.raknet;

import io.github.jython234.jraklibplus.nio.Buffer;
import io.github.jython234.jraklibplus.protocol.ConnectionType;
import io.github.jython234.jraklibplus.protocol.RakNetPacket;

import static io.github.jython234.jraklibplus.JRakLibPlus.ID_UNCONNECTED_PONG_OPEN_CONNECTIONS;
import static io.github.jython234.jraklibplus.JRakLibPlus.RAKNET_MAGIC;

/**
 * ID_UNCONNECTED_PONG_OPEN_CONNECTIONS Packet implementation
 *
 * @author jython234
 */
public class UnconnectedPongOpenConnectionsPacket extends RakNetPacket {

    public long pingID;
    public long serverID;
    public String identifier;
    public ConnectionType connectionType;

    @Override
    protected void _encode(Buffer buffer) {
        buffer.putLong(pingID);
        buffer.putLong(serverID);
        buffer.put(RAKNET_MAGIC);
        buffer.putString(identifier);
        connectionType = buffer.putConnectionType();
    }

    @Override
    protected void _decode(Buffer buffer) {
        pingID = buffer.getLong();
        serverID = buffer.getLong();
        buffer.skip(16); //MAGIC
        identifier = buffer.getString();
        connectionType = buffer.getConnectionType();
    }

    @Override
    public byte getPID() {
        return ID_UNCONNECTED_PONG_OPEN_CONNECTIONS;
    }

    @Override
    public int getSize() {
        return 35 + identifier.getBytes().length;
    }
}
