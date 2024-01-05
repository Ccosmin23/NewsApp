import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import service.BrokerService;
import service.LoggerService;
import service.PublisherService;
import service.SubscriberService;
import ui.MainView;

public class Main {
    public static void main(String[] args) {

        if (args.length == 0) {
            MainView.showMenuMessages();
            return;
        }

        startAppWith(args);
    }

    public static void startAppWith(String[] args) {
        switch (args[0]) {
            case "publisher":
                startAsAPublisher();
                break;
            case "subscriber":
                startAsASubscriber();
                break;
            case "broker":
                startAsABroker();
                break;
            case "logger":
                startAsALogger();
                break;
            case "test":
                sendLogToLogger();
                break;
            default: {
                MainView.showMenuMessages();
                break;
            }
        }
    }

    public static void sendLogToLogger() {
        String loggerIpAddress = "192.168.30.13"; // Replace with the actual IP address of the LoggerService container
        int loggerPort = 9700; // Choose the port for the LoggerService

        try (Socket socket = new Socket(loggerIpAddress, loggerPort);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {

            // Send a trigger message to start LoggerService
            oos.writeObject("start");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startAsAPublisher() {
        try {
            PublisherService.shared.start();
        } catch (UnknownHostException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void startAsASubscriber() {
        try {
            SubscriberService.shared.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static void startAsABroker() {
        try {
            BrokerService.shared.start();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static void startAsALogger() {
        try {
            LoggerService.shared.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
