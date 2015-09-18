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
package io.github.jython234.jraklibplus;

import org.slf4j.Logger;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * JRakLibPlus Constants
 *
 * @author jython234
 */
public class JRakLibPlus {
    public static final String LIBRARY_VERSION = "1.0-SNAPSHOT";

    public static final int RAKNET_PROTOCOL = 7;
    public static final byte[] RAKNET_MAGIC = new byte[] {
            0x00, (byte) 0xff, (byte) 0xff, 0x00,
            (byte) 0xfe, (byte) 0xfe, (byte) 0xfe, (byte) 0xfe,
            (byte) 0xfd, (byte) 0xfd, (byte) 0xfd, (byte) 0xfd,
            0x12, 0x34, 0x56, 0x78 };

    public static final byte ID_CONNECTED_PING_OPEN_CONNECTIONS = 0x01;
    public static final byte ID_UNCONNECTED_PING_OPEN_CONNECTIONS = 0x02;
    public static final byte ID_OPEN_CONNECTION_REQUEST_1 = 0x05;
    public static final byte ID_OPEN_CONNECTION_REPLY_1 = 0x06;
    public static final byte ID_OPEN_CONNECTION_REQUEST_2 = 0x07;
    public static final byte ID_OPEN_CONNECTION_REPLY_2 = 0x08;
    public static final byte ID_INCOMPATIBLE_PROTOCOL_VERSION = 0x1A;
    public static final byte ID_UNCONNECTED_PONG_OPEN_CONNECTIONS = 0x1C;
    public static final byte ID_ADVERTISE_SYSTEM = 0x1D;

    public static final byte CUSTOM_PACKET_0 = (byte) 0x80;
    public static final byte CUSTOM_PACKET_1 = (byte) 0x81;
    public static final byte CUSTOM_PACKET_2 = (byte) 0x82;
    public static final byte CUSTOM_PACKET_3 = (byte) 0x83;
    public static final byte CUSTOM_PACKET_4 = (byte) 0x84;
    public static final byte CUSTOM_PACKET_5 = (byte) 0x85;
    public static final byte CUSTOM_PACKET_6 = (byte) 0x86;
    public static final byte CUSTOM_PACKET_7 = (byte) 0x87;
    public static final byte CUSTOM_PACKET_8 = (byte) 0x88;
    public static final byte CUSTOM_PACKET_9 = (byte) 0x89;
    public static final byte CUSTOM_PACKET_A = (byte) 0x8A;
    public static final byte CUSTOM_PACKET_B = (byte) 0x8B;
    public static final byte CUSTOM_PACKET_C = (byte) 0x8C;
    public static final byte CUSTOM_PACKET_D = (byte) 0x8D;
    public static final byte CUSTOM_PACKET_E = (byte) 0x8E;
    public static final byte CUSTOM_PACKET_F = (byte) 0x8F;

    public static final byte ACK = (byte) 0xA0;
    public static final byte NACK = (byte) 0xC0;

    public static final byte MC_PING = 0x00;
    public static final byte MC_PONG = 0x03;

    public static final byte MC_CLIENT_CONNECT = 0x09;
    public static final byte MC_SERVER_HANDSHAKE = 0x10;
    public static final byte MC_CLIENT_HANDSHAKE = 0x13;
    public static final byte MC_DISCONNECT_NOTIFICATION = 0x15;

    public static void printExceptionToLogger(Logger logger, Exception e) {
        for(StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }
    }

    public static byte[][] splitByteArray(byte[] array, int chunkSize) {
        byte[][] splits = new byte[1024][chunkSize];
        int chunks = 0;
        for(int i=0;i<array.length;i+=chunkSize){
            if((array.length - i) > chunkSize){
                splits[chunks] = Arrays.copyOfRange(array, i, i + chunkSize);
            } else {
                splits[chunks] = Arrays.copyOfRange(array, i, array.length);
            }
            chunks++;
        }

        splits = Arrays.copyOf(splits, chunks);

        return splits;
    }

    public static String printBytesAsHex(byte[] packet) {
        StringBuffer sb = new StringBuffer();
        for(byte b : packet) {
            sb.append(String.format("%02X", b)+" ");
        }
        return sb.toString();
    }
}
