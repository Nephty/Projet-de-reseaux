package reso.examples.selectiveRepeat.logger;

import reso.common.Host;
import reso.examples.selectiveRepeat.AppReceiver;
import reso.examples.selectiveRepeat.AppSender;
import reso.examples.selectiveRepeat.Packet;
import reso.ip.IPAddress;
import reso.ip.IPLayer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static class LogFormatter {
        public static String logFormat(String message) {
            return "[" + LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss")) + "] <> " + message;
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

    public static void logAppReceiverLaunched(AppReceiver appReceiver) {
        log(String.format("App receiver launched with host %s.", appReceiver.getHostName()));
    }

    public static void logAppReceiverStopped(AppReceiver appReceiver) {
        log(String.format("App receiver with host %s stopped.", appReceiver.getHostName()));
    }

    public static void logAppSenderLaunched(AppSender appSender) {
        log(String.format("App sender launched with host %s and with IP %s.", appSender.getHostName(), appSender.getIPAddressAsString()));
    }

    public static void logAppSenderStopped(AppSender appSender) {
        log(String.format("App sender with host %s and with IP %s stopped.", appSender.getHostName(), appSender.getIPAddressAsString()));
    }

    public static void logPacketSent(Packet packet) {
        log(String.format("Sent packet with sequence number %d.", packet.getSeqNumber()));
    }

    public static void logPacketReceived(Packet packet) {
        log(String.format("Received packet with sequence number %d.", packet.getSeqNumber()));
    }

    public static void logAckSent(Packet packet) {
        log(String.format("ACK sent for packet with sequence number %d.", packet.getSeqNumber()));
    }

    public static void logAckReceived(Packet packet) {
        log(String.format("Received ACK for packet with sequence number %d.", packet.getSeqNumber()));
    }

    public static void logPacketLoss(Packet packet) {
        log(String.format("PACKET LOSS : packet with sequence number %d was lost.", packet.getSeqNumber()));
    }

    public static void logAckLoss(Packet packet) {
        log(String.format("ACK LOSS : ACK for packet with sequence number %d was lost.", packet.getSeqNumber()));
    }
}
