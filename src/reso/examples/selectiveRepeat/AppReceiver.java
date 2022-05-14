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

    public void receiveData(int data, IPAddress src){
        Logger.packetReceived(data,src,ipAddress,host);
    }
}