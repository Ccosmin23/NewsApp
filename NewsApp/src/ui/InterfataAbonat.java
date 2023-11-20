package ui;

import java.util.Scanner;

public class InterfataAbonat {
    public Scanner scannerTastatura;

    public void afiseazaInterfata () {
        String optiune;

        scannerTastatura = new Scanner(System.in);

        System.out.println("Optiunile disponibile sunt:\n");
        System.out.println("[a] -> abonare la un topic");
        System.out.println("[f] -> aflati ultimele stiri");
        System.out.println("[x] -> iesire din program\n");

        System.out.print("Optiunea dorita: ");
        optiune = scannerTastatura.nextLine();

        switch (optiune) {
            case "a": {
                afisareStiri();
                break;
            }

            case "f": {
                abonareLaTopic();
                break;
            }

            case "x": {
                // Închidere program
            }
        }
    }

    public void afisareStiri () {

    }

    public void abonareLaTopic () {

    }

    public void inchideInterfata () {
        scannerTastatura.close();
    }

    public InterfataAbonat () {}
}