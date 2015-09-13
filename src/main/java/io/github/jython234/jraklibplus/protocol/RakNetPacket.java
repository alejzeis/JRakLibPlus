package io.github.jython234.jraklibplus.protocol;

import io.github.jython234.jraklibplus.nio.Buffer;
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
        b.putByte(getPID());
        _encode(b);
        return b.toByteArray();
    }

    /**
     * Decodes this packet from a byte array.
     * @param bytes The raw byte array of this packet to be decoded from.
     */
    public final void decode(byte[] bytes) {
        Buffer b = NioBuffer.wrapBuffer(bytes, ByteOrder.BIG_ENDIAN);
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
