package io.github.jython234.jraklibplus.protocol.raknet;

/**
 * Represents a level of Reliability on the RakNet network.
 * <br>
 * Information was found at: http://www.jenkinssoftware.com/raknet/manual/reliabilitytypes.html
 *
 * @author jython234
 */
public enum Reliability {
    UNRELIABLE((byte) 0),
    UNRELIABLE_SEQUENCED((byte) 1),
    RELIABLE((byte) 2),
    RELIABLE_ORDERED((byte) 3),
    RELIABLE_SEQUENCED((byte) 4),
    UNRELIABLE_WITH_ACK_RECEIPT((byte) 5),
    RELIABLE_WITH_ACK_RECEIPT((byte) 6),
    RELIABLE_ORDERED_WITH_ACK_RECEIPT((byte) 7);

    private byte reliability;

    Reliability(byte reliability) {
        this.reliability = reliability;
    }

    public static Reliability lookup(byte reliability) {
        switch (reliability) {
            case 0:
                return UNRELIABLE;
            case 1:
                return UNRELIABLE_SEQUENCED;
            case 2:
                return RELIABLE;
            case 3:
                return RELIABLE_ORDERED;
            case 4:
                return RELIABLE_SEQUENCED;
            case 5:
                return UNRELIABLE_WITH_ACK_RECEIPT;
            case 6:
                return RELIABLE_WITH_ACK_RECEIPT;
            case 7:
                return RELIABLE_ORDERED_WITH_ACK_RECEIPT;
            default:
                return null;
        }
    }

    public byte asByte() {
        return reliability;
    }
}
