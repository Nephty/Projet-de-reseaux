package reso.examples.selectiveRepeat;

import reso.common.AbstractTimer;
import reso.ip.*;
import reso.scheduler.AbstractScheduler;

import java.util.ArrayList;
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
            System.out.println("Ack received "+packet.seqNumber);
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
        } else {
            // Receiver side
            System.out.println("PKT received "+packet.seqNumber);
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
        if (next_seq_num < sendBase + WINDOW_SIZE) {
            if (random.nextDouble() > packetLossProbability) {
                System.out.println("-- SENDING pkt n°" + next_seq_num);
                host.getIPLayer().send(IPAddress.ANY, dst, IP_PROTO_SELECTIVE_REPEAT, data);
            } else {
                System.out.println("===== PACKET LOSS "+next_seq_num+" ========");
            }
            Timer tmpTimer = new Timer(host.getNetwork().getScheduler(),3,dst,next_seq_num);
            tmpTimer.start();
            next_seq_num++;
        }
    }

    public void sendAck(Datagram datagram) throws Exception {
        Packet packet = new Packet(((Packet) datagram.getPayload()).seqNumber);

        if (random.nextDouble() > packetLossProbability){
            System.out.println("---- ACK pkt n°" + packet.seqNumber);
            host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_SELECTIVE_REPEAT, packet);
        } else {
            System.out.println("===== ACK LOSS "+packet.seqNumber+" ========");
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
