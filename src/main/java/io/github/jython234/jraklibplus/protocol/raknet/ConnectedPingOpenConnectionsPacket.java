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
package io.github.jython234.jraklibplus.protocol.raknet;

import io.github.jython234.jraklibplus.protocol.RakNetPacket;
import io.github.jython234.jraklibplus.nio.Buffer;
import static io.github.jython234.jraklibplus.JRakLibPlus.*;

/**
 * ID_CONNECTED_PING_OPEN_CONNECTIONS Packet implementation
 *
 * @author RedstoneLamp Team
 */
public class ConnectedPingOpenConnectionsPacket extends RakNetPacket {

    public long pingID;

    @Override
    protected void _encode(Buffer buffer) {
        buffer.putLong(pingID);
        buffer.put(RAKNET_MAGIC);
    }

    @Override
    protected void _decode(Buffer buffer) {
        pingID = buffer.getLong();
        //MAGIC
    }

    @Override
    public byte getPID() {
        return ID_CONNECTED_PING_OPEN_CONNECTIONS;
    }

    @Override
    public int getSize() {
        return 25;
    }
}
