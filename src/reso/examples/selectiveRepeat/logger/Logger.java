package reso.examples.selectiveRepeat.logger;

import reso.common.Host;
import reso.examples.selectiveRepeat.AppReceiver;
import reso.examples.selectiveRepeat.AppSender;
import reso.examples.selectiveRepeat.Demo;
import reso.examples.selectiveRepeat.Packet;
import reso.ip.IPAddress;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Scanner;

public class Logger {
    private static class LogFormatter {
        private final static int NUMBER_OF_DIGITS = 6;

        public static String logFormat(String message) {
            int time = (int) Math.floor(Demo.scheduler.getCurrentTime() * 1000);
            StringBuilder bobTheBuilder = new StringBuilder();
            int j = Math.max(1, (int) Math.ceil(Math.log10(time)));
            bobTheBuilder.append("0".repeat(Math.max(0, NUMBER_OF_DIGITS - j)));
            bobTheBuilder.append(time);
            return "[" + bobTheBuilder + "] <> " + message;
        }
    }

    public static void initSelectiveRepeat() {
        System.out.println("  /$$$$$$            /$$                       /$$     /$$                             /$$$$$$$                                            /$$    \n" +
                " /$$__  $$          | $$                      | $$    |__/                            | $$__  $$                                          | $$    \n" +
                "| $$  \\__/  /$$$$$$ | $$  /$$$$$$   /$$$$$$$ /$$$$$$   /$$ /$$    /$$ /$$$$$$         | $$  \\ $$  /$$$$$$   /$$$$$$   /$$$$$$   /$$$$$$  /$$$$$$  \n" +
                "|  $$$$$$  /$$__  $$| $$ /$$__  $$ /$$_____/|_  $$_/  | $$|  $$  /$$//$$__  $$ /$$$$$$| $$$$$$$/ /$$__  $$ /$$__  $$ /$$__  $$ |____  $$|_  $$_/  \n" +
                " \\____  $$| $$$$$$$$| $$| $$$$$$$$| $$        | $$    | $$ \\  $$/$$/| $$$$$$$$|______/| $$__  $$| $$$$$$$$| $$  \\ $$| $$$$$$$$  /$$$$$$$  | $$    \n" +
                " /$$  \\ $$| $$_____/| $$| $$_____/| $$        | $$ /$$| $$  \\  $$$/ | $$_____/        | $$  \\ $$| $$_____/| $$  | $$| $$_____/ /$$__  $$  | $$ /$$\n" +
                "|  $$$$$$/|  $$$$$$$| $$|  $$$$$$$|  $$$$$$$  |  $$$$/| $$   \\  $/  |  $$$$$$$        | $$  | $$|  $$$$$$$| $$$$$$$/|  $$$$$$$|  $$$$$$$  |  $$$$/\n" +
                " \\______/  \\_______/|__/ \\_______/ \\_______/   \\___/  |__/    \\_/    \\_______/        |__/  |__/ \\_______/| $$____/  \\_______/ \\_______/   \\___/  \n" +
                "                                                                                                          | $$                                    \n" +
                "                                                                                                          | $$                                    \n" +
                "                                                                                                          |__/                                    ");
    }

    private static void log(String message) {
        System.out.println(LogFormatter.logFormat(message));
    }

    public static void logPacketSent(Packet packet) {
        log(String.format("SENT PACKET with sequence number %d.", packet.getSeqNumber()));
    }

    public static void logPacketReceived(Packet packet) {
        log(String.format("RECEIVED PACKET with sequence number %d.", packet.getSeqNumber()));
    }

    public static void logAckSent(Packet packet) {
        log(String.format("ACK SENT for packet with sequence number %d.", packet.getSeqNumber()));
    }

    public static void logAckReceived(Packet packet) {
        log(String.format("RECEIVED ACK for packet with sequence number %d.", packet.getSeqNumber()));
    }

    public static void logPacketLoss(Packet packet) {
        log(String.format("PACKET LOSS : packet with sequence number %d was lost.", packet.getSeqNumber()));
    }

    public static void logAckLoss(Packet packet) {
        log(String.format("ACK LOSS : ACK for packet with sequence number %d was lost.", packet.getSeqNumber()));
    }

    public static void logLoss(int seqNumber, double newCwnd, double newSst) {
        log(String.format("Timeout for packet number %d \n" +
                "    -> New congestion window size : %f.\n" +
                "    -> New slow start threshold   : %f.", seqNumber, newCwnd, newSst));
    }

    public static void logCongestionWindowSizeChanged(double oldCwnd, double newCwnd) {
        if (newCwnd - oldCwnd != 1 && oldCwnd != 1) {
            // Fast recovery : newCwnd = oldCwnd + 1/oldCwnd
            log(String.format("Congestion window size changed from %f MSS to %f MSS (current mode : additive increase).", oldCwnd, newCwnd));
        } else {
            // Slow start : newCwnd = oldCwnd++
            log(String.format("Congestion window size changed from %f MSS to %f MSS (current mode : slow start).", oldCwnd, newCwnd));
        }
    }

    public static void packetReceived(int data, IPAddress src, IPAddress dst,Host host) {
        log("=====> SELECTIVE-REPEAT (" + (int) (host.getNetwork().getScheduler().getCurrentTime() * 1000) + "ms)" +
                " host=" + host.name + ", dgram.src=" + src   + ", dgram.dst="+dst +", counter=" + data +"\n");
    }

    public static void saveWindowChanges(String windowHistory) throws Exception {
        BufferedWriter fw = new BufferedWriter(new FileWriter("WindowSize.csv"));
        fw.write(windowHistory);
        fw.close();
    }

    public static int askPacketNbr() {
        Scanner scanner = new Scanner(System.in);
        int packetNbr;
        do {
            System.out.print("How many packets would you like to send ? (1+) >> ");
            while (!scanner.hasNextInt()) {
                System.out.println("You need to enter an integer");
                System.out.print("How many packets would you like to send ? (1+) >> ");
                scanner.next();
            }
            packetNbr = scanner.nextInt();
        } while (packetNbr < 1);
        return packetNbr;
    }

    public static double askMissingRate() {
        Scanner scanner = new Scanner(System.in);
        double missingRate;
        do {
            System.out.print("Percentage of chance to loose a packet ? (0-1) >> ");
            while (!scanner.hasNextDouble()) {
                System.out.println("You need to enter a double");
                System.out.print("Percentage of chance to loose a packet ? (0-1) >> ");
                scanner.next();
            }
            missingRate = scanner.nextDouble();
        } while (missingRate < 0 || missingRate >= 1);
        return missingRate;
    }

    public static int askBitRate() {
        Scanner scanner = new Scanner(System.in);
        int bitRate;
        do {
            System.out.print("Bit rate of the link ? (1+) >> ");
            while (!scanner.hasNextInt()) {
                System.out.println("You need to enter an integer");
                System.out.print("Bit rate of the link ? (1+) >> ");
                scanner.next();
            }
            bitRate = scanner.nextInt();
        } while (bitRate < 1);
        return bitRate;
    }

    public static int askLinkLength() {
        Scanner scanner = new Scanner(System.in);
        int length;
        do {
            System.out.print("Length of the link ? (km) >> ");
            while (!scanner.hasNextInt()) {
                System.out.println("You need to enter an integer");
                System.out.print("Length of the link ? (km) >> ");
                scanner.next();
            }
            length = scanner.nextInt();
        } while (length < 1);
        return length*1000;
    }
}
