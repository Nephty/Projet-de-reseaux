package reso.examples.selectiveRepeat.logger;

import reso.common.Host;
import reso.examples.selectiveRepeat.Demo;
import reso.examples.selectiveRepeat.Packet;
import reso.ip.IPAddress;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Scanner;

/**
 * Utility class that logs data to the console, ask for user input and log data in files.
 * Primary roles :
 *  - log data changes such as window size
 *  - log packet and ack sending, receiving and loss
 *  - ask for user input (link length, bitrate...)
 *  - log data in a .csv file
 */
public class Logger {
    /**
     * Inner class that formats a string as a log message for printing to the console.
     * Format :
     *  [xxxxxx] <> ............
     * where xxxxxx is a time representation and ............ is the message body.
     */
    private static class LogFormatter {
        /**
         * Number of digits to use to represent time.
         * Example with 3 digits and 5 different time stamps :
         *  021   837   271   008   201   042
         * The same time stamps, with 5 digits :
         * 00021 00837 00271 00008 00201 00042
         */
        private final static int NUMBER_OF_DIGITS = 6;

        /**
         * Method that will format the message according to the following format :
         *  [xxxxxx] <> ............
         * where xxxxxx is a time representation and ............ is the message body.
         * @param message The message body.
         * @return The formatted message.
         */
        public static String logFormat(String message) {
            int time = (int) Math.floor(Demo.scheduler.getCurrentTime() * 1000);
            StringBuilder bobTheBuilder = new StringBuilder();
            int j = Math.max(1, (int) Math.ceil(Math.log10(time)));
            bobTheBuilder.append("0".repeat(Math.max(0, NUMBER_OF_DIGITS - j)));
            bobTheBuilder.append(time);
            return "[" + bobTheBuilder + "] <> " + message;
        }
    }

    /**
     * Because we thought it looked cool.
     */
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

    /**
     * Main method to log a message to the console. The given message should be the message body, since this method
     * will automatically format the output.
     * @param message The message body.
     */
    private static void log(String message) {
        System.out.println(LogFormatter.logFormat(message));
    }

    /**
     * Log the sending of a packet using its sequence number.
     * @param packet The packet sent.
     */
    public static void logPacketSent(Packet packet) {
        log(String.format("SENT PACKET with sequence number %d.", packet.getSeqNumber()));
    }

    /**
     * Log the reception of a packet using its sequence number.
     * @param packet The received packet.
     */
    public static void logPacketReceived(Packet packet) {
        log(String.format("RECEIVED PACKET with sequence number %d.", packet.getSeqNumber()));
    }

    /**
     * Log the sending of an ACK using the sequence number of the ACKed packet.
     * @param packet The ACK sent.
     */
    public static void logAckSent(Packet packet) {
        log(String.format("ACK SENT for packet with sequence number %d.", packet.getSeqNumber()));
    }

    /**
     * Log the reception of an ACK using the sequence number of the ACKed packet.
     * @param packet The received ACK.
     */
    public static void logAckReceived(Packet packet) {
        log(String.format("RECEIVED ACK for packet with sequence number %d.", packet.getSeqNumber()));
    }

    /**
     * Log a packet loss using its sequence number.
     * @param packet The lost packet.
     */
    public static void logPacketLoss(Packet packet) {
        log(String.format("PACKET LOSS : packet with sequence number %d was lost.", packet.getSeqNumber()));
    }

    /**
     * Log an ACK loss using its sequence number.
     * @param packet The lost ACK.
     */
    public static void logAckLoss(Packet packet) {
        log(String.format("ACK LOSS : ACK for packet with sequence number %d was lost.", packet.getSeqNumber()));
    }

    /**
     * Log a timeout and show the new congestion window size and the new slow start threshold.
     * @param seqNumber The sequence number of the timed out packet.
     * @param newCwnd The new size of the congestion window.
     * @param newSst The new slow start threshold.
     */
    public static void logLoss(int seqNumber, double newCwnd, double newSst) {
        log(String.format("Timeout for packet number %d \n" +
                "    -> New congestion window size : %f.\n" +
                "    -> New slow start threshold   : %f.", seqNumber, newCwnd, newSst));
    }

    /**
     * Log a change in size of the congestion window and indicates whether we are in additive increase or
     * slow start mode.
     * @param oldCwnd The previous size of the congestion window.
     * @param newCwnd The new size of the congestion window.
     */
    public static void logCongestionWindowSizeChanged(double oldCwnd, double newCwnd) {
        if (newCwnd - oldCwnd != 1 && oldCwnd != 1) {
            // Fast recovery : newCwnd = oldCwnd + 1/oldCwnd
            log(String.format("Congestion window size changed from %f MSS to %f MSS (current mode : additive increase).", oldCwnd, newCwnd));
        } else {
            // Slow start : newCwnd = oldCwnd++
            log(String.format("Congestion window size changed from %f MSS to %f MSS (current mode : slow start).", oldCwnd, newCwnd));
        }
    }

    /**
     * Log a packet received by the receiver part of the Selective Repeat Protocol.
     * // TODO : cyril can you do the arguments pls :pray:
     * @param data
     * @param src
     * @param dst
     * @param host
     */
    public static void packetReceived(int data, IPAddress src, IPAddress dst,Host host) {
        log("=====> SELECTIVE-REPEAT (" + (int) (host.getNetwork().getScheduler().getCurrentTime() * 1000) + "ms)" +
                " host=" + host.name + ", dgram.src=" + src   + ", dgram.dst="+dst +", value=" + data +"\n");
    }

    /**
     * Register a size change of the congestion window in a .csv file for plotting.
     * @param windowHistory The content to write to the .csv file.
     * @throws Exception When an exception occurs because of the BufferedWriter.
     */
    public static void saveWindowChanges(String windowHistory) throws Exception {
        BufferedWriter fw = new BufferedWriter(new FileWriter("WindowSize.csv"));
        fw.write(windowHistory);
        fw.close();
    }

    /**
     * Ask for user input : how many packets should be sent ?
     * @return The number of packets that will be sent.
     */
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

    /**
     * Ask for user input : what should be the loss rate ?
     * @return The loss rate that will be simulated.
     */
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

    /**
     * Ask for user input : what should be the bitrate of the link ?
     * @return The bitrate that will be simulated.
     */
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

    /**
     * Ask for user input : what should be the length of the link ?
     * @return The length that will be simulated.
     */
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
