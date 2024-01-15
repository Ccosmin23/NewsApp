package service;

import utils.InetAddressUtils;
import utils.StringUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Enumeration;

public final class LoggerService {
    public static LoggerService shared = new LoggerService();

    ServerSocket serverSocket;
    Socket clientSocket;
    String loggerIpAddress = "192.168.30.13";
    int loggerPort = 9700;
    Boolean programIsRunning = false;

    public void start() throws IOException, ClassNotFoundException {
        serverSocket = new ServerSocket(loggerPort);
        programIsRunning = true;

        System.out.println("=======================================================================================");
        System.out.println("\tSalutare! Eu sunt sistemul de log-uri cu adresa IP " + boldedHostAddress);
        System.out.println("\t\tca sa inchizi executia apasa CTRL+C apoi ENTER");
        System.out.println("=======================================================================================\n");

        while (true) {
            clientSocket = serverSocket.accept();
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());

            //primeste si afiseaza mesajul
            String message = (String) ois.readObject();

            if (message != null && message != "") {
                System.out.println(message);
            }
        }
    }

    public void sendLogToLogger(String logMessage) {
        try (Socket socket = new Socket(loggerIpAddress, loggerPort);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
            oos.writeObject(logMessage);
        } catch (IOException e) {
            //daca nu ramane commentat codul de mai jos o sa avem print-uri cand logger-ul nu e rulat
//            System.out.println("Nu putem loga mesajul datorita: " + e.getMessage());
        }
    }

    private String boldedHostAddress; {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();

                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress()) {
                        System.out.println("Local IP Address: " + inetAddress.getHostAddress());
                        boldedHostAddress = StringUtils.applyBoldTo(inetAddress.getHostAddress(), false);
                    }
                }
            }

        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }
}

