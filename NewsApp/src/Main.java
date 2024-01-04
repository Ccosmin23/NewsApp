import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import model.broker.Broker;
import model.publisher.Publisher;
import model.subscriber.Subscriber;
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
            default: {
                MainView.showMenuMessages();
                break;
            }
        }
    }

    public static void startAsAPublisher() {
        Publisher publisher = new Publisher();

        try {
            publisher.start();
        } catch (UnknownHostException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void startAsASubscriber() {
        Subscriber subscriber = new Subscriber();

        try {
            subscriber.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static void startAsABroker() {
        ArrayList<InetAddress> inetAddressList = new ArrayList<>();

        try {
            inetAddressList.add(InetAddress.getByName("192.168.30.4"));
            inetAddressList.add(InetAddress.getByName("192.168.30.7"));
            inetAddressList.add(InetAddress.getByName("192.168.30.9"));
            inetAddressList.add(InetAddress.getByName("192.168.30.10"));
            inetAddressList.add(InetAddress.getByName("192.168.30.12"));

            Broker broker = new Broker(inetAddressList);
            broker.start();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
