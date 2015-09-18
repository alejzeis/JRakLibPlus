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

/**
 * Represents a level of Reliability on the RakNet network.
 * <br>
 * Information was found at: http://www.jenkinssoftware.com/raknet/manual/reliabilitytypes.html
 *
 * @author jython234
 */
public enum Reliability {
    UNRELIABLE((byte) 0),
    UNRELIABLE_SEQUENCED((byte) 1),
    RELIABLE((byte) 2),
    RELIABLE_ORDERED((byte) 3),
    RELIABLE_SEQUENCED((byte) 4),
    UNRELIABLE_WITH_ACK_RECEIPT((byte) 5),
    RELIABLE_WITH_ACK_RECEIPT((byte) 6),
    RELIABLE_ORDERED_WITH_ACK_RECEIPT((byte) 7);

    private byte reliability;

    Reliability(byte reliability) {
        this.reliability = reliability;
    }

    public static Reliability lookup(byte reliability) {
        switch (reliability) {
            case 0:
                return UNRELIABLE;
            case 1:
                return UNRELIABLE_SEQUENCED;
            case 2:
                return RELIABLE;
            case 3:
                return RELIABLE_ORDERED;
            case 4:
                return RELIABLE_SEQUENCED;
            case 5:
                return UNRELIABLE_WITH_ACK_RECEIPT;
            case 6:
                return RELIABLE_WITH_ACK_RECEIPT;
            case 7:
                return RELIABLE_ORDERED_WITH_ACK_RECEIPT;
            default:
                return null;
        }
    }

    public byte asByte() {
        return reliability;
    }
}
