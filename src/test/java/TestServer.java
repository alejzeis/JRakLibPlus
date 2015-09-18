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
import io.github.jython234.jraklibplus.protocol.raknet.EncapsulatedPacket;
import io.github.jython234.jraklibplus.server.NioSession;
import io.github.jython234.jraklibplus.server.RakNetServer;
import io.github.jython234.jraklibplus.server.ServerInterface;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.SimpleLogger;

import java.net.InetSocketAddress;

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
        server.startup();
    }

}
