package io.github.jython234.jraklibplus.server;

import io.github.jython234.jraklibplus.JRakLibPlus;
import io.github.jython234.jraklibplus.protocol.RakNetPacket;
import io.github.jython234.jraklibplus.protocol.minecraft.DisconnectNotificationPacket;
import io.github.jython234.jraklibplus.protocol.raknet.*;

import static io.github.jython234.jraklibplus.JRakLibPlus.*;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by jython234 on 9/12/2015.
 *
 * @author RedstoneLamp Team
 */
public class OldSession {
    private final RakNetServer server;
    private final SocketAddress address;

    private long lastPing = System.currentTimeMillis();
    private int mtu;

    private List<EncapsulatedPacket> toBeProcessed = new CopyOnWriteArrayList<>();

    private CustomPacket sendQueue;
    private int currentSeqNumber = 0;
    private int currentMessageIndex = 0;
    private int currentSplitID;


    public OldSession(RakNetServer server, SocketAddress address) {
        this.server = server;
        this.address = address;

        mtu = 1448;
        sendQueue = new CustomPackets.CustomPacket_4();
    }

    public void update(long time) {
        if((time - lastPing) >= 10000) { //10 second timeout
            disconnect("timeout");
        }
        processEncapsulated();
        if(!sendQueue.packets.isEmpty()) sendQueue();


    }

    private void disconnect(String reason) {
        //server.closeSession(this, reason);
        encapsulateAddToQueue(new DisconnectNotificationPacket().encode());
    }

    /**
     * Adds a byte array to the queue and encapsulates it.
     * @param packet The byte array of the packet to be added.
     */
    public void encapsulateAddToQueue(byte[] packet) {
        EncapsulatedPacket pk = new EncapsulatedPacket();
        pk.reliability = Reliability.RELIABLE;
        pk.payload = packet;
        addEncapsulatedToQueue(pk);
    }

    private synchronized void addEncapsulatedToQueue(EncapsulatedPacket pk) {
        if(pk.reliability == Reliability.RELIABLE_ORDERED) {
            //TODO: channelIndex
        }

        pk.messageIndex = currentMessageIndex++;

        if(pk.getSize() + 4 > mtu) {
            byte[][] buffers = JRakLibPlus.splitByteArray(pk.payload, mtu - 34);
            int splitID = currentSplitID++;
            splitID = splitID % 65536;
            for(int count = 0; count < buffers.length; count++) {
                byte[] buffer = buffers[count];
                EncapsulatedPacket pkt = new EncapsulatedPacket();
                pkt.splitID = splitID;
                pkt.split = true;
                pkt.splitCount = buffers.length;
                pkt.reliability = pk.reliability;
                pkt.splitIndex = count;
                pkt.payload = buffer;
                if (count > 0) {
                    pkt.messageIndex = currentMessageIndex++;
                } else {
                    pkt.messageIndex = pk.messageIndex;
                }
                if (pkt.reliability == Reliability.RELIABLE_ORDERED) {
                    pkt.orderIndex = pk.orderIndex;
                    pkt.orderChannel = pk.orderChannel;
                }
                CustomPacket cp = new CustomPackets.CustomPacket_0();
                cp.sequenceNumber = currentSeqNumber++;
                cp.packets = Arrays.asList(pkt);
                sendPacket(cp);
            }
        } else {
            toBeProcessed.add(pk);
        }
    }

    private void processEncapsulated() {
        for(EncapsulatedPacket pk : toBeProcessed) {
            int len = sendQueue.getSize();
            if(len + pk.getSize() > mtu) {
                sendQueue();
            }

            sendQueue.packets.add(pk);
        }
    }

    private void sendQueue() {
        sendQueue.sequenceNumber = currentSeqNumber++;
        sendPacket(sendQueue);
    }

    public void sendPacket(RakNetPacket packet) {
        //server.workers.execute(() -> server.channel.send(ByteBuffer.wrap(packet.encode()), address));
    }

    public void handleRawPacket(byte[] buffer) {
        RakNetPacket packet = RakNetServer.getAndDecodePacket(buffer);
        if(packet == null) {
            server.getLogger().warn("Unknown packet: 0x"+String.format("%02X", buffer[0]));
            return;
        }
        switch (packet.getPID()) {
            case ID_CONNECTED_PING_OPEN_CONNECTIONS:
            case ID_UNCONNECTED_PING_OPEN_CONNECTIONS:
                ConnectedPingOpenConnectionsPacket cpocp = (ConnectedPingOpenConnectionsPacket) packet;
                UnconnectedPongOpenConnectionsPacket pong = new UnconnectedPongOpenConnectionsPacket();
                pong.pingID = cpocp.pingID;
                pong.serverID = server.serverID;
                pong.identifier = server.getBroadcastName();
                sendPacket(pong);
                break;

        }
    }
}
