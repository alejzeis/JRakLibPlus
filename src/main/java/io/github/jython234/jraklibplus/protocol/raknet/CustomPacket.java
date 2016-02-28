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

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Packet Encode/Decode implementation.
 *
 * @author RedstoneLamp Team
 */
public abstract class CustomPacket extends RakNetPacket {

    public int sequenceNumber;
    public List<EncapsulatedPacket> packets = new ArrayList<>();

    @Override
    protected void _encode(Buffer buffer) {
        buffer.putLTriad(sequenceNumber);
        packets.stream().forEach(packet -> packet._encode(buffer));
    }

    @Override
    public void _decode(Buffer buffer) {
        sequenceNumber = buffer.getLTriad();
        while (buffer.getRemainingBytes() >= 4) { //4 is the smallest amount of bytes an EncapsulatedPacket can be
            EncapsulatedPacket packet = new EncapsulatedPacket();
            packet._decode(buffer);
            packets.add(packet);
        }
    }

    @Override
    public int getSize() {
        int len = 4;
        for(EncapsulatedPacket packet : packets) {
            len = len + packet.getSize();
        }
        return len;
    }
}
