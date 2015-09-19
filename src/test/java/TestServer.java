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
import io.github.jython234.jraklibplus.protocol.raknet.CustomPackets;
import io.github.jython234.jraklibplus.protocol.raknet.EncapsulatedPacket;
import io.github.jython234.jraklibplus.server.NioSession;
import io.github.jython234.jraklibplus.server.RakNetServer;
import io.github.jython234.jraklibplus.server.ServerInterface;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.SimpleLogger;

import javax.xml.bind.DatatypeConverter;
import java.net.InetSocketAddress;
import java.util.regex.Pattern;

/**
 * Created by jython234 on 9/12/2015.
 *
 * @author RedstoneLamp Team
 */
public class TestServer {

    public static void main(String[] args) {
        RakNetServer.ServerOptions options = new RakNetServer.ServerOptions();
        options.workerThreads = 1;
        RakNetServer server = new RakNetServer(LoggerFactory.getLogger("RakNetServer-Test"), new InetSocketAddress("0.0.0.0", 19132), options, new ServerInterface() {
            @Override
            public void handleEncapsulatedPacket(EncapsulatedPacket packet, NioSession session) {
                System.out.println("EncapsulatedPacket: "+packet+", session: "+session);
            }

            @Override
            public void sessionOpened(NioSession session) {
                System.out.println("Session opened "+session);
            }

            @Override
            public void sessionClosed(NioSession session, String reason) {
                System.out.println("Session closed "+session+", reason: "+reason);
            }
        });
        //server.startup();

        CustomPackets.CustomPacket_0 c = new CustomPackets.CustomPacket_0();
        c.decode(DatatypeConverter.parseHexBinary("80 3E 00 00 60 01 11 22 00 00 21 00 00 00 9D 00 00 00 00 00 00 00 00 00 00 00 00 42 B4 50 94 40 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 60 01 11 23 00 00 22 00 00 00 9D 00 00 00 00 00 00 00 00 00 00 00 00 42 B0 57 28 40 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 60 01 11 24 00 00 23 00 00 00 9D 00 00 00 00 00 00 00 00 00 00 00 00 42 AC 49 F2 40 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 60 01 11 25 00 00 24 00 00 00 9D 00 00 00 00 00 00 00 00 00 00 00 00 42 A8 29 56 40 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 60 01 11 26 00 00 25 00 00 00 9D 00 00 00 00 00 00 00 00 00 00 00 00 42 A3 F5 B8 40 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 60 01 11 27 00 00 26 00 00 00 9D 00 00 00 00 00 00 00 00 00 00 00 00 42 9F AF 79 40 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 60 01 11 28 00 00 27 00 00 00 9D 00 00 00 00 00 00 00 00 00 00 00 00 42 9B 56 F9 40 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 60 01 11 29 00 00 28 00 00 00 9D 00 00 00 00 00 00 00 00 00 00 00 00 42 96 EC 95 40 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 60 01 11 2A 00 00 29 00 00 00 9D 00 00 00 00 00 00 00 00 00 00 00 00 42 92 70 A8 40 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 60 01 11 2C 00 00 2B 00 00 00 9D 00 00 00 00 00 00 00 00 00 00 00 00 42 8B 3D 71 40 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 80 60 01 11 2B 00 00 2A 00 00 00 9D 00 00 00 00 00 00 00 00 00 00 00 00 42 8D E3 8C 40 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00".replaceAll(Pattern.quote(" "), "")));
    }

}
