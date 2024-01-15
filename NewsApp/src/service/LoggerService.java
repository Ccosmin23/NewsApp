package service;

import architecture.RingManager;
import com.sun.tools.javac.Main;
import utils.InetAddressUtils;
import utils.StringUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Enumeration;

public final class LoggerService {
    public static LoggerService shared = new LoggerService();

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private int loggerPort = 9700;

    public void start() throws IOException, ClassNotFoundException {
        serverSocket = new ServerSocket(loggerPort);

        System.out.println("=======================================================================================");
        System.out.println("\tSalutare! Eu sunt sistemul de log-uri cu adresa IP " + RingManager.shared.boldedHostAddress());
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
        try (Socket socket = new Socket(RingManager.shared.hostAddress(), loggerPort);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
            oos.writeObject(logMessage);
        } catch (IOException e) {
            //daca nu ramane commentat codul de mai jos o sa avem print-uri cand logger-ul nu e rulat
//            System.out.println("Nu putem loga mesajul datorita: " + e.getMessage());
        }
    }

    //================================== LOGGER CONNECTION =====================================================
    private ServerSocket serverSocket2;
    private Socket clientSocket2;

    public void start2() throws IOException, ClassNotFoundException {
        serverSocket2 = new ServerSocket(loggerPort);

        System.out.println("=======================================================================================");
        System.out.println("\tSalutare! Eu sunt sistemul de log-uri PENTRU CONEXIUNE cu adresa IP " + RingManager.shared.boldedHostAddress());
        System.out.println("\t\tca sa inchizi executia apasa CTRL+C apoi ENTER");
        System.out.println("=======================================================================================\n");

        while (true) {
            clientSocket2 = serverSocket2.accept();
            ObjectInputStream ois = new ObjectInputStream(clientSocket2.getInputStream());

            //primeste si afiseaza mesajul
            String message = (String) ois.readObject();

            if (message != null && message != "") {
                System.out.println(message);
            }
        }
    }

    public void sendLogToLogger2(String logMessage) {
        try (Socket socket = new Socket(RingManager.shared.hostAddress(), loggerPort);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
            oos.writeObject(logMessage);
        } catch (IOException e) {
            //daca nu ramane commentat codul de mai jos o sa avem print-uri cand logger-ul nu e rulat
//            System.out.println("Nu putem loga mesajul datorita: " + e.getMessage());
        }
    }
}

