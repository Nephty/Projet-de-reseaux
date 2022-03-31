package reso.examples.queueing;

import reso.common.AbstractMessage;
import reso.ethernet.EthernetFrame;
import reso.ip.Datagram;

public class ProbeMessage
        extends AbstractMessage {
    public final int seqNum;
    public final double departureTime;
    private final int size;

    public ProbeMessage(int size, int seqNum, double departureTime) {
        this.size = size;
        this.seqNum = seqNum;
        this.departureTime = departureTime;
    }

    public int getByteLength() {
        return size - Datagram.HEADER_LEN - EthernetFrame.HEADER_LEN;
    }

    public String toString() {
        return "Packet " + seqNum;
    }
}