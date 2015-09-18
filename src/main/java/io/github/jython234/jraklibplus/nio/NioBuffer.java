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
package io.github.jython234.jraklibplus.nio;

import io.github.jython234.jraklibplus.util.SystemAddress;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.regex.Pattern;

/**
 * An implementation of a Buffer. This class uses java.nio.ByteBuffer to read and write
 * RakNet types. WARNING: Do NOT share buffer over multiple threads!
 *
 * @author jython234
 */
public class NioBuffer implements Buffer{
    private byte[] buffer;
    private ByteOrder order;
    private int position;

    protected NioBuffer(byte[] buffer, ByteOrder order) {
        this.buffer = buffer;
        this.order = order;
    }

    /**
     * Allocates and returns a new instance of a NioBuffer. This buffer will expand if the data attempting to be
     * put is greater than the size of the buffer.
     * @param initalSize The inital size of the buffer, which may expand if needed.
     * @param order The ByteOrder or endianness of the buffer.
     * @return A new allocated NioBuffer
     */
    public static NioBuffer allocateBuffer(int initalSize, ByteOrder order) {
        return new NioBuffer(new byte[initalSize], order);
    }

    /**
     * Creates a new NioBuffer wrapped around a byte array. The Buffer will be ready for reading from the specified
     * byte array.
     * @param bytes The byte array to be wrapped around of.
     * @param order The ByteOrder or endianness of the buffer.
     * @return A new NioBuffer wrapped around the specified byte array.
     */
    public static NioBuffer wrapBuffer(byte[] bytes, ByteOrder order) {
        return new NioBuffer(bytes, order);
    }

    @Override
    public byte[] get(int len) {
        if(getRemainingBytes() < len) {
            throw new BufferUnderflowException();
        }
        byte[] data = new byte[len];
        //int offset = position == 0 ? 0 : 1;
        int offset = 0;
        for(int i = 0; i < len; i++) {
            data[i] = buffer[position + offset];
            offset++;
        }
        position = position + offset;
        return data;
    }

    @Override
    public void put(byte[] bytes) {
        if((buffer.length - position) < bytes.length) {
            byte[] data = buffer;
            buffer = new byte[bytes.length+data.length];
            position = 0;
            put(data);
            put(bytes);
            return;
        }
        //int offset = position == 0 ? 0 : 1;
        int offset = 0;
        for(int i = 0; i < bytes.length; i++) {
            buffer[position + offset] = bytes[i];
            offset++;
        }
        position = position + offset;
    }

    @Override
    public byte getByte() {
        return get(1)[0];
    }

    @Override
    public short getShort() {
        return ByteBuffer.wrap(get(2)).order(order).getShort();
    }

    @Override
    public int getUnsignedShort() {
        return ByteBuffer.wrap(get(2)).order(order).getShort() & 0xFFFF;
    }

    @Override
    public int getLTriad() {
        return (getByte() & 0xFF) | ((getByte() & 0xFF) << 8) | ((getByte() & 0x0F) << 16);
    }

    @Override
    public int getInt() {
        return ByteBuffer.wrap(get(4)).order(order).getInt();
    }

    @Override
    public long getLong() {
        return ByteBuffer.wrap(get(8)).order(order).getLong();
    }

    @Override
    public String getString() {
        return new String(get(getUnsignedShort()));
    }

    @Override
    public SystemAddress getAddress() {
        int version = getByte();
        if(version == 4) {
            String address = ((~getByte()) & 0xff) +"."+ ((~getByte()) & 0xff) +"."+ ((~getByte()) & 0xff) +"."+ ((~getByte()) & 0xff);
            int port = getUnsignedShort();
            return new SystemAddress(address, port, version);
        } else if(version == 6) {
            //TODO: IPv6 Decode
            throw new UnsupportedOperationException("Can't read IPv6 address: Not Implemented");
        } else {
            throw new UnsupportedOperationException("Can't read IPv"+version+" address: unknown");
        }
    }

    @Override
    public boolean getBoolean() {
        return getByte() > 0;
    }

    @Override
    public void putByte(byte b) {
        put(new byte[] {b});
    }

    @Override
    public void putShort(short s) {
        put(ByteBuffer.allocate(2).order(order).putShort(s).array());
    }

    @Override
    public void putUnsignedShort(int us) {
        put(ByteBuffer.allocate(2).order(order).putShort((short) us).array());
    }

    @Override
    public void putLTriad(int t) {
        byte b1,b2,b3;
        b3 = (byte)(t & 0xFF);
        b2 = (byte)((t >> 8) & 0xFF);
        b1 = (byte)((t >> 16) & 0xFF);
        put(new byte[] {b3, b2, b1});
    }

    @Override
    public void putInt(int i) {
        put(ByteBuffer.allocate(4).order(order).putInt(i).array());
    }

    @Override
    public void putLong(long l) {
        put(ByteBuffer.allocate(8).order(order).putLong(l).array());
    }

    @Override
    public void putString(String s) {
        putUnsignedShort(s.getBytes().length);
        put(s.getBytes());
    }

    @Override
    public void putAddress(SystemAddress address) {
        if(address.getVersion() != 4) {
            throw new UnsupportedOperationException("Can't put IPv"+address.getVersion()+": not implemented");
        }
        putByte((byte) address.getVersion());
        for(String part : address.getIpAddress().split(Pattern.quote("."))) {
            putByte((byte) ((byte) ~(Integer.parseInt(part)) & 0xFF));
        }
        putUnsignedShort(address.getPort());
    }

    @Override
    public void putBoolean(boolean b) {
        putByte((byte) (b ? 1 : 0));
    }

    @Override
    public void skip(int len) {
        position = position + len;
    }

    @Override
    public byte[] toByteArray() {
        return buffer;
    }

    @Override
    public int getRemainingBytes() {
        return buffer.length - position;
    }
}
