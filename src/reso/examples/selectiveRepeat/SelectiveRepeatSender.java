package reso.examples.selectiveRepeat;

import reso.common.AbstractTimer;
import reso.ip.*;
import reso.scheduler.AbstractScheduler;

public class SelectiveRepeatSender implements IPInterfaceListener {


    public final int WINDOW_SIZE = 8;
    public Packet[] window;

    private final int send_base = 0;
    private int next_seq_num = 0;

    private final IPHost host;



    public SelectiveRepeatSender(IPHost host) {
        this.host = host;
        window = new Packet[WINDOW_SIZE * 2];
    }

    public void sendPackets(String msg, IPAddress dst) throws Exception {
        // TODO: 5/6/22
    }

    public void sendPacket(SelectiveRepeatMessage msg, IPAddress dst) throws Exception {
        if (next_seq_num < send_base + WINDOW_SIZE) {
            Packet pkt = new Packet(msg, next_seq_num);
            window[next_seq_num] = pkt; // Add packet to window
            host.getIPLayer().send(IPAddress.ANY, dst, SelectiveRepeatReceiver.IP_PROTO_SELECTREPEAT, window[next_seq_num]);
            // todo - Start timer here -
            next_seq_num = (next_seq_num + 1) % window.length;
        }
    }


    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        // TODO: 5/5/22 ACK RECEPTION
        Packet packet = (Packet) datagram.getPayload();
        System.out.println(packet.getSeqNumber() + " " + packet.msg.msg);
    }
}
