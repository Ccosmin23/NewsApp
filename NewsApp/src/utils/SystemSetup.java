package utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class SystemSetup {
//    public static InetAddress ringManagerAddress = InetAddress.getByName("192.168.30.4");
//
//    static {
//        try {
//            ringManagerAddress = InetAddress.getByName("192.168.30.4");
//        } catch (UnknownHostException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public static String ringManagerIpAddress = "192.168.30.4";

    public static int port = 9700;
}
