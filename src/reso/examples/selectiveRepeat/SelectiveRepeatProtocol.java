package reso.examples.selectiveRepeat;

import reso.common.AbstractTimer;
import reso.examples.selectiveRepeat.logger.Logger;
import reso.ip.*;
import reso.scheduler.AbstractScheduler;

import java.util.HashMap;
import java.util.Random;

/**
 * Implementation of the pipelining protocol SelectiveRepeat with a congestion control with the same
 * behavior as TCP RENO (additive increase, Multiplicative decrease, slow start).
 * <p>
 * In this implementation, only integers can be sent.
 */
public class SelectiveRepeatProtocol implements IPInterfaceListener {

    // ================== IP =====================
    public static final int IP_PROTO_SELECTIVE_REPEAT = Datagram.allocateProtocolNumber("SELECTIVE_REPEAT");

    private final IPHost host;

    // ============= SELECTIVE REPEAT ============

    public int next_seq_num = 0;

    public int sendBase = 0;

    public int recvBase = 0;

    private final Packet[] buffer;

    private final Timer[] timers;

    private int bufferSize = 0;

    // ========== PACKET LOSS SIMULATION ==========

    public static Random random = new Random();

    private final double packetLossProbability;

    // ============ CONGESTION CONTROL ============

    private double ssthresh = Double.MAX_VALUE;

    private double cwnd = 1;

    private final HashMap<Integer, Integer> duplicateACKsHashMap = new HashMap<>();

    public String windowSizeHistory = "Time(s),WindowSize(Packet)\n";

    // ================= RTT & RTO ================

    private double devRTT;

    private double SRTT;

    public double RTO = 3;


    /**
     * SENDER constructor
     *
     * @param host                  The host of the sender side of the protocol.
     * @param packetNbr             The number of packets he's going to send.
     * @param packetLossProbability The probability to lose a packet
     */
    public SelectiveRepeatProtocol(IPHost host, int packetNbr, double packetLossProbability) {
        this.host = host;
        this.buffer = new Packet[packetNbr];
        this.timers = new Timer[packetNbr];
        this.packetLossProbability = packetLossProbability;
        windowSizeHistory += host.getNetwork().getScheduler().getCurrentTime() + "," + cwnd + "\n";
        host.getIPLayer().addListener(IP_PROTO_SELECTIVE_REPEAT, this);
    }

    /**
     * The protocol reception behavior.
     * <p>
     * If the packet we received is an ACK, we are in the SENDER side.
     * Each time we receive an ack, the {@link Timer} of the packet is stopped (know with a table of {@link Timer})
     * and we calculate the RTO.<br>
     * The window size is increased here (slow start, additive increase).
     *
     * @param src      source of the message.
     * @param datagram IP datagram of the message, used to get the data of the message.
     * @see #sendData(Packet, IPAddress)
     *
     * <br><br>
     * <p>
     * If the packet isn't an ack, we are in the RECEIVER SIDE.
     * We're always sending an ack if we receive a packet, even if we had already received the packet.
     * @see #sendAck(Datagram)
     */
    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        Packet packet = (Packet) datagram.getPayload();
        if (packet.isAck) {
            if (timers[0] == null)
                // When there's a timeout just before the sender receives the ack of that packet,
                // Then the packet is resent but with ACK = true. I don't know why :(
                return;
            // Sender side
            Logger.logAckReceived(packet);
            int seqNumber = packet.getSeqNumber();
            timers[seqNumber].stop();
            setRTO(timers[seqNumber]);
            if (duplicateACKsHashMap.get(seqNumber) == null) {
                // not in hashmap (= 0)
                duplicateACKsHashMap.put(seqNumber, 1);
            } else {
                int numberOfACKsForSeqNumber = duplicateACKsHashMap.get(seqNumber);
                duplicateACKsHashMap.put(seqNumber, ++numberOfACKsForSeqNumber);
            }
            if (duplicateACKsHashMap.get(seqNumber) > 3) {
                // Loss marked by three duplicate ACKs
                // Multiplicative decrease
                timeout(datagram.src, seqNumber);
                cwnd /= 2;
                ssthresh = cwnd;
                Logger.logLoss(seqNumber, cwnd, ssthresh);
                windowSizeHistory += host.getNetwork().getScheduler().getCurrentTime() + "," + cwnd + "\n";
            }

            double oldCwnd = cwnd;
            double offset;
            if (cwnd < ssthresh) cwnd++;  // Slow start (add one packet to the window)
            else
                cwnd += 1 / cwnd;         // additive increase : (Add 1/cwnd to the window, the value will be round in sendData() )
            Logger.logCongestionWindowSizeChanged(oldCwnd, cwnd);
            windowSizeHistory += host.getNetwork().getScheduler().getCurrentTime() + "," + cwnd + "\n";

            offset = cwnd - oldCwnd;
            if (!buffer[packet.seqNumber].isAck) {
                buffer[packet.seqNumber].setAck(true);
                if (sendBase == packet.seqNumber && sendBase != bufferSize - 1) {
                    // if the ACK is the sendBase, we increase it until we are on a packet that has not yet been acked.
                    while (sendBase < bufferSize && buffer[sendBase].isAck) {
                        sendBase++;
                        offset++;
                    }
                }
                Logger.saveWindowChanges(windowSizeHistory);
            }

            // Send all the packet that is missing in the cwnd.
            for (int i = 0; i <= offset + 1 && next_seq_num < bufferSize; i++) {
                sendData(buffer[next_seq_num], datagram.src);
            }

        } else {
            // Receiver side
            Logger.logPacketReceived(packet);
            sendAck(datagram);
            if (buffer[packet.seqNumber] == null) {
                // We have not yet received this ack.
                buffer[packet.seqNumber] = packet;
                if (packet.seqNumber == recvBase) {
                    Logger.packetReceived(buffer[recvBase].data, datagram.src, datagram.dst, host);
                    recvBase++;
                    while (recvBase < buffer.length && buffer[recvBase] != null) {
                        Logger.packetReceived(buffer[recvBase].data, datagram.src, datagram.dst, host);
                        recvBase++;
                    }
                }
            }

        }
    }

    /**
     * sendData method that the sender use to send data.
     * <p>
     * It buffers his data and send it to the destination directly if the
     * window is big enough. If not, the packets will wait in the buffer.
     *
     * @param data The data the sender wants to transfer
     * @param dst  The destination of the message
     */
    public void sendData(int data, IPAddress dst) throws Exception {
        Packet packet = new Packet(data, bufferSize);
        buffer[bufferSize] = packet;
        this.sendData(packet, dst);
        bufferSize++;
    }

    /**
     * sendData method of the protocol.
     * Check if the window is full and if not, it tries to send the packet with a chance to lose the packet.
     * Sometimes, with the congestion control (mostly because of additive increase), the window size won't be an integer
     * so, we round it only when we want to use it in this condition.
     */
    private void sendData(Packet data, IPAddress dst) throws Exception {
        if (next_seq_num < sendBase + Math.round(cwnd)) {
            if (random.nextDouble() > packetLossProbability) {
                // === SENT ===
                Logger.logPacketSent(data);
                host.getIPLayer().send(IPAddress.ANY, dst, IP_PROTO_SELECTIVE_REPEAT, data);
            } else {
                // === LOSS ===
                Logger.logPacketLoss(data);
            }
            //System.out.println(RTO);
            timers[next_seq_num] = new Timer(host.getNetwork().getScheduler(), RTO, dst, next_seq_num);
            timers[next_seq_num].start();
            next_seq_num++;
        }
    }

    /**
     * Send ACKNOWLEDGMENT method of the protocol.
     * <p>
     * It tries to send the packet with a chance to lose the ACK.
     *
     * @param datagram IP datagram of the message, used to get the sequence number and the src of the message.
     * @see Logger#logAckLoss(Packet)
     */
    public void sendAck(Datagram datagram) throws Exception {
        Packet packet = new Packet(((Packet) datagram.getPayload()).seqNumber);

        if (random.nextDouble() > packetLossProbability) {
            Logger.logAckSent(packet);
            host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_SELECTIVE_REPEAT, packet);
        } else {
            Logger.logAckLoss(packet);
        }
    }

    /**
     * The timer of the packet has ended, the packet has been lost.
     * <p>
     * Changes the RTO, the window size and resend the message.
     *
     * @param dst       The destination of the message.
     * @param seqNumber The sequence number of the packet
     * @see Logger#logLoss(int, double, double)
     */
    public void timeout(IPAddress dst, int seqNumber) throws Exception {
        timers[seqNumber] = new Timer(host.getNetwork().getScheduler(), RTO, dst, seqNumber);
        timers[seqNumber].start();
        host.getIPLayer().send(IPAddress.ANY, dst, IP_PROTO_SELECTIVE_REPEAT, buffer[seqNumber]);

        double oldCwnd = cwnd;
        cwnd = 1;
        ssthresh = oldCwnd / 2;
        RTO *= 2;
        Logger.logLoss(seqNumber, cwnd, ssthresh);
        windowSizeHistory += host.getNetwork().getScheduler().getCurrentTime() + "," + cwnd + "\n";
    }

    // ==================== RTT & RTO ===========================

    /**
     * SRTT calculation method.
     *
     * @param timer Timer of the packet we want to get the SRTT.
     * @return The value of the SRTT of a certain packet.
     */
    public double getSRTT(Timer timer) {
        double alpha = 0.125;
        if (SRTT > 0) {
            SRTT = (1 - alpha) * SRTT + alpha * timer.getRTT();
        } else {
            SRTT = timer.getRTT();
        }
        return SRTT;
    }

    /**
     * DevRTT calculation method.
     *
     * @param timer Timer of the packet we want to get the DevRTT.
     * @return The value of the DevRTT of a certain packet.
     */
    public double getDevRTT(Timer timer) {
        double beta = 0.25;
        if (devRTT > 0) {
            devRTT = (1 - beta) * devRTT + beta * Math.abs((SRTT - timer.getRTT()));
        } else {
            devRTT = timer.getRTT() / 2;
        }
        return devRTT;
    }

    /**
     * Set the value of the RTO.
     * <p>
     * It uses this formula : RTO = 2 * RTO
     *
     * @param timer Timer of the packet from which we want to set the RTO.
     */
    public void setRTO(Timer timer) {
        RTO = getSRTT(timer) + 4 * getDevRTT(timer);
    }

    /**
     * Timer used to detect lost packets.
     */
    private class Timer extends AbstractTimer {
        private final IPAddress dst;

        private final int seqNumber;

        private double startingTime;

        private double stopTime;

        /**
         * Timer constructor.
         *
         * @param scheduler The scheduler of the network. (To have access to the time)
         * @param interval  seconds between two timer events.
         * @param dst       The destination of the packet linked to this timer
         * @param seqNumber The sequence number of the packet linked to the timer
         */
        public Timer(AbstractScheduler scheduler, double interval, IPAddress dst, int seqNumber) {
            super(scheduler, interval, false);
            this.dst = dst;
            this.seqNumber = seqNumber;
        }

        /**
         * This method is called after the interval of time.
         * <p>
         * If the seqNumber hasn't been yet ACKed, we stop the timer and call the {@link #timeout(IPAddress, int)} method.
         */
        protected void run() throws Exception {
            if (!buffer[seqNumber].isAck) {
                stop();
                timeout(dst, seqNumber);
            }
        }

        /**
         * Start the timer and save the creating time of the timer to use it in {@link #getRTT()}
         */
        @Override
        public void start() {
            super.start();
            startingTime = scheduler.getCurrentTime();
        }

        /**
         * Stop the timer and save the stopping time of the timer to use it in {@link #getRTT()}
         */
        @Override
        public void stop() {
            super.stop();
            stopTime = scheduler.getCurrentTime();
        }

        /**
         * get the value of the RTT of the packet.
         *
         * @return The RTT (stopTime - startingTime)
         */
        public double getRTT() {
            //System.out.println("Timer RTT : "+(stopTime - startingTime));
            return stopTime - startingTime;
        }
    }
}
