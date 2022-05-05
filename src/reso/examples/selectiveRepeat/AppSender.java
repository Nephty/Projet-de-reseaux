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
        ip.addListener(SelectiveRepeatProtocol.IP_PROTO_SELECTREPEAT, new SelectiveRepeatProtocol((IPHost) host));
        ip.send(IPAddress.ANY, dst, SelectiveRepeatProtocol.IP_PROTO_SELECTREPEAT, new SelectiveRepeatMessage(msg));
    }

    public void stop() {
    }

}

