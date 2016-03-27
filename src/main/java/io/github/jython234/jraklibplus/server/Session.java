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
package io.github.jython234.jraklibplus.server;

import io.github.jython234.jraklibplus.JRakLibPlus;
import io.github.jython234.jraklibplus.nio.Buffer;
import io.github.jython234.jraklibplus.nio.JavaByteBuffer;
import io.github.jython234.jraklibplus.nio.NioBuffer;
import io.github.jython234.jraklibplus.protocol.RakNetPacket;
import io.github.jython234.jraklibplus.protocol.minecraft.*;
import io.github.jython234.jraklibplus.protocol.raknet.*;
import io.github.jython234.jraklibplus.util.SystemAddress;
import lombok.Getter;

import java.nio.ByteOrder;
import java.util.*;

/**
 * Represents a session opened by a client connected to the RakNetServer.
 *
 * @author jython234
 */
public class Session {
    public static final int DISCONNECTED = 0;
    public static final int CONNECTING_1 = 1;
    public static final int CONNECTING_2 = 2;
    public static final int HANDSHAKING = 3;
    public static final int CONNECTED = 4;

    public static final int MAX_SPLIT_SIZE = 128;
    public static final int MAX_SPLIT_COUNT = 4;

    @Getter private final RakNetServer server;

    @Getter private SystemAddress address;
    @Getter private int state;
    @Getter private int mtu;
    @Getter private long clientID;
    @Getter private long timeLastPacketReceived;

    private int currentSeqNum = 0;
    private int lastSeqNum = 0;
    private int sendSeqNum = 0;
    private int messageIndex = 0;
    private int splitID = 0;

    private final CustomPacket sendQueue = new CustomPackets.CustomPacket_4();
    private final Map<Integer, CustomPacket> recoveryQueue = new HashMap<>();
    private final List<Integer> ACKQueue = new ArrayList<>();
    private final List<Integer> NACKQueue = new ArrayList<>();
    private final Map<Integer, List<EncapsulatedPacket>> splitQueue = new HashMap<>();

    public Session(SystemAddress address, RakNetServer server) {
        this.address = address;
        this.server = server;

        this.state = CONNECTING_1;

        this.server.addTask(0, this::update);
    }

    private void update() {
        if(state == DISCONNECTED) return;
        if((System.currentTimeMillis() - this.timeLastPacketReceived) >= this.server.getPacketTimeout()) {
            this.disconnect("timeout");
        } else {
            synchronized (this.ACKQueue) {
                if (!this.ACKQueue.isEmpty()) {
                    ACKPacket ack = new ACKPacket();
                    ack.packets = this.ACKQueue.stream().toArray(Integer[]::new);
                    this.sendPacket(ack);
                    this.ACKQueue.clear();
                }
            }
            synchronized (this.NACKQueue) {
                if (!this.NACKQueue.isEmpty()) {
                    NACKPacket nack = new NACKPacket();
                    nack.packets = this.NACKQueue.stream().toArray(Integer[]::new);
                    this.sendPacket(nack);
                    this.NACKQueue.clear();
                }
            }

            this.sendQueuedPackets();

            //TODO:
            /*
            synchronized (this.epSendQueue) {
                if(!this.epSendQueue.isEmpty()) {
                    int pkLimit = 8;
                    CustomPacket cp = new CustomPackets.CustomPacket_4();
                    for(EncapsulatedPacket pk : this.epSendQueue) {
                        cp.packets.add(pk);
                    }
                }
            }
            */

            this.server.addTask(0, this::update);
        }
    }

    private void sendQueuedPackets() {
        synchronized (this.sendQueue) {
            if(!this.sendQueue.packets.isEmpty()) {
                this.sendQueue.sequenceNumber = this.sendSeqNum++;
                this.sendPacket(this.sendQueue);
                synchronized (this.recoveryQueue) { this.recoveryQueue.put(this.sendQueue.sequenceNumber, this.sendQueue); }

                this.sendQueue.packets.clear();
            }
        }
    }

    public void sendPacket(RakNetPacket packet) {
        this.server.addPacketToQueue(packet, this.address.toSocketAddress());
    }

    public void addPacketToQueue(EncapsulatedPacket pkt, boolean immediate) {
        switch (pkt.reliability) {
            case RELIABLE:
            case RELIABLE_ORDERED:
                //TODO: OrderIndex
            case RELIABLE_SEQUENCED:
            case RELIABLE_WITH_ACK_RECEIPT:
            case RELIABLE_ORDERED_WITH_ACK_RECEIPT:
                pkt.messageIndex = this.messageIndex++;
                break;
        }

        if(pkt.getSize() + 4 > this.mtu) { // Too big to be sent in one packet, need to be split
            byte[][] buffers = JRakLibPlus.splitByteArray(pkt.payload, this.mtu - 34);
            int splitID = (this.splitID++) % 65536;
            for(int count = 0; count < buffers.length; count++) {
                EncapsulatedPacket ep = new EncapsulatedPacket();
                ep.splitID = splitID;
                ep.split = true;
                ep.splitCount = buffers.length;
                ep.reliability = pkt.reliability;
                ep.splitIndex = count;
                ep.payload = buffers[count];

                if(count > 0) {
                    ep.messageIndex = this.messageIndex++;
                } else {
                    ep.messageIndex = pkt.messageIndex;
                }
                if(ep.reliability == Reliability.RELIABLE_ORDERED) {
                    ep.orderChannel = pkt.orderChannel;
                    ep.orderIndex = pkt.orderIndex;
                }

                this.addToQueue(ep, true);
            }
        } else {
            this.addToQueue(pkt, immediate);
        }
    }

