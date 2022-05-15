package reso.examples.selectiveRepeat;

import reso.common.AbstractApplication;
import reso.ip.IPAddress;
import reso.ip.IPHost;

import java.util.Random;

/**
 * Sender application that sends message to the {@link AppReceiver} using the {@link SelectiveRepeatProtocol}
 */
public class AppSender
        extends AbstractApplication {

    private final IPAddress dst;

    private final double packetLossProbability;
    private final int packetNbr;

    /**
     * AppSender constructor
     *
     * @param host                  The host of the sender side of the protocol.
     * @param dst                   The destination of the message
     * @param packetNbr             The number of packet that will be sent.
     * @param packetLossProbability The probability that a packet will be lost (0-1)
     */
    public AppSender(IPHost host, IPAddress dst, int packetNbr, double packetLossProbability) {
        super(host, "sender");
        this.dst = dst;
        this.packetLossProbability = packetLossProbability;
        this.packetNbr = packetNbr;
    }

    /**
     * Starts the application.
     * <p>
     * When the application is started,
     * it tries to send N packet with randomly generated data to the destination.(N=packetNbr)
     */
    public void start()
            throws Exception {
        Random rand = new Random();
        Packet[] packets = new Packet[packetNbr];
        for (int i = 0; i < packetNbr; i++) {
            packets[i] = new Packet(rand.nextInt(), i);
        }
        SelectiveRepeatProtocol transportLayer = new SelectiveRepeatProtocol((IPHost) host, packetNbr, packetLossProbability);
        for (int i = 0; i < packetNbr; i++) {
            transportLayer.sendData(packets[i].data, dst);
        }
    }

    public void stop() {
    }

}

