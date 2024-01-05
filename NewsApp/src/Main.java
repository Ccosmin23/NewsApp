import java.net.SocketException;
import java.net.UnknownHostException;

import service.BrokerService;
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
            BrokerService.shared.start();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static void startAsALogger() {

    }
}
