package utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class InetAddressUtils {
    public static InetAddress getLocalAddress() throws UnknownHostException {
        return InetAddress.getLocalHost();
    }
}
