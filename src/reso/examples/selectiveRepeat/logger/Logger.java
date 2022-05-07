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
}
