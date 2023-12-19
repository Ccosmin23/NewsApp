import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import architecture.Broker;
import architecture.Publisher;
import architecture.Subscriber;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Trebuie specificat unul dintre argumentele date:");
            System.out.println("    [publisher] -> lanseaza procesul pentru abonat");
            System.out.println("    [subscriber] -> lanseaza procesul pentru publisher");
            System.out.println("    [broker] -> lanseaza procesul pentru broker, care se ocupa cu stocarea si dispecerizarea mesajelor");
            return;
        }

        switch (args[0]) {
            case "publisher": {
                Publisher masina = new Publisher();

                try {
                    masina.start();
                } catch (UnknownHostException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

                break;
            }

            case "subscriber": {
                Subscriber masina = new Subscriber();
                
                try {
                    masina.start();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                
                break;
            }

            case "broker": {
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
                break;
            }

            default: {
                System.out.println("Trebuie specificat unul dintre argumentele date:");
                System.out.println("    [publisher] -> lanseaza procesul pentru abonat");
                System.out.println("    [subscriber] -> lanseaza procesul pentru publisher");
                System.out.println("    [broker] -> lanseaza procesul pentru broker, care se ocupa cu stocarea si dispecerizarea mesajelor");
                break;
            }
        }
    }
}