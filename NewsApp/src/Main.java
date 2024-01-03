import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import model.broker.Broker;
import model.publisher.Publisher;
import model.subscriber.Subscriber;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            showMenuMessages();
            return;
        }

        startAppWith(args);
    }

    public static void startAppWith(String[] args) {
        switch (args[0]) {
            case "publisher":
                startAsAPublisher();
            case "subscriber":
                startAsASubscriber();
            case "broker":
                startAsABroker();
            default: {
                showMenuMessages();
                break;
            }
        }
    }

    public static void showMenuMessages() {
        System.out.println("We cannot start the app for you, because you should enter an argument as:" +
                "\t'publisher', 'subscribe' or 'broker'\n\n" +
                "\tpublisher will start the execution as a publisher" +
                "\tsubscriber will start the execution as a subscriber" +
                "\tbroker -> will start the execution as a broker");
    }

    public static void startAsAPublisher() {
        Publisher masina = new Publisher();

        try {
            masina.start();
        } catch (UnknownHostException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void startAsASubscriber() {
        Subscriber masina = new Subscriber();

        try {
            masina.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static void startAsABroker() {
        ArrayList<InetAddress> adreseRetea = new ArrayList<>();

        try {
            adreseRetea.add(InetAddress.getByName("192.168.30.4"));
            adreseRetea.add(InetAddress.getByName("192.168.30.7"));
            adreseRetea.add(InetAddress.getByName("192.168.30.9"));
            adreseRetea.add(InetAddress.getByName("192.168.30.10"));
            adreseRetea.add(InetAddress.getByName("192.168.30.12"));

            Broker masina = new Broker(adreseRetea);
            masina.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
