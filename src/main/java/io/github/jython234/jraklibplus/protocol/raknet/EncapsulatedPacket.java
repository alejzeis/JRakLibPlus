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

/**
 * Implementation of an EncapsulatedPacket contained in a CustomPacket.
 *
 * @author RedstoneLamp Team
 */
public class EncapsulatedPacket extends RakNetPacket {

    //Header
    public Reliability reliability;
    public boolean split = false;
    //If RELIABLE, RELIABLE_SEQUENCED, RELIABLE_ORDERED
    public int messageIndex = -1;
    //If UNRELIABLE_SEQUENCED, RELIABLE_SEQUENCED, RELIABLE_ORDERED
    public int orderIndex = -1;
    public byte orderChannel = -1;
    //If split
    public int splitCount = -1;
    /**
     * uint16 (unsigned short)
     */
    public int splitID = -1;
    public int splitIndex = -1;
    //Payload buffer
    public byte[] payload = new byte[0];

    @Override
    protected void _encode(Buffer buffer) {
        buffer.putByte((byte) ((reliability.asByte() << 5) | (split ? 0b00010000 : 0)));
        buffer.putUnsignedShort(payload.length * 8); //Bytes to Bits

        if(reliability ==  Reliability.RELIABLE || reliability ==  Reliability.RELIABLE_SEQUENCED || reliability ==  Reliability.RELIABLE_ORDERED) {
            buffer.putLTriad(messageIndex);
        }

        if(reliability == Reliability.UNRELIABLE_SEQUENCED || reliability == Reliability.RELIABLE_SEQUENCED || reliability == Reliability.RELIABLE_ORDERED) {
            buffer.putLTriad(orderIndex);
            buffer.putByte(orderChannel);
        }

        if(split) {
            buffer.putInt(splitCount);
            buffer.putUnsignedShort(splitID);
            buffer.putInt(splitIndex);
        }

        buffer.put(payload);
    }

    @Override
    protected void _decode(Buffer buffer) {
        //Header
        byte flags = buffer.getByte();
        reliability = Reliability.lookup((byte) ((flags & 0b11100000) >> 5));
        split = (flags & 0b00010000) > 0;
        int length = (int) Math.ceil(buffer.getUnsignedShort() / 8.0); //Bits to Bytes

        if(reliability ==  Reliability.RELIABLE || reliability ==  Reliability.RELIABLE_SEQUENCED || reliability ==  Reliability.RELIABLE_ORDERED) {
            messageIndex = buffer.getLTriad();
        }

        if(reliability == Reliability.UNRELIABLE_SEQUENCED || reliability == Reliability.RELIABLE_SEQUENCED || reliability == Reliability.RELIABLE_ORDERED) {
            orderIndex = buffer.getLTriad();
            orderChannel = buffer.getByte();
        }

        if(split) {
            splitCount = buffer.getInt();
            splitID = buffer.getUnsignedShort();
            splitIndex = buffer.getInt();
        }

        payload = buffer.get(length);
    }

    @Override
    public byte getPID() {
        return -1;
    }

    @Override
    public int getSize() {
        return 3 + payload.length + (messageIndex != -1 ? 3 : 0) + (orderIndex != -1 ? 4 : 0) + (split ? 10 : 0);
    }
}
