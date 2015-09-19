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

import io.github.jython234.jraklibplus.nio.Buffer;
import io.github.jython234.jraklibplus.nio.JavaByteBuffer;
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
        /*
        buffer = JavaByteBuffer.allocate(payload.toByteArray().length + 3, ByteOrder.BIG_ENDIAN);
        buffer.putByte(getPID());
        */
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

    @Override
    public int getSize() {
        return 1;
    }
}
