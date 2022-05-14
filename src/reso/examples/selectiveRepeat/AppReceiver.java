package reso.examples.selectiveRepeat;

import reso.common.AbstractApplication;
import reso.examples.selectiveRepeat.logger.Logger;
import reso.ip.IPAddress;
import reso.ip.IPHost;

public class AppReceiver
        extends AbstractApplication {

    private final IPAddress ipAddress;

    private final double packetLossProbability;

    private final int packetNbr;

    public AppReceiver(IPHost host,IPAddress ipAddress,int packetNbr,double packetLossProbability) {
        super(host, "receiver");
        this.packetNbr = packetNbr;
        this.packetLossProbability = packetLossProbability;
        this.ipAddress = ipAddress;
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
        System.out.println("=====> SELECTIVE-REPEAT (" + (int) (host.getNetwork().getScheduler().getCurrentTime() * 1000) + "ms)" +
                " host=" + host.name + ", dgram.src=" + src   + ", counter=" + data);
    }

    public String getIPAddressAsString() {
        return ipAddress.toString();
    }
}
