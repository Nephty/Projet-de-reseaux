package reso.examples.selectiveRepeat;

import reso.common.Message;

/**
 * Packet class used in the {@link SelectiveRepeatProtocol}.
 * <p>
 * This class can be used to send data (isAck = false && data != null)
 * but it can also be used to send ACK (isAck = true && data == null).
 */
public class Packet implements Message {

    public int data;
    public final int seqNumber;
    public boolean isAck;

    /**
     * Packet constructor for data.
     *
     * @param data      The data we want to send
     * @param seqNumber The sequence number of the packet {@link SelectiveRepeatProtocol see the protocol for more information}
     */
    public Packet(int data, int seqNumber) {
        this.data = data;
        this.seqNumber = seqNumber;
        isAck = false;
    }

    /**
     * Packet constructor for ACK.
     *
     * @param seqNumber The sequence number of the packet {@link SelectiveRepeatProtocol see the protocol for more information}
     */
    public Packet(int seqNumber) {
        this.isAck = true;
        this.seqNumber = seqNumber;
    }

    /**
     * Get the string format of the packet.
     * <p>
     * Format like : "Packet [$seqNumber$, isAck=$isAck$]
     *
     * @return The packet formatted as a string
     */
    public String toString() {
        return "Packet [" + seqNumber + ", isAck=" + isAck + "]";
    }

    /**
     * Get the sequence number of the Packet
     *
     * @return {@link #seqNumber} of the packet.
     */
    public int getSeqNumber() {
        return seqNumber;
    }

    /**
     * Get the number of byte that the packet contains.
     * <p>
     * This is not used in our implementation because our window is calculated per packet and not per byte.
     *
     * @return The number of byte that the packet contains.
     */
    @Override
    public int getByteLength() {
        return 3 * (Integer.SIZE) / 8;
    }
}
