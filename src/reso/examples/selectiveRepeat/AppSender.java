package reso.examples.selectiveRepeat;

import reso.common.AbstractApplication;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPLayer;

import java.util.Random;

public class AppSender
        extends AbstractApplication {

    private final IPAddress dst;

    private final double packetLossProbability;
    private final int packetNbr;

    public AppSender(IPHost host, IPAddress dst,int packetNbr,double packetLossProbability) {
        super(host, "sender");
        this.dst = dst;
        this.packetLossProbability = packetLossProbability;
        this.packetNbr = packetNbr;
    }

    public void start()
            throws Exception {
        Random rand = new Random();
        Packet[] packets = new Packet[packetNbr];
        for (int i=0;i<packetNbr;i++){
            packets[i] = new Packet(i+1,i);
        }
        SelectiveRepeatProtocol transportLayer = new SelectiveRepeatProtocol((IPHost) host,packetNbr,packetLossProbability);
        for(int i=0;i<packetNbr;i++){
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

