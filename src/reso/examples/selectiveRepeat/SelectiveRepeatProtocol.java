package reso.examples.selectiveRepeat;

import reso.examples.pingpong.PingPongMessage;
import reso.ip.*;

public class SelectiveRepeatProtocol implements IPInterfaceListener {

    public static final int IP_PROTO_SELECTREPEAT = Datagram.allocateProtocolNumber("SELECTIVE_REPEAT");

    private final IPHost host;

    public SelectiveRepeatProtocol(IPHost host) {
        this.host = host;
    }

    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        SelectiveRepeatMessage msg = (SelectiveRepeatMessage) datagram.getPayload();
        System.out.println("Selective Repeat (" + (int) (host.getNetwork().getScheduler().getCurrentTime() * 1000) + "ms)" +
                " host=" + host.name + ", dgram.src=" + datagram.src + ", dgram.dst=" +
                datagram.dst + ", iif=" + src + ", message=" + msg.msg);
    }

}
