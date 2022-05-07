package reso.examples.selectiveRepeat;

import reso.common.AbstractApplication;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPLayer;

import java.util.Random;

public class AppSender
        extends AbstractApplication {

    private final IPAddress dst;

    public AppSender(IPHost host, IPAddress dst) {
        super(host, "sender");
        this.dst = dst;
    }

    public void start()
            throws Exception {
        Random rand = new Random();
        Packet[] packets = new Packet[15];
        for (int i=0;i<15;i++){
            packets[i] = new Packet(i,i);
        }
        SelectiveRepeatProtocol transportLayer = new SelectiveRepeatProtocol((IPHost) host);
        for(int i=0;i<15;i++){
            transportLayer.sendData(packets[i].data,dst);
        }
    }

    public void stop() {
    }

    public String getHostName() {
        return host.name;
    }

    public String getIPAddressAsString() {
        return dst.toString();
    }

}

