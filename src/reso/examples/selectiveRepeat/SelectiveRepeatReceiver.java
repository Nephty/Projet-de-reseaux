package reso.examples.selectiveRepeat;

import reso.ip.*;

public class SelectiveRepeatReceiver implements IPInterfaceListener {

    public static final int IP_PROTO_SELECTREPEAT = Datagram.allocateProtocolNumber("SELECTIVE_REPEAT");

    public final int WINDOW_SIZE = 4;
    private final IPHost host;

    private int recv_base;
    public SelectiveRepeatMessage[] window;

    public SelectiveRepeatReceiver(IPHost host) {
        this.host = host;
        window = new SelectiveRepeatMessage[WINDOW_SIZE * 2];
        recv_base = 0;
    }

    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        // packet reception
        Packet msg = (Packet) datagram.getPayload();
        //System.out.println("Packet received : " + msg);
        int n = msg.getSeqNumber();
        if (recv_base <= n && n <= recv_base + WINDOW_SIZE - 1) {
            // If sequence number in window.
            // TODO: 5/5/22 Trouver un moyen stylé de stocker les ACK.
            Packet ack = new Packet(new SelectiveRepeatMessage("ACK"), n);
            host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_SELECTREPEAT, ack);

            // Data management
            SelectiveRepeatMessage data = msg.msg;
            if (recv_base == n) {
                System.out.println("Selective Repeat (" + (int) (host.getNetwork().getScheduler().getCurrentTime() * 1000) + "ms)" +
                        " host=" + host.name + ", dgram.src=" + datagram.src + ", dgram.dst=" +
                        datagram.dst + ", iif=" + src + ", message=" + data.msg);

                recv_base = (recv_base + 1) % window.length;
                while (window[recv_base] != null) {
                    System.out.println("Selective Repeat (" + (int) (host.getNetwork().getScheduler().getCurrentTime() * 1000) + "ms)" +
                            " host=" + host.name + ", dgram.src=" + datagram.src + ", dgram.dst=" +
                            datagram.dst + ", iif=" + src + ", message=" + data.msg);
                    recv_base = (recv_base + 1) % window.length;
                }
            } else {
                // Out-of-order packet
                window[n] = data;

            }
        } else if (recv_base - WINDOW_SIZE <= n && n <= recv_base - 1) {
            Packet ack = new Packet(new SelectiveRepeatMessage("ACK"), n);
            host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_SELECTREPEAT, ack);
        }
    }

}
