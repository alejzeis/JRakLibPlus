/*
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

import lombok.Getter;

/**
 * Used to signify which implementation of the RakNet protocol is being used by
 * a connection. If you would like your implementation to be listed here, create
 * an issue on the JRakNet repository with the tag "Connection type support".
 * Keep in mind that this functionality has <i>no</i> guarantees to function
 * completely, as it is completely dependent on the implementation to implement
 * this feature.
 * 
 * @author Trent "MarfGamer" Summerlin
 * @author jython234
 * @see <a href=
 *      "https://github.com/JRakNet/JRakNet/issues/new">https://github.com/JRakNet/JRakNet/issues/new</a>
 */
public enum ConnectionType {
	
	VANILLA("Vanilla", null, 0x00), JRAKNET("JRakNet", "Java", 0x01), RAKLIB("RakLib", "PHP", 0x02),
	JRAKLIB_PLUS("JRakLib+", "Java", 0x03);

	// Connection type header magic
	public static final byte[] MAGIC = new byte[] { (byte) 0x03, (byte) 0x08, (byte) 0x05, (byte) 0x0B, 0x43,
			(byte) 0x54, (byte) 0x49 };

	@Getter private final String name;
	@Getter private final String language;
	@Getter private final short id;

	private ConnectionType(String name, String language, int id) {
		this.name = name;
		this.language = language;
		this.id = (short) id;
		if (id < 0 || id > 255) {
			throw new IllegalArgumentException("Invalid ID, must be in between 0-255");
		}
	}

	public static ConnectionType getType(int id) {
		for (ConnectionType type : ConnectionType.values()) {
			if (type.id == id) {
				return type;
			}
		}
		return ConnectionType.VANILLA;
	}

}
