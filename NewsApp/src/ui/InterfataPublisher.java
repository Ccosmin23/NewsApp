package ui;

import java.util.ArrayList;
import java.util.Scanner;

import model.news.NewsStory;

public class InterfataPublisher {
    public Scanner scannerTastatura;

    public String afiseazaInterfata () {
        String optiune;
        
        scannerTastatura = new Scanner(System.in);

        System.out.println("Optiunile disponibile sunt:\n");
        System.out.println("[c] -> creeaza un articol");
        System.out.println("[g] -> genereaza (multe) articole");
        System.out.println("[x] -> iesire din program\n");

        System.out.print("Optiunea dorita: ");
        optiune = scannerTastatura.nextLine();

        return optiune;
    }

    public NewsStory creeazaArticol () {
        String titluArticol;
        ArrayList<String> listaLinii;
        String linieCaractere;
        String continut = "";
        NewsStory articolStire = null;

        System.out.println("Scrieti titlul articolului");
        titluArticol = scannerTastatura.nextLine();

        // Dacă nu s-a dat titlul articolului, atunci
        // se consideră anularea operației
        if (titluArticol.length() == 0) {
            return null;
        }

        listaLinii = new ArrayList<String>();

        System.out.println("Scrieti continutul articolului");
        linieCaractere = scannerTastatura.nextLine();

        // Atâta timp cât utilizatorul scrie conținutul știrei,
        // adaugă noi linii pentru conținutul știrii
        while (linieCaractere.length() != 0) {
            listaLinii.add(linieCaractere);
            linieCaractere = scannerTastatura.nextLine();
        }

        if (listaLinii.size() != 0) {
            for (String linie : listaLinii) {
                continut += linie + "\n";    
            }

            articolStire = new NewsStory(0, titluArticol, continut);
            return articolStire;
        } else {
            // Dacă o știre cu un titlu dar fără conținut a fost creat, atunci
            // nu se consideră introducerea știrei în sistem.
            return null;
        }
    }

    public void genereazaArticole () {

    }

    public void inchideInterfata () {
        scannerTastatura.close();
    }

    public InterfataPublisher () {}
}