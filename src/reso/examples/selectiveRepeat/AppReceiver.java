package reso.examples.selectiveRepeat;

import reso.common.AbstractApplication;
import reso.examples.selectiveRepeat.logger.Logger;
import reso.ip.IPAddress;
import reso.ip.IPHost;

/**
 * Receiver application that receives message from the {@link AppSender} using the {@link SelectiveRepeatProtocol}
 */
public class AppReceiver
        extends AbstractApplication {

    private final IPAddress ipAddress;

    private final double packetLossProbability;

    private final int packetNbr;

    /**
     * AppReceiver constructor
     * @param host The host of the receiver side of the protocol.
     * @param ipAddress The IP of the Receiver
     * @param packetNbr The number of packet that he's going to receive from the Sender.
     * @param packetLossProbability The probability to lose an ACK.
     */
    public AppReceiver(IPHost host,IPAddress ipAddress,int packetNbr,double packetLossProbability) {
        super(host, "receiver");
        this.packetNbr = packetNbr;
        this.packetLossProbability = packetLossProbability;
        this.ipAddress = ipAddress;
    }

    /**
     * Starts the application.
     *
     * It only creates the {@link SelectiveRepeatProtocol} for the Receiver.
     */
    public void start() {
        new SelectiveRepeatProtocol((IPHost) host,packetNbr,packetLossProbability);
    }

    public void stop() {
    }
}