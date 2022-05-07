package reso.examples.selectiveRepeat;

import reso.common.AbstractApplication;
import reso.ip.IPHost;
import reso.ip.IPLayer;

public class AppReceiver
        extends AbstractApplication {


    public AppReceiver(IPHost host) {
        super(host, "receiver");
    }

    public void start() {
        new SelectiveRepeatProtocol((IPHost) host);
    }

    public void stop() {
    }

    public String getHostName() {
        return host.name;
    }

}
