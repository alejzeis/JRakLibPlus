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
        options.workerThreads = 4;
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
        server.startup();

        CustomPackets.CustomPacket_0 c = new CustomPackets.CustomPacket_0();
        c.decode(DatatypeConverter.parseHexBinary("80 E2 00 00 60 01 11 9C 00 00 9A 00 00 00 9D 00 00 00 00 00 00 00 00 C0 3B EE 02 42 81 3D 71 40 CF FB 54 43 BC 9D 7C 43 BC 9D 7C 42 00 5B 6D 00 80 60 01 11 9D 00 00 9B 00 00 00 9D 00 00 00 00 00 00 00 00 C0 40 01 16 42 81 3D 71 40 D6 8D 02 43 BC 9D 7C 43 BC 9D 7C 42 00 5B 6D 00 80".replaceAll(Pattern.quote(" "), "")));
    }

}
