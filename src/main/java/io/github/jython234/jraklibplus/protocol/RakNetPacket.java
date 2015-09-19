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
package io.github.jython234.jraklibplus.protocol;

import io.github.jython234.jraklibplus.nio.Buffer;
import io.github.jython234.jraklibplus.nio.JavaByteBuffer;
import io.github.jython234.jraklibplus.nio.NioBuffer;

import java.nio.ByteOrder;

/**
 * Represents a Packet of data. The data can be encoded/decoded from their class implementations.
 * Default ByteOrder is BIG_ENDIAN
 *
 * @author RedstoneLamp Team
 */
public abstract class RakNetPacket {

    /**
     * The time this packet was last sent at. This is used internally by JRakLibPlus, and
     * sometimes may be null.
     */
    public long sendTime;

    /**
     * Encodes this packet into a byte array.
     * @return The encoded bytes of this packet.
     */
    public final byte[] encode() {
        Buffer b = NioBuffer.allocateBuffer(getSize(), ByteOrder.BIG_ENDIAN);
        //Buffer b = JavaByteBuffer.allocate(getSize(), ByteOrder.BIG_ENDIAN);
        b.putByte(getPID());
        _encode(b);
        return b.toByteArray();
    }

    /**
     * Decodes this packet from a byte array.
     * @param bytes The raw byte array of this packet to be decoded from.
     */
    public final void decode(byte[] bytes) {
        //Buffer b = NioBuffer.wrapBuffer(bytes, ByteOrder.BIG_ENDIAN);
        Buffer b = JavaByteBuffer.wrap(bytes, ByteOrder.BIG_ENDIAN);
        b.getByte();
        _decode(b);
    }

    protected abstract void _encode(Buffer buffer);
    protected abstract void _decode(Buffer buffer);

    /**
     * Get this packet's PacketID. The PacketID is always the first byte of the packet.
     * @return This packet's PacketID.
     */
    public abstract byte getPID();

    /**
     * Get the correct size for this packet (in bytes). Subclasses may override this.
     * @return The size for the packet (in bytes). The default is zero
     */
    public int getSize() {
        return 0;
    }
}