    private void addToQueue(EncapsulatedPacket pkt, boolean immediate) {
        if(immediate) {
            CustomPacket cp = new CustomPackets.CustomPacket_0();
            cp.packets.add(pkt);
            cp.sequenceNumber = this.sendSeqNum++;
            this.sendPacket(cp);
            synchronized (this.recoveryQueue) { this.recoveryQueue.put(cp.sequenceNumber, cp); }
        } else {
            if((this.sendQueue.getSize() + pkt.getSize()) > this.mtu) {
                this.sendQueuedPackets();
            }
            synchronized (this.sendQueue) {
                this.sendQueue.packets.add(pkt);
            }
        }
    }

    public void handlePacket(byte[] data) {
        if(this.state == DISCONNECTED) return;
        this.timeLastPacketReceived = System.currentTimeMillis();

        switch (data[0]) {
            case JRakLibPlus.ID_OPEN_CONNECTION_REQUEST_1:
                if(this.state == CONNECTING_1) {
                    OpenConnectionRequest1Packet req1 = new OpenConnectionRequest1Packet();
                    req1.decode(data);

                    if(req1.protocolVersion != JRakLibPlus.RAKNET_PROTOCOL) {
                        IncompatibleProtocolVersionPacket ipvp = new IncompatibleProtocolVersionPacket();
                        ipvp.protocolVersion = JRakLibPlus.RAKNET_PROTOCOL;
                        ipvp.serverID = this.server.getServerID();
                        this.server.addPacketToQueue(ipvp, address.toSocketAddress());
                    }
                    this.mtu = req1.nullPayloadLength;

                    OpenConnectionReply1Packet reply1 = new OpenConnectionReply1Packet();
                    reply1.mtuSize = this.mtu;
                    reply1.serverID = this.server.getServerID();
                    sendPacket(reply1);

                    this.state = CONNECTING_2;
                }
                break;
            case JRakLibPlus.ID_OPEN_CONNECTION_REQUEST_2:
                if(this.state == CONNECTING_2) {
                    OpenConnectionRequest2Packet req2 = new OpenConnectionRequest2Packet();
                    req2.decode(data);

                    this.clientID = req2.clientID;
                    if(this.server.isPortChecking() && req2.serverAddress.getPort() != this.server.getBindAddress().getPort()) {
                        this.disconnect("Incorrect Port");
                        return;
                    }

                    if(req2.mtuSize != this.mtu) {
                        this.disconnect("Incorrect MTU");
                        return;
                    }

                    OpenConnectionReply2Packet reply2 = new OpenConnectionReply2Packet();
                    reply2.serverID = this.server.getServerID();
                    reply2.mtuSize = this.mtu;
                    reply2.clientAddress = this.address;
                    sendPacket(reply2);

                    this.state = HANDSHAKING;
                }
                break;
            // ACK/NACK

            case JRakLibPlus.ACK:
                if(state != CONNECTED || this.state == HANDSHAKING) break;
                ACKPacket ack = new ACKPacket();
                ack.decode(data);

                synchronized (this.recoveryQueue) {
                    for (int seq : ack.packets) {
                        if(this.recoveryQueue.containsKey(seq)) {
                            this.recoveryQueue.remove(seq);
                        }
                    }
                }

                break;
            case JRakLibPlus.NACK:
                if(state != CONNECTED || this.state == HANDSHAKING) break;
                NACKPacket nack = new NACKPacket();
                nack.decode(data);

                synchronized (this.recoveryQueue) {
                    for(int seq : nack.packets) {
                        if(this.recoveryQueue.containsKey(seq)) {
                            CustomPacket pk = this.recoveryQueue.get(seq);
                            pk.sequenceNumber = this.sendSeqNum++;
                            this.sendPacket(pk);
                            this.recoveryQueue.remove(seq);
                        }
                    }
                }
                break;
            default:
                if(this.state == CONNECTED || this.state == HANDSHAKING) {
                    //noinspection ConstantConditions
                    if(data[0] >= JRakLibPlus.CUSTOM_PACKET_0 && data[0] <= JRakLibPlus.CUSTOM_PACKET_F) {
                        this.handleDataPacket(data);
                        break;
                    }
                }
                this.server.getLogger().debug("Unknown packet received: "+String.format("%02X", data[0]));
                break;
        }
    }

    private void handleDataPacket(byte[] data) {
        CustomPacket pk = new CustomPackets.CustomPacket_0();
        pk.decode(data);

        int diff = pk.sequenceNumber - this.lastSeqNum;
        synchronized (this.NACKQueue) {
            if(!this.NACKQueue.isEmpty()) {
                this.NACKQueue.remove(pk.sequenceNumber);
                if (diff != 1) {
                    for (int i = this.lastSeqNum + 1; i < pk.sequenceNumber; i++) {
                        this.NACKQueue.add(i);
                    }
                }
            }
        }
        synchronized (this.ACKQueue) { this.ACKQueue.add(pk.sequenceNumber); }

        if(diff >= 1) {
            this.lastSeqNum = pk.sequenceNumber;
        }

        pk.packets.forEach(this::handleEncapsulatedPacket);

    }

    private void handleSplitPacket(EncapsulatedPacket pk) {
        if(pk.splitCount >= this.MAX_SPLIT_SIZE || pk.splitIndex >= this.MAX_SPLIT_SIZE || pk.splitIndex < 0) {
            return;
        }

        synchronized (this.splitQueue) {
            if (!this.splitQueue.containsKey(pk.splitID)) {
                if (this.splitQueue.size() >= this.MAX_SPLIT_COUNT) return; //Too many split packets in the queue will increase memory usage
                List<EncapsulatedPacket> l = new ArrayList<>();
                l.add(pk.splitIndex, pk);
                this.splitQueue.put(pk.splitID, l);
            } else {
                List<EncapsulatedPacket> l = this.splitQueue.get(pk.splitID);
                l.add(pk.splitIndex, pk);
                this.splitQueue.put(pk.splitID, l);
            }

            if(this.splitQueue.get(pk.splitID).size() == pk.splitCount) {
                EncapsulatedPacket ep = new EncapsulatedPacket();
                Buffer b = JavaByteBuffer.allocate(1024 * 1024, ByteOrder.BIG_ENDIAN);
                int size = 0;
                List<EncapsulatedPacket> packets = this.splitQueue.get(pk.splitID);
                for(int i = 0; i < pk.splitCount; i++) {
                    b.put(packets.get(i).payload);
                    size = size + packets.get(i).payload.length;
                }
                byte[] data = b.toByteArray();
                data = Arrays.copyOf(data, size);

                this.splitQueue.remove(pk.splitID);

                ep.payload = data;
                handleEncapsulatedPacket(ep);
            }
        }
    }


    private void handleEncapsulatedPacket(EncapsulatedPacket pk) {
        if(!(this.state == CONNECTED || this.state == HANDSHAKING)) return;
        if(pk.split && this.state == CONNECTED) {
            this.handleSplitPacket(pk);
        }

        switch (pk.payload[0]) {
            case JRakLibPlus.MC_DISCONNECT_NOTIFICATION:
                this.disconnect("client disconnected");
                break;
            case JRakLibPlus.MC_CLIENT_CONNECT:
                ClientConnectPacket ccp = new ClientConnectPacket();
                ccp.decode(pk.payload);

                ServerHandshakePacket shp = new ServerHandshakePacket();
                shp.address = this.address;
                shp.sendPing = ccp.sendPing;
                shp.sendPong = ccp.sendPing + 1000L;

                EncapsulatedPacket ep = new EncapsulatedPacket();
                ep.reliability = Reliability.UNRELIABLE;
                ep.payload = shp.encode();
                this.addToQueue(ep, true);
                break;

            case JRakLibPlus.MC_CLIENT_HANDSHAKE:
                if(this.server.isPortChecking()) {
                    ClientHandshakePacket chp = new ClientHandshakePacket();
                    chp.decode(pk.payload);
                    if(chp.address.getPort() != this.server.getBindAddress().getPort()) {
                        this.disconnect("Invalid Port");
                    }
                }
                this.state = CONNECTED;

                /*
                PingPacket ping2 = new PingPacket();
                ping2.pingID = System.currentTimeMillis();

                EncapsulatedPacket ep3 = new EncapsulatedPacket();
                ep3.reliability = Reliability.UNRELIABLE;
                ep3.payload = ping2.encode();
                this.addToQueue(ep3, true);
                this.server.getLogger().debug("Ping: "+ping2.pingID);
                */
                break;

            case JRakLibPlus.MC_PING:
                PingPacket ping = new PingPacket();
                ping.decode(pk.payload);

                PongPacket pong = new PongPacket();
                pong.pingID = ping.pingID;

                EncapsulatedPacket ep2 = new EncapsulatedPacket();
                ep2.reliability = Reliability.UNRELIABLE;
                ep2.payload = pong.encode();
                this.addToQueue(ep2, true);
                break;

            case JRakLibPlus.MC_PONG:
                PongPacket pong2 = new PongPacket();
                pong2.decode(pk.payload);

                //this.server.getLogger().debug("Pong: "+pong2.pingID);
                break;
        }
    }

    public void disconnect(String reason) {
        EncapsulatedPacket ep = new EncapsulatedPacket();
        ep.reliability = Reliability.UNRELIABLE;
        ep.payload = new DisconnectNotificationPacket().encode();
        this.addToQueue(ep, true);

        this.state = DISCONNECTED;

        this.server.onSessionClose(reason, this);
    }
}
