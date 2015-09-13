package io.github.jython234.jraklibplus.protocol.raknet;

import io.github.jython234.jraklibplus.protocol.RakNetPacket;
import io.github.jython234.jraklibplus.nio.Buffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Packet Encode/Decode implementation.
 *
 * @author RedstoneLamp Team
 */
public abstract class CustomPacket extends RakNetPacket {

    public int sequenceNumber;
    public List<EncapsulatedPacket> packets = new ArrayList<>();

    @Override
    protected void _encode(Buffer buffer) {
        buffer.putLTriad(sequenceNumber);
        packets.stream().forEach(packet -> packet._encode(buffer));
    }

    @Override
    protected void _decode(Buffer buffer) {
        sequenceNumber = buffer.getLTriad();
        while (buffer.getRemainingBytes() >= 4) { //4 is the smallest amount of bytes an EncapsulatedPacket can be
            EncapsulatedPacket packet = new EncapsulatedPacket();
            packet._decode(buffer);
            packets.add(packet);
        }
    }

    @Override
    public int getSize() {
        int len = 4;
        for(EncapsulatedPacket packet : packets) {
            len = len + packet.getSize();
        }
        return len;
    }
}
