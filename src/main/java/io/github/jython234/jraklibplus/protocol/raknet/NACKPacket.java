package io.github.jython234.jraklibplus.protocol.raknet;

import static io.github.jython234.jraklibplus.JRakLibPlus.*;

/**
 * Created by jython234 on 9/12/2015.
 *
 * @author RedstoneLamp Team
 */
public class NACKPacket extends AcknowledgePacket {

    @Override
    public byte getPID() {
        return NACK;
    }
}
