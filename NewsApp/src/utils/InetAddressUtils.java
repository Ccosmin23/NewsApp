package utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class InetAddressUtils {
    public static InetAddress getLocalAddress() throws UnknownHostException {
        return InetAddress.getLocalHost();
    }

    public static String boldedHostAddress() {
        return StringUtils.applyBoldTo(hostAddress().getHostAddress(), false);
    }

    public static InetAddress hostAddress() {
        InetAddress hostAddress = null;

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();

                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();

                    if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress()) {
                        hostAddress = inetAddress;
                    }
                }
            }

        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        return hostAddress;
    }
}
