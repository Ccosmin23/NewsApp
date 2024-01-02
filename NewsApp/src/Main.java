
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
    public static String applyBoldTo(String text) {
        return "'\033[1m" + text + "\033[0m'";
    }

    public static void showMenuMessages() {
        String publisherBolded = applyBoldTo("publisher");
        String subscriberBolded = applyBoldTo("subscriber");
        String brokerBolded = applyBoldTo("broker");

        System.out.println("We cannot start the app for you, because you should enter an argument as: " +
                publisherBolded + ", " + subscriberBolded + " or " + brokerBolded + "\n" +
                publisherBolded + " -> will start the execution as a publisher\n" +
                subscriberBolded + " -> will start the execution as a subscriber\n" +
                brokerBolded + "-> will start the execution as a broker\n\n");
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