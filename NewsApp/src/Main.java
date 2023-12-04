import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import architecture.Broker;
import ui.InterfataAbonat;
import ui.InterfataPublisher;

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
                System.out.println("Hello world!");
                InterfataPublisher uiPublisher = new InterfataPublisher();

                uiPublisher.afiseazaInterfata();
                uiPublisher.inchideInterfata();
                break;
            }

            case "subscriber": {
                System.out.println("Hello world!");
                InterfataAbonat uiAbonat = new InterfataAbonat();

                uiAbonat.afiseazaInterfata();
                uiAbonat.inchideInterfata();
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