package reso.examples.selectiveRepeat;

import reso.common.Message;

public class Packet implements Message {

    public int data;
    public final int seqNumber;
    public boolean isAck;

    public Packet(int data, int seqNumber) {
        this.data = data;
        this.seqNumber = seqNumber;
        isAck = false;
    }

    public Packet(int seqNumber){
        this.isAck = true;
        this.seqNumber = seqNumber;
    }

    public String toString() {
        return "Packet " + seqNumber + ", isAck="+isAck+"]";
    }

    public int getSeqNumber() {
        return seqNumber;
    }

    @Override
    public int getByteLength() {
        return (Integer.SIZE)/8;
    }
}
