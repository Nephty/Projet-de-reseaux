package reso.examples.selectiveRepeat;

import reso.common.AbstractTimer;
import reso.examples.selectiveRepeat.logger.Logger;
import reso.ip.*;
import reso.scheduler.AbstractScheduler;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.Random;

public class SelectiveRepeatProtocol implements IPInterfaceListener {

    public static final int IP_PROTO_SELECTIVE_REPEAT = Datagram.allocateProtocolNumber("SELECTIVE_REPEAT");

    public static Random random = new Random();

    public FileWriter fw;

    private AppReceiver receiver;

    private final IPHost host;

    public int next_seq_num = 0;

    public int sendBase = 0;

    public int recvBase = 0;

    private final Packet[] buffer;

    private Timer[] timers;

    private int bufferSize = 0;

    private final double packetLossProbability;

    private double ssthresh = Double.MAX_VALUE;

    private double cwnd = 1;
    private final HashMap<Integer, Integer> duplicateACKsHashMap = new HashMap<>();

    private double devRTT;

    private double SRTT;

    public double RTO = 3;

    public String windowSizeHistory = "Time,WindowSize\n";


    public SelectiveRepeatProtocol(IPHost host, int packetNbr, double packetLossProbability) {
        this.host = host;
        this.buffer = new Packet[packetNbr];
        this.timers = new Timer[packetNbr];
        this.packetLossProbability = packetLossProbability;
        host.getIPLayer().addListener(IP_PROTO_SELECTIVE_REPEAT, this);
    }

    public SelectiveRepeatProtocol(IPHost host, int packetNbr, AppReceiver receiver, double packetLossProbability) {
        this.host = host;
        this.buffer = new Packet[packetNbr];
        this.bufferSize = buffer.length;
        this.packetLossProbability = packetLossProbability;
        host.getIPLayer().addListener(IP_PROTO_SELECTIVE_REPEAT, this);
        this.receiver = receiver;
    }

    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        Packet packet = (Packet) datagram.getPayload();
        if (packet.isAck) {
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
                timeout(datagram.src,seqNumber);
                cwnd /= 2;
                ssthresh = cwnd;
                Logger.logLoss(seqNumber, cwnd, ssthresh);
                windowSizeHistory += host.getNetwork().getScheduler().getCurrentTime() + "," + cwnd + "\n";
            }

            double oldCwnd = cwnd;
            double offset;
            if (cwnd < ssthresh) cwnd++;  // Slow start (add one packet to the window)
            else cwnd += 1 / cwnd;         // additive increase : (Add 1/cwnd to the window, the value will be round in sendData() )
            Logger.logCongestionWindowSizeChanged(oldCwnd, cwnd);
            windowSizeHistory += host.getNetwork().getScheduler().getCurrentTime() + "," + cwnd + "\n";

            offset = cwnd - oldCwnd;
            if (!buffer[packet.seqNumber].isAck) {
                buffer[packet.seqNumber].isAck = true;
                if (sendBase == packet.seqNumber && sendBase != bufferSize - 1) {
                    while (sendBase < bufferSize && buffer[sendBase].isAck) {
                        sendBase++;
                        offset++;
                    }
                }
                Logger.saveWindowChanges(windowSizeHistory);
            }

            for (int i =0;i<=offset+1 && next_seq_num < bufferSize;i++){
                sendData(buffer[next_seq_num],datagram.src);
            }

        } else {
            // Receiver side
            Logger.logPacketReceived(packet);
            sendAck(datagram);
            if (buffer[packet.seqNumber] == null) {
                buffer[packet.seqNumber] = packet;
                if (packet.seqNumber == recvBase) {
                    receiver.receiveData(buffer[recvBase].data, datagram.src);
                    recvBase++;
                    while (recvBase < bufferSize && buffer[recvBase] != null) {
                        receiver.receiveData(buffer[recvBase].data, datagram.src);
                        recvBase++;
                    }
                }
            }

        }
    }

    public void sendData(int data, IPAddress dst) throws Exception {
        Packet packet = new Packet(data, bufferSize);
        buffer[bufferSize] = packet;
        this.sendData(packet, dst);
        bufferSize++;
    }

    private void sendData(Packet data, IPAddress dst) throws Exception {
        if (next_seq_num < sendBase + Math.round(cwnd)) {
            Logger.logPacketSent(data);
            if (random.nextDouble() > packetLossProbability) {
                host.getIPLayer().send(IPAddress.ANY, dst, IP_PROTO_SELECTIVE_REPEAT, data);
            } else {
                Logger.logPacketLoss(data);
            }
            //System.out.println(RTO);
            timers[next_seq_num] = new Timer(host.getNetwork().getScheduler(), RTO, dst, next_seq_num);
            timers[next_seq_num].start();
            next_seq_num++;
        }
    }

    public void sendAck(Datagram datagram) throws Exception {
        Packet packet = new Packet(((Packet) datagram.getPayload()).seqNumber);

        Logger.logAckSent(packet);
        if (random.nextDouble() > packetLossProbability) {
            host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_SELECTIVE_REPEAT, packet);
        } else {
            Logger.logAckLoss(packet);
        }
    }

    public void timeout(IPAddress dst, int seqNumber) throws Exception {
        setRTO(timers[seqNumber]);
        //System.out.println(RTO);
        timers[seqNumber] = new Timer(host.getNetwork().getScheduler(), RTO, dst, seqNumber);
        timers[seqNumber].start();
        host.getIPLayer().send(IPAddress.ANY, dst, IP_PROTO_SELECTIVE_REPEAT, buffer[seqNumber]);

        double oldCwnd = cwnd;
        cwnd = 1;
        ssthresh = oldCwnd/2;
        Logger.logLoss(seqNumber,cwnd, ssthresh);
        windowSizeHistory += host.getNetwork().getScheduler().getCurrentTime() + "," + cwnd + "\n";
    }


    public double getSRTT(Timer timer) {
        double alpha = 0.125;
        if (SRTT > 0) {
            SRTT = (1 - alpha)* SRTT + alpha * timer.getRTT();
        } else {
            SRTT = timer.getRTT();
        }
        return SRTT;
    }

    public double getDevRTT(Timer timer) {
        double beta = 0.25;
        if (devRTT > 0) {
            devRTT = (1-beta)*devRTT + beta * Math.abs((SRTT-timer.getRTT()));
        } else {
            devRTT = timer.getRTT()/2;
        }
        return devRTT;
    }

    public void setRTO(Timer timer){
        RTO = getSRTT(timer) + 4 * getDevRTT(timer);
    }

    private class Timer extends AbstractTimer {
        private final IPAddress dst;

        private final int seqNumber;

        private double startingTime;

        private double stopTime;

        public Timer(AbstractScheduler scheduler, double interval, IPAddress dst, int seqNumber) {
            super(scheduler, interval, false);
            this.dst = dst;
            this.seqNumber = seqNumber;
        }

        protected void run() throws Exception {
            if (!buffer[seqNumber].isAck) {
                stop();
                timeout(dst, seqNumber);
            }
        }

        @Override
        public void start() {
            super.start();
            startingTime = scheduler.getCurrentTime();
        }

        @Override
        public void stop() {
            super.stop();
            stopTime = scheduler.getCurrentTime();
        }

        public double getRTT() {
            //System.out.println("Timer RTT : "+(stopTime - startingTime));
            return stopTime - startingTime;
        }
    }
}
