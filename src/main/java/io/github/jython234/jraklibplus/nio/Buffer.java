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

/**
 * Represents a Buffer that can hold bytes. The Buffer can read and write all
 * RakNet data types.
 *
 * @author RedstoneLamp Team
 */
public interface Buffer {
    /**
     * Gets <code>len</code> amount of bytes from the buffer.
     * @param len The amount of bytes to get.
     * @return A byte array containing the bytes read from the buffer.
     * @throws java.nio.BufferUnderflowException If there are more bytes attempting to be read than available.
     */
    byte[] get(int len);

    /**
     * Puts <code>bytes</code> into the buffer. If there is not enough space in the buffer, the buffer
     * will expand to accommodate the extra bytes.
     * @param bytes The bytes to be put into the buffer.
     */
    void put(byte[] bytes);

    byte getByte();

    short getShort();

    int getUnsignedShort();

    int getLTriad();

    int getInt();

    long getLong();

    String getString();

    SystemAddress getAddress();

    boolean getBoolean();

    void putByte(byte b);

    void putShort(short s);

    void putUnsignedShort(int us);

    void putLTriad(int t);

    void putInt(int i);

    void putLong(long l);

    void putString(String s);

    void putAddress(SystemAddress address);

    void putBoolean(boolean b);

    /**
     * Skips <code>len</code> amount of bytes in the buffer. This increments the position by <code>len</code>
     * @param len The amount of bytes to skip in the buffer.
     */
    void skip(int len);

    byte[] toByteArray();

    int getRemainingBytes();
}
