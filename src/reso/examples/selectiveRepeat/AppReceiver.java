package reso.examples.selectiveRepeat;

import reso.common.AbstractApplication;
import reso.ip.IPHost;
import reso.ip.IPLayer;

public class AppReceiver
        extends AbstractApplication {

    private final IPLayer ip;

    public AppReceiver(IPHost host) {
        super(host, "receiver");
        ip = host.getIPLayer();
    }

    public void start() {
        ip.addListener(SelectiveRepeatReceiver.IP_PROTO_SELECTREPEAT, new SelectiveRepeatReceiver((IPHost) host));
    }

    public void stop() {
    }

    public String getHostName() {
        return host.name;
    }

}
