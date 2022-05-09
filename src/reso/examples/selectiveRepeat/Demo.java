package reso.examples.selectiveRepeat;

import reso.common.Link;
import reso.common.Network;
import reso.ethernet.EthernetAddress;
import reso.ethernet.EthernetInterface;
import reso.examples.selectiveRepeat.logger.Logger;
import reso.examples.static_routing.AppSniffer;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.scheduler.AbstractScheduler;
import reso.scheduler.Scheduler;
import reso.utilities.NetworkBuilder;

import java.util.Scanner;

public class Demo {

    /* Enable or disable packet capture (can be used to observe ARP messages) */
    private static final boolean ENABLE_SNIFFER = false;
    public static AbstractScheduler scheduler;

    public static void main(String[] args) {
        // Params of the application :
        Logger.initSelectiveRepeat();

        Scanner scanner = new Scanner(System.in);
        int packetNbr;
        do {
            System.out.print("How many packets would you like to send ? (1+) >> ");
            packetNbr = scanner.nextInt();
        } while (packetNbr < 1);
        double missingRate;
        do {
            System.out.print("Percentage of chance to loose a packet ? (0-1) >> ");
            missingRate = scanner.nextDouble();
        } while (missingRate > 1);


        scheduler = new Scheduler();
        Network network = new Network(scheduler);
        try {
            final EthernetAddress MAC_ADDR1 = EthernetAddress.getByAddress(0x00, 0x26, 0xbb, 0x4e, 0xfc, 0x28);
            final EthernetAddress MAC_ADDR2 = EthernetAddress.getByAddress(0x00, 0x26, 0xbb, 0x4e, 0xfc, 0x29);
            final IPAddress IP_ADDR1 = IPAddress.getByAddress(192, 168, 0, 1);
            final IPAddress IP_ADDR2 = IPAddress.getByAddress(192, 168, 0, 2);

            IPHost host1 = NetworkBuilder.createHost(network, "H1", IP_ADDR1, MAC_ADDR1);
            host1.getIPLayer().addRoute(IP_ADDR2, "eth0");
            if (ENABLE_SNIFFER)
                host1.addApplication(new AppSniffer(host1, new String[]{"eth0"}));
            host1.addApplication(new AppSender(host1, IP_ADDR2,packetNbr,missingRate));

            IPHost host2 = NetworkBuilder.createHost(network, "H2", IP_ADDR2, MAC_ADDR2);
            host2.getIPLayer().addRoute(IP_ADDR1, "eth0");
            host2.addApplication(new AppReceiver(host2,packetNbr,missingRate));

            EthernetInterface h1_eth0 = (EthernetInterface) host1.getInterfaceByName("eth0");
            EthernetInterface h2_eth0 = (EthernetInterface) host2.getInterfaceByName("eth0");

            // Connect both interfaces with a 5000km long link
            new Link<>(h1_eth0, h2_eth0, 5000000, 100000);

            host1.start();
            host2.start();

            scheduler.run();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }
    }

}
