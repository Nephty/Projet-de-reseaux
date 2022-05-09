package reso.examples.selectiveRepeat;

import reso.common.AbstractApplication;
import reso.ip.IPAddress;
import reso.ip.IPHost;

public class AppReceiver
        extends AbstractApplication {

    private final double packetLossProbability;

    private final int packetNbr;

    public AppReceiver(IPHost host,int packetNbr,double packetLossProbability) {
        super(host, "receiver");
        this.packetNbr = packetNbr;
        this.packetLossProbability = packetLossProbability;
    }

    public void start() {
        new SelectiveRepeatProtocol((IPHost) host,packetNbr,this,packetLossProbability);
    }

    public void stop() {
    }

    public String getHostName() {
        return host.name;
    }

    public void receiveData(int data, IPAddress src){
        //System.out.println("SELECTIVE-REPEAT (" + (int) (host.getNetwork().getScheduler().getCurrentTime() * 1000) + "ms)" +
        //        " host=" + host.name + ", dgram.src=" + src   + ", counter=" + data);
    }

}
