import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Enumeration;
import java.util.logging.Logger;

import architecture.RingManager;
import service.BrokerService;
import service.LoggerService;
import service.PublisherService;
import service.SubscriberService;
import ui.MainView;
import utils.StringUtils;

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
            case "logger_connection":
                startAsALoggerConnection();
                break;
            case "ring_manager":
                startAsARingManager();
                break;
            default: {
                MainView.showMenuMessages();
                break;
            }
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
            new BrokerService().start();
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

    public static void startAsALoggerConnection() {
        try {
            LoggerService.shared.start2();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void startAsARingManager() {
        try {
            RingManager.shared.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
