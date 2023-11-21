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
                // TODO: De implementat partea de broker
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