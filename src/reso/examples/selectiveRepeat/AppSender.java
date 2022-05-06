package reso.examples.selectiveRepeat;

import reso.common.AbstractApplication;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPLayer;

public class AppSender
        extends AbstractApplication {

    private final IPLayer ip;
    private final IPAddress dst;
    private final String msg;

    public AppSender(IPHost host, IPAddress dst, String msg) {
        super(host, "sender");
        this.dst = dst;
        this.msg = msg;
        ip = host.getIPLayer();
    }

    public void start()
            throws Exception {
        SelectiveRepeatSender sender = new SelectiveRepeatSender((IPHost) host);
        ip.addListener(SelectiveRepeatReceiver.IP_PROTO_SELECTREPEAT, sender);
        sender.sendPackets(msg, dst);
        //sender.sendPacket(new SelectiveRepeatMessage(msg+1),dst);
        //sender.sendPacket(new SelectiveRepeatMessage(msg+2),dst);
        //sender.sendPacket(new SelectiveRepeatMessage(msg+3),dst);
        //ip.send(IPAddress.ANY, dst, SelectiveRepeatReceiver.IP_PROTO_SELECTREPEAT, new SelectiveRepeatMessage(msg));
    }

    public void stop() {
    }

}

