package reso.examples.selectiveRepeat;

import reso.common.AbstractTimer;
import reso.ip.*;
import reso.scheduler.AbstractScheduler;

import java.util.ArrayList;

public class SelectiveRepeatProtocol implements IPInterfaceListener {

    public static final int IP_PROTO_SELECTIVE_REPEAT = Datagram.allocateProtocolNumber("SELECTIVE_REPEAT");

    private final IPHost host;

    public int next_seq_num = 0;

    public int sendBase = 0;

    public int recvBase = 0;

    public double WINDOW_SIZE = 1;

    //private Packet[] packetList;

    private int bufferSize = 0;

    private final ArrayList<Packet> packetList = new ArrayList<>();


    public SelectiveRepeatProtocol(IPHost host) {
        this.host = host;
        host.getIPLayer().addListener(IP_PROTO_SELECTIVE_REPEAT,this);
    }

    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        Packet packet = (Packet) datagram.getPayload();
        if (packet.isAck){
            // Sender side
            System.out.println("Ack received "+packet.seqNumber);
            if (!packetList.get(packet.seqNumber).isAck) {
                packetList.get(packet.seqNumber).isAck = true;
                if (sendBase == packet.seqNumber && sendBase != bufferSize-1) {
                    while(packetList.get(++sendBase).isAck){
                        sendBase++;
                    }
                    WINDOW_SIZE++;
                    if (next_seq_num != packetList.size()){
                        sendData(packetList.get(next_seq_num),datagram.src);
                        sendData(packetList.get(next_seq_num),datagram.src);
                    }
                }
            }
        } else {
            // Receiver side
            System.out.println("PKT received "+packet.seqNumber);
            packetList.add(packet.seqNumber,packet);
            if (packet.seqNumber == recvBase) {
                System.out.println("SELECTIVE-REPEAT (" + (int) (host.getNetwork().getScheduler().getCurrentTime() * 1000) + "ms)" +
                        " host=" + host.name + ", dgram.src=" + datagram.src + ", dgram.dst=" +
                        datagram.dst + ", iif=" + src + ", counter=" + packet.data);
                recvBase++;
            }
            sendAck(datagram);

        }
    }

    public void sendData(int data, IPAddress dst) throws Exception {
        Packet packet = new Packet(data,bufferSize);
        packetList.add(bufferSize, packet);
        this.sendData(packet,dst);
        bufferSize++;
    }

    private void sendData(Packet data, IPAddress dst) throws Exception {
        if (next_seq_num < sendBase + WINDOW_SIZE) {

            System.out.println("-- SENDING pkt n°" + next_seq_num);
            host.getIPLayer().send(IPAddress.ANY, dst, IP_PROTO_SELECTIVE_REPEAT, data);

            next_seq_num++;
        }
    }

    public void sendAck(Datagram datagram) throws Exception {
        Packet packet = new Packet(((Packet) datagram.getPayload()).seqNumber);
        System.out.println("---- ACK pkt n°" + packet.seqNumber);
        host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_SELECTIVE_REPEAT, packet);
    }
    
    private class Timer extends AbstractTimer {
        private IPAddress dst;
        
        public Timer(AbstractScheduler scheduler, double interval, IPAddress dst) {
            super(scheduler,interval,false);
            this.dst = dst;
        }
        
        protected  void run() throws Exception {
            // TODO: 7/05/22 timeout here  
        }
    }
}
