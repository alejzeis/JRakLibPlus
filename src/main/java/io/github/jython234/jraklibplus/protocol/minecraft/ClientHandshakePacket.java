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
package io.github.jython234.jraklibplus.protocol.minecraft;

import io.github.jython234.jraklibplus.nio.Buffer;
import io.github.jython234.jraklibplus.protocol.RakNetPacket;
import io.github.jython234.jraklibplus.util.SystemAddress;

import static io.github.jython234.jraklibplus.JRakLibPlus.*;

/**
 * MC_CLIENT_HANDSHAKE Packet implementation.
 *
 * @author RedstoneLamp Team
 */
public class ClientHandshakePacket extends RakNetPacket {

    public SystemAddress address;
    public SystemAddress[] systemAddresses;
    public long sendPing;
    public long sendPong;

    @Override
    protected void _encode(Buffer buffer) {
        buffer.putAddress(address);
        for(SystemAddress a : systemAddresses) {
            buffer.putAddress(a);
        }
        buffer.putLong(sendPing);
        buffer.putLong(sendPong);
    }

    @Override
    protected void _decode(Buffer buffer) {
        address = buffer.getAddress();
        systemAddresses = new SystemAddress[10];
        for(int i = 0; i < 10; i++) {
            systemAddresses[i] = buffer.getAddress();
        }
        sendPing = buffer.getLong();
        sendPong = buffer.getLong();
    }

    @Override
    public byte getPID() {
        return MC_CLIENT_HANDSHAKE;
    }

    @Override
    public int getSize() {
        return 94;
    }
}
