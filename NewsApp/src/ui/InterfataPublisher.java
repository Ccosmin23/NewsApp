package ui;

import java.util.ArrayList;
import java.util.Scanner;

public class InterfataPublisher {
    public Scanner scannerTastatura;

    public void afiseazaInterfata () {
        String optiune;
        
        scannerTastatura = new Scanner(System.in);

        System.out.println("Optiunile disponibile sunt:\n");
        System.out.println("[c] -> creeaza un articol");
        System.out.println("[g] -> genereaza (multe) articole");
        System.out.println("[x] -> iesire din program\n");

        System.out.print("Optiunea dorita: ");
        optiune = scannerTastatura.nextLine();

        switch (optiune) {
            case "c": {
                creeazaArticol();
                break;
            }

            case "g": {
                genereazaArticole();
                break;
            }

            case "x": {
                // Închidere program
            }
        }
    }

    public void creeazaArticol () {
        String titluArticol;
        ArrayList<String> listaLinii;
        String linieCaractere;
        String continut = "";

        System.out.println("Scrieti titlul articolului");
        titluArticol = scannerTastatura.nextLine();

        // Dacă nu s-a dat titlul articolului, atunci
        // se consideră anularea operației
        if (titluArticol.length() == 0) {
            return;
        }

        listaLinii = new ArrayList<String>();
        System.out.println("Scrieti continutul articolului");
        linieCaractere = scannerTastatura.nextLine();

        while (linieCaractere.length() != 0) {
            listaLinii.add(linieCaractere);
            linieCaractere = scannerTastatura.nextLine();
        }

        if (listaLinii.size() != 0) {
            for (String linie : listaLinii) {
                continut += linie + "\n";    
            }

            System.out.print(continut);
        }
    }

    public void genereazaArticole () {

    }

    public void inchideInterfata () {
        scannerTastatura.close();
    }

    public InterfataPublisher () {}
}