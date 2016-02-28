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
import io.github.jython234.jraklibplus.protocol.RakNetPacket;
import io.github.jython234.jraklibplus.protocol.minecraft.*;
import io.github.jython234.jraklibplus.protocol.raknet.*;
import io.github.jython234.jraklibplus.util.SystemAddress;
import static io.github.jython234.jraklibplus.JRakLibPlus.*;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a networking session.
 */
public class NioSession {
    public final static int STATE_UNCONNECTED = 0;
    public final static int STATE_CONNECTING_1 = 1;
    public final static int STATE_CONNECTING_2 = 2;
    public final static int STATE_CONNECTED = 3;

    public static int WINDOW_SIZE = 2048;

    private int messageIndex = 0;
    private Map<Byte, Integer> channelIndex = new ConcurrentHashMap<>();

    private final RakNetServer server;
    private SystemAddress address;
    private int state = STATE_UNCONNECTED;
    private List<EncapsulatedPacket> preJoinQueue = new CopyOnWriteArrayList<>();
    private int mtuSize = 548; //Min size
    private long id = 0;
    private int splitID = 0;

    private int sendSeqNumber = 0;
    private int lastSeqNumber = -1;

    private long lastUpdate;
    private long startTime;

    private List<CustomPacket> packetToSend = new ArrayList<>();

    private boolean isActive;

    private List<Integer> ACKQueue = new ArrayList<>();
    private List<Integer> NACKQueue = new ArrayList<>();

    private Map<Integer, CustomPacket> recoveryQueue = new ConcurrentHashMap<>();

    private Map<Integer, Map<Integer, EncapsulatedPacket>> splitPackets = new ConcurrentHashMap<>();

    private Map<Integer, Map<Integer, Integer>> needACK = new ConcurrentHashMap<>();


    private CustomPacket sendQueue;

    private int windowStart;
    private Map<Integer, Integer> receivedWindow = new ConcurrentHashMap<>();
    private int windowEnd;

    private int reliableWindowStart;
    private int reliableWindowEnd;
    private Map<Integer, EncapsulatedPacket> reliableWindow = new ConcurrentHashMap<>();
    private int lastReliableIndex = -1;

    public NioSession(RakNetServer server, SystemAddress address) {
        this.server = server;
        this.address = address;
        sendQueue = new CustomPackets.CustomPacket_4();
        lastUpdate = System.currentTimeMillis();
        startTime = System.currentTimeMillis();
        isActive = false;
        windowStart = -1;
        windowEnd = WINDOW_SIZE;

        reliableWindowStart = 0;
        reliableWindowEnd = WINDOW_SIZE;

        for(byte i = 0; i < 32; i++){
            channelIndex.put(i, 0);
        }
    }

    public SystemAddress getAddress() {
        return address;
    }

    public String getIpAddress() {
        return address.getIpAddress();
    }

    public int getPort() {
        return address.getPort();
    }

    public long getID() {
        return id;
    }

    public void update(long time) {
        if(!isActive && (lastUpdate + 10000) < time){ //10 second timeout
            disconnect("timeout");
            return;
        }
        isActive = false;

        if(!ACKQueue.isEmpty()){
            ACKPacket pk = new ACKPacket();
            pk.packets = ACKQueue.stream().toArray(Integer[]::new);
            sendPacket(pk);
            ACKQueue.clear();
        }

        if(!NACKQueue.isEmpty()){
            NACKPacket pk = new NACKPacket();
            pk.packets = NACKQueue.stream().toArray(Integer[]::new);
            sendPacket(pk);
            NACKQueue.clear();
        }

        if(!packetToSend.isEmpty()){
            int limit = 16;
            for(int i = 0; i < packetToSend.size(); i++){
                CustomPacket pk = packetToSend.get(i);
                pk.sendTime = time;
                pk.encode();
                recoveryQueue.put(pk.sequenceNumber, pk);
                packetToSend.remove(pk);
                sendPacket(pk);
                if(limit-- <= 0){
                    break;
                }
            }
        }

        if(packetToSend.size() > WINDOW_SIZE){
            packetToSend.clear();
        }

        for(Integer seq : recoveryQueue.keySet()){
            CustomPacket pk = recoveryQueue.get(seq);
            if(pk.sendTime < Instant.now().toEpochMilli() - 6000){ //If no ACK in 6 seconds, resend :)
                packetToSend.add(pk);
                recoveryQueue.remove(seq);
            } else {
                break;
            }
        }

        for(Integer seq : receivedWindow.keySet()){
            if(seq < windowStart){
                receivedWindow.remove(seq);
            } else {
                break;
            }
        }

        try {
            sendQueue();
        } catch (Exception e) {
            server.getLogger().error("Failed to send queue: " + e.getClass().getName() + ":" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void disconnect() {
        disconnect("unknown");
    }

    public void disconnect(String reason) {
        close();
        server.closeSession(this, reason);
    }

    private void sendPacket(RakNetPacket packet) {
        server.workers.execute(() -> server.sendPacket(packet, address));
    }

    public void sendQueue() {
        if(!sendQueue.packets.isEmpty()){
            sendQueue.sequenceNumber = sendSeqNumber++;
            sendPacket(sendQueue);
            sendQueue.sendTime = System.currentTimeMillis();
            recoveryQueue.put(sendQueue.sequenceNumber, sendQueue);
            sendQueue = new CustomPackets.CustomPacket_4();
        }
    }

    private void addToQueue(EncapsulatedPacket pk) {
        addToQueue(pk, false);
    }

    private void addToQueue(EncapsulatedPacket pk, boolean immediate) {
        if(immediate){ //Skip queues
            CustomPacket packet = new CustomPackets.CustomPacket_0();
            packet.sequenceNumber = sendSeqNumber++;
            packet.packets.add(pk);

            sendPacket(packet);
            packet.sendTime = System.currentTimeMillis();
            recoveryQueue.put(packet.sequenceNumber, packet);
            return;
        }
        int length = sendQueue.getSize();
        if(length + pk.getSize() > mtuSize){
            sendQueue();
        }

        sendQueue.packets.add(pk);
    }

    public void addEncapsulatedToQueue(EncapsulatedPacket packet) {
        addEncapsulatedToQueue(packet, false);
    }

    public void addEncapsulatedToQueue(EncapsulatedPacket packet, boolean immediate) {
        if(packet.reliability == Reliability.RELIABLE || packet.reliability == Reliability.RELIABLE_ORDERED || packet.reliability == Reliability.RELIABLE_SEQUENCED){
            packet.messageIndex = messageIndex++;
        }

        if(packet.reliability == Reliability.UNRELIABLE_SEQUENCED || packet.reliability == Reliability.RELIABLE_SEQUENCED || packet.reliability == Reliability.RELIABLE_ORDERED) {
            channelIndex.put(packet.orderChannel, channelIndex.get(packet.orderChannel) + 1);
            packet.orderIndex = channelIndex.get(packet.orderChannel);
        }

        if(packet.getSize() + 4 > mtuSize){
            byte[][] buffers = JRakLibPlus.splitByteArray(packet.payload, mtuSize - 34);
            int splitID = this.splitID++;
            splitID = splitID % 65536;
            for(int count = 0; count < buffers.length; count++){
                byte[] buffer = buffers[count];
                EncapsulatedPacket pk = new EncapsulatedPacket();
                pk.splitID = (short) splitID;
                pk.split = true;
                pk.splitCount = buffers.length;
                pk.reliability = packet.reliability;
                pk.splitIndex = count;
                pk.payload = buffer;
                if(count > 0){
                    pk.messageIndex = messageIndex++;
                } else {
                    pk.messageIndex = packet.messageIndex;
                }
                if(pk.reliability == Reliability.RELIABLE_ORDERED){
                    pk.orderChannel = packet.orderChannel;
                    pk.orderIndex = packet.orderIndex;
                }
                addToQueue(pk, true);
            }
        } else {
            addToQueue(packet, immediate);
        }
    }

    private void handleSplit(EncapsulatedPacket packet) {
        if(packet.splitCount >= 128){
            return;
        }

        if(!splitPackets.containsKey(packet.splitID)){
            Map<Integer, EncapsulatedPacket> map = new ConcurrentHashMap<>();
            map.put(packet.splitIndex, packet);
            splitPackets.put(packet.splitID, map);
        } else {
            Map<Integer, EncapsulatedPacket> map = splitPackets.get(packet.splitID);
            map.put(packet.splitIndex, packet);
            splitPackets.put(packet.splitID, map);
        }

        if(splitPackets.get(packet.splitID).values().size() == packet.splitCount){
            EncapsulatedPacket pk = new EncapsulatedPacket();
            ByteBuffer bb = ByteBuffer.allocate(64 * 64 * 64);
            for(int i = 0; i < packet.splitCount; i++){
                bb.put(splitPackets.get(packet.splitID).get(i).payload);
            }
            pk.payload = Arrays.copyOf(bb.array(), bb.position());
            bb = null;

            splitPackets.remove(packet.splitID);

            handleEncapsulatedPacketRoute(pk);
        }
    }

    private void handleEncapsulatedPacket(EncapsulatedPacket packet) {
        if(packet.messageIndex == -1){
            handleEncapsulatedPacketRoute(packet);
        } else {
            if(packet.messageIndex < reliableWindowStart || packet.messageIndex > reliableWindowEnd){
                return;
            }

            if((packet.messageIndex - lastReliableIndex) == 1){
                lastReliableIndex++;
                reliableWindowStart++;
                reliableWindowEnd++;
                handleEncapsulatedPacketRoute(packet);

                if(!reliableWindow.values().isEmpty()){
                    //TODO: Implement ksort() ?
                    //ksort(reliableWindow.values());

                    for(Integer index : reliableWindow.keySet()){
                        EncapsulatedPacket pk = reliableWindow.get(index);

                        if((index - lastReliableIndex) != 1){
                            break;
                        }
                        lastReliableIndex++;
                        reliableWindowStart++;
                        reliableWindowEnd++;
                        handleEncapsulatedPacketRoute(packet);
                        reliableWindow.remove(index);
                    }
                }
            } else {
                reliableWindow.put(packet.messageIndex, packet);
            }
        }
    }

    private void handleEncapsulatedPacketRoute(EncapsulatedPacket packet) {
        if(server == null){
            return;
        }

        if(packet.split){
            if(state == STATE_CONNECTED){
                handleSplit(packet);
            }
            return;
        }

        if(packet.payload == null || packet.payload.length < 1) {
            return;
        }

        byte id = packet.payload[0];
        if(id < 0x80) { //internal data packet
            if (state == STATE_CONNECTING_2) {
                if (id == MC_CLIENT_CONNECT) {
                    ClientConnectPacket dataPacket = (ClientConnectPacket) RakNetServer.getAndDecodePacket(packet.payload);
                    ServerHandshakePacket pk = new ServerHandshakePacket();
                    pk.address = address;
                    pk.sendPing = dataPacket.sendPing;
                    pk.sendPong = dataPacket.sendPing + 1000L;

                    EncapsulatedPacket sendPacket = new EncapsulatedPacket();
                    sendPacket.reliability = Reliability.UNRELIABLE;
                    sendPacket.payload = pk.encode();
                    addToQueue(sendPacket, true);
                } else if (id == MC_CLIENT_HANDSHAKE) {
                    ClientHandshakePacket dataPacket = (ClientHandshakePacket) RakNetServer.getAndDecodePacket(packet.payload);

                    if (dataPacket.address.getPort() == server.getPort() || !server.getOptions().portChecking) {
                        state = STATE_CONNECTED; //FINALLY!
                        server.signalSessionConnected(this);
                        for (EncapsulatedPacket p : preJoinQueue) {
                            server.signalEncapsulatedPacket(this, p);
                        }
                        preJoinQueue.clear();
                    }
                }
            } else if (id == MC_DISCONNECT_NOTIFICATION) {
                disconnect("client disconnect");
            } else if (id == MC_PING) {
                PingPacket dataPacket = (PingPacket) RakNetServer.getAndDecodePacket(packet.payload);

                PongPacket pk = new PongPacket();
                pk.pingID = dataPacket.pingID;

                EncapsulatedPacket sendPacket = new EncapsulatedPacket();
                sendPacket.reliability = Reliability.UNRELIABLE;
                sendPacket.payload = pk.encode();
                addToQueue(sendPacket);
                //TODO: add PING/PONG (0x00/0x03) automatic latency measure
            } else if(state  == STATE_CONNECTED) {
                server.signalEncapsulatedPacket(this, packet);
                //TODO: stream channels
            }
        } else {
            preJoinQueue.add(packet);
        }
    }

    public void handlePacket(RakNetPacket packet) {
        isActive = true;
        lastUpdate = System.currentTimeMillis();
        if(state == STATE_CONNECTED || state == STATE_CONNECTING_2){
            if(packet.getPID() >= 0x80 || packet.getPID() <= 0x8f && packet instanceof CustomPacket){
                CustomPacket dp = (CustomPacket) packet;
                if(dp.sequenceNumber < windowStart || dp.sequenceNumber > windowEnd || receivedWindow.containsKey(dp.sequenceNumber)){
                    return;
                }

                int diff = dp.sequenceNumber - lastSeqNumber;

                NACKQueue.remove(Integer.valueOf(dp.sequenceNumber));
                ACKQueue.add(dp.sequenceNumber);
                receivedWindow.put(dp.sequenceNumber, dp.sequenceNumber);

                if(diff != 1){
                    for(int i = lastSeqNumber + 1; i < dp.sequenceNumber; i++){
                        if(!receivedWindow.containsKey(i)){
                            NACKQueue.add(i);
                        }
                    }
                }

                if(diff >= 1){
                    lastSeqNumber = dp.sequenceNumber;
                    windowStart += diff;
                    windowEnd += diff;
                }

                for(EncapsulatedPacket pk : dp.packets){
                    handleEncapsulatedPacket(pk);
                }
            } else {
                if(packet instanceof ACKPacket){
                    for(int seq : ((ACKPacket) packet).packets){
                        if(recoveryQueue.containsKey(seq)){
                            for(EncapsulatedPacket pk : recoveryQueue.get(seq).packets) {
                                recoveryQueue.remove(seq);
                            }
                        }
                    }
                } else if(packet instanceof NACKPacket){
                    for(Integer seq : ((NACKPacket) packet).packets){
                        if(recoveryQueue.containsKey(seq)){
                            CustomPacket pk = recoveryQueue.get(seq);
                            pk.sequenceNumber = sendSeqNumber++;
                            packetToSend.add(pk);
                            recoveryQueue.remove(seq);
                        }
                    }
                }
            }
        } else if(packet.getPID() > 0x00 || packet.getPID() < 0x80){ //Not Data packet :)
            if(packet instanceof ConnectedPingOpenConnectionsPacket){
                UnconnectedPongOpenConnectionsPacket pk = new UnconnectedPongOpenConnectionsPacket();
                pk.serverID = server.serverID;
                pk.pingID = ((ConnectedPingOpenConnectionsPacket) packet).pingID;
                pk.identifier = server.getBroadcastName();
                pk.encode();
                sendPacket(pk);
            } else if(packet instanceof OpenConnectionRequest1Packet){
                OpenConnectionRequest1Packet request1 = (OpenConnectionRequest1Packet) packet;
                if(request1.protocolVersion != JRakLibPlus.RAKNET_PROTOCOL && server.getOptions().disconnectInvalidProtocol) {
                    IncompatibleProtocolVersionPacket ipvp = new IncompatibleProtocolVersionPacket();
                    ipvp.protocolVersion = JRakLibPlus.RAKNET_PROTOCOL;
                    ipvp.serverID = server.serverID;
                    sendPacket(ipvp);
                    disconnect("invalid raknet protocol: "+request1.protocolVersion);
                    return;
                }
                OpenConnectionReply1Packet pk = new OpenConnectionReply1Packet();
                pk.mtuSize = request1.nullPayloadLength;
                pk.serverID = server.serverID;
                pk.encode();
                sendPacket(pk);
                state = STATE_CONNECTING_1;
            } else if(state == STATE_CONNECTING_1 && packet instanceof OpenConnectionRequest2Packet){
                id = ((OpenConnectionRequest2Packet) packet).clientID;
                if(((OpenConnectionRequest2Packet) packet).serverAddress.getPort() == server.getPort() || !server.getOptions().portChecking){
                    mtuSize = Math.min(Math.abs(((OpenConnectionRequest2Packet) packet).mtuSize), 1464); //Max size, do not allow creating large buffers to fill server memory
                    OpenConnectionReply2Packet pk = new OpenConnectionReply2Packet();
                    pk.mtuSize = mtuSize;
                    pk.serverID = server.serverID;
                    pk.clientAddress = address;
                    pk.encode();
                    sendPacket(pk);
                    state = STATE_CONNECTING_2;
                }
            }
        }
    }

    public void close() {
        EncapsulatedPacket pk = new EncapsulatedPacket();
        pk.reliability = Reliability.UNRELIABLE;
        pk.payload = new DisconnectNotificationPacket().encode();
        addEncapsulatedToQueue(pk, true);
        //server = null;
        state = NioSession.STATE_UNCONNECTED;
    }

    @Override
    public String toString() {
        return "NioSession{address: "+address.getIpAddress()+":"+address.getPort()+", state: "+state+"}";
    }
}
