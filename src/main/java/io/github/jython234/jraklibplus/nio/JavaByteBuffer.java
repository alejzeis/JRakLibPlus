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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.regex.Pattern;

/**
 * A Buffer implementation that wraps around a ByteBuffer
 *
 * @author RedstoneLamp Team
 */
public class JavaByteBuffer implements Buffer {
    private ByteBuffer buffer;

    protected JavaByteBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public static JavaByteBuffer allocate(int size, ByteOrder order) {
        ByteBuffer bb = ByteBuffer.allocate(size);
        bb.order(order);
        return new JavaByteBuffer(bb);
    }

    public static JavaByteBuffer wrap(byte[] bytes, ByteOrder order) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(order);
        return new JavaByteBuffer(bb);
    }

    @Override
    public byte[] get(int len) {
        byte[] data = new byte[len];
        buffer.get(data);
        return data;
    }

    @Override
    public void put(byte[] bytes) {
        buffer.put(bytes);
    }

    @Override
    public byte getByte() {
        return buffer.get();
    }

    @Override
    public short getShort() {
        return buffer.getShort();
    }

    @Override
    public int getUnsignedShort() {
        return buffer.getShort() & 0xFFFF;
    }

    @Override
    public int getLTriad() {
        return (getByte() & 0xFF) | ((getByte() & 0xFF) << 8) | ((getByte() & 0x0F) << 16);
    }

    @Override
    public int getInt() {
        return buffer.getInt();
    }

    @Override
    public long getLong() {
        return buffer.getLong();
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
        buffer.put(b);
    }

    @Override
    public void putShort(short s) {
        buffer.putShort(s);
    }

    @Override
    public void putUnsignedShort(int us) {
        buffer.putShort((short) us);
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
        buffer.putInt(i);
    }

    @Override
    public void putLong(long l) {
        buffer.putLong(l);
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
        buffer.position(buffer.position() + len);
    }

    @Override
    public byte[] toByteArray() {
        return buffer.array();
    }

    @Override
    public int getRemainingBytes() {
        return buffer.remaining();
    }
}
