package io.github.jython234.jraklibplus.server;

import io.github.jython234.jraklibplus.protocol.raknet.EncapsulatedPacket;

/**
 * Interface for communicating with the server implementation.
 *
 * @author RedstoneLamp Team
 */
public interface ServerInterface {

    void handleEncapsulatedPacket(EncapsulatedPacket packet, NioSession session);

    void sessionOpened(NioSession session);

    void sessionClosed(NioSession session, String reason);
}
