package io.github.jython234.jraklibplus.protocol.raknet;

import io.github.jython234.jraklibplus.nio.Buffer;
import io.github.jython234.jraklibplus.nio.NioBuffer;
import io.github.jython234.jraklibplus.protocol.RakNetPacket;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for ACK/NACK
 *
 * @author RedstoneLamp Team
 */
public abstract class AcknowledgePacket extends RakNetPacket {

    public Integer[] packets;

    @Override
    protected void _encode(Buffer buffer) {
        Buffer payload = NioBuffer.allocateBuffer(0, ByteOrder.BIG_ENDIAN);
        int count = packets.length;
        int records = 0;

        if(count > 0) {
            int pointer = 0;
            int start = packets[0];
            int last = packets[0];

            while(pointer + 1 < count) {
                int current = packets[pointer++];
                int diff = current - last;
                if(diff == 1){
                    last = current;
                } else if(diff > 1){ //Forget about duplicated packets (bad queues?)
                    if(start == last){
                        payload.putBoolean(true);
                        payload.putLTriad(start);
                        start = last = current;
                    } else {
                        payload.putBoolean(false);
                        payload.putLTriad(start);
                        payload.putLTriad(last);
                        start = last = current;
                    }
                    records = records + 1;
                }
            }

            if(start == last){
                payload.putBoolean(true);
                payload.putLTriad(start);
            } else {
                payload.putBoolean(false);
                payload.putLTriad(start);
                payload.putLTriad(last);
            }
            records = records + 1;
        }

        buffer.putUnsignedShort(records);
        buffer.put(payload.toByteArray());
    }

    @Override
    protected void _decode(Buffer buffer) {
        int count = buffer.getUnsignedShort();
        List<Integer> packets = new ArrayList<>();
        int cnt = 0;
        for(int i = 0; i < count && buffer.getRemainingBytes() > 0 && cnt < 4096; i++) {
            if(!buffer.getBoolean()) {
                int start = buffer.getLTriad();
                int end = buffer.getLTriad();
                if((end - start) > 512) {
                    end = start + 512;
                }
                for(int c = start; c <= end; c++) {
                    cnt = cnt + 1;
                    packets.add(c);
                }
            } else {
                packets.add(buffer.getLTriad());
            }
        }
        this.packets = packets.stream().toArray(Integer[]::new);
    }
}
