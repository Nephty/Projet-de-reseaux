package reso.examples.selectiveRepeat;

import reso.common.Message;

public class Packet implements Message {

    public final SelectiveRepeatMessage msg;

    private final int seqNumber;

    public Packet(SelectiveRepeatMessage msg, int seqNumber) {
        this.msg = msg;
        this.seqNumber = seqNumber;
    }

    public String toString() {
        return "Packet " + seqNumber + "= [" + msg + "]";
    }

    public int getSeqNumber() {
        return seqNumber;
    }

    @Override
    public int getByteLength() {
        return (Character.SIZE * msg.msg.length()) / 8 + Integer.SIZE / 8;
    }
}
