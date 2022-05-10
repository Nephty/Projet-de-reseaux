package reso.examples.selectiveRepeat;

import reso.common.AbstractTimer;
import reso.examples.selectiveRepeat.logger.Logger;
import reso.ip.*;
import reso.scheduler.AbstractScheduler;

import java.util.HashMap;
import java.util.Random;

public class SelectiveRepeatProtocol implements IPInterfaceListener {

    public static final int IP_PROTO_SELECTIVE_REPEAT = Datagram.allocateProtocolNumber("SELECTIVE_REPEAT");

    public static Random random = new Random();

    private AppReceiver receiver;

    private final IPHost host;

    public int next_seq_num = 0;

    public int sendBase = 0;

    public int recvBase = 0;

    public double WINDOW_SIZE = 1;

    private final Packet[] buffer;

    private int bufferSize = 0;

    private final double packetLossProbability;

    private int ssthresh = Integer.MAX_VALUE;

    private int cwnd = 1;

    private int increaseCwndBy = 0;  // how many MSS to add to cwnd

    private HashMap<Integer, Integer> duplicateACKsHashMap = new HashMap<>();



    public SelectiveRepeatProtocol(IPHost host,int packetNbr,double packetLossProbability) {
        this.host = host;
        this.buffer = new Packet[packetNbr];
        this.packetLossProbability = packetLossProbability;
        host.getIPLayer().addListener(IP_PROTO_SELECTIVE_REPEAT,this);
    }

    public SelectiveRepeatProtocol(IPHost host,int packetNbr,AppReceiver receiver,double packetLossProbability){
        this.host = host;
        this.buffer = new Packet[packetNbr];
        this.bufferSize = buffer.length;
        this.packetLossProbability =packetLossProbability;
        host.getIPLayer().addListener(IP_PROTO_SELECTIVE_REPEAT,this);
        this.receiver = receiver;
    }

    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        Packet packet = (Packet) datagram.getPayload();
        if (packet.isAck){
            // Sender side
            Logger.logAckReceived(packet);
            int seqNumber = packet.getSeqNumber();
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
                cwnd /= 2;
                ssthresh = cwnd;
                Logger.logLoss(packet, cwnd, ssthresh);
            } else {
                int oldCwnd = cwnd;
                if (cwnd < ssthresh) increaseCwndBy++;  // Slow start : allows exponential growth (add 1, then 2, then 3, then 4...)
                else increaseCwndBy = 1; // Fast recovery : linear growth
                cwnd += increaseCwndBy;
                Logger.logCongestionWindowSizeChanged(oldCwnd, cwnd);

                // TODO : should the code below be put in this else ? or should we execute it even in case of a loss ?
            }
            if (!buffer[packet.seqNumber].isAck) {
                buffer[packet.seqNumber].isAck = true;
                if (sendBase == packet.seqNumber && sendBase != bufferSize-1) {
                    while(sendBase < bufferSize && buffer[sendBase].isAck){
                        sendBase++;
                    }
                    WINDOW_SIZE++;
                    int offset = 1;
                    for (int i =0;i<=offset+1 && next_seq_num < bufferSize;i++){
                        sendData(buffer[next_seq_num],datagram.src);
                    }
                }
            }
            // TODO : when timeout, ssthresh = cwnd / 2; cwnd = 1;
        } else {
            // Receiver side
            Logger.logPacketReceived(packet);
            sendAck(datagram);
            if (buffer[packet.seqNumber] == null) {
                buffer[packet.seqNumber]=packet;
                if (packet.seqNumber == recvBase) {
                    receiver.receiveData(buffer[recvBase].data,datagram.src);
                    recvBase++;
                    while(recvBase < bufferSize && buffer[recvBase]!=null) {
                        receiver.receiveData(buffer[recvBase].data,datagram.src);
                        recvBase++;
                    }
                }
            }

        }
    }

    public void sendData(int data, IPAddress dst) throws Exception {
        Packet packet = new Packet(data,bufferSize);
        buffer[bufferSize] = packet;
        this.sendData(packet,dst);
        bufferSize++;
    }

    private void sendData(Packet data, IPAddress dst) throws Exception {
        //if (next_seq_num < sendBase + WINDOW_SIZE) {
        if (next_seq_num < cwnd) {
            Logger.logPacketSent(data);
            if (random.nextDouble() > packetLossProbability) {
                host.getIPLayer().send(IPAddress.ANY, dst, IP_PROTO_SELECTIVE_REPEAT, data);
            } else {
                Logger.logPacketLoss(data);
            }
            Timer tmpTimer = new Timer(host.getNetwork().getScheduler(),3,dst,next_seq_num);
            tmpTimer.start();
            next_seq_num++;
        }
    }

    public void sendAck(Datagram datagram) throws Exception {
        Packet packet = new Packet(((Packet) datagram.getPayload()).seqNumber);

        Logger.logAckSent(packet);
        if (random.nextDouble() > packetLossProbability){
            host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_SELECTIVE_REPEAT, packet);
        } else {
            Logger.logAckLoss(packet);
        }
    }

    public void timeout(IPAddress dst, int seqNumber) throws Exception {
        Timer tmpTimer = new Timer(host.getNetwork().getScheduler(),3,dst,seqNumber);
        tmpTimer.start();
        host.getIPLayer().send(IPAddress.ANY,dst, IP_PROTO_SELECTIVE_REPEAT,buffer[seqNumber]);
    }
    
    private class Timer extends AbstractTimer {
        private final IPAddress dst;

        private final int seqNumber;

        private double startingTime;

        private double stopTime;
        
        public Timer(AbstractScheduler scheduler, double interval, IPAddress dst,int seqNumber) {
            super(scheduler,interval,false);
            this.dst = dst;
            this.seqNumber = seqNumber;
        }
        
        protected  void run() throws Exception {
            if (!buffer[seqNumber].isAck){
                timeout(dst,seqNumber);
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
    }
}
