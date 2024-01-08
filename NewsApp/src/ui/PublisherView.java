package ui;

import java.util.ArrayList;
import java.util.Scanner;

import model.news.NewsStory;

public class PublisherView {
    public Scanner scannerTastatura = new Scanner(System.in);

    public PublisherView() {

    }

    public String afiseazaInterfata () {
        String optiune;

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

    public int getDesiredNumberOfArticles () {
        int numberOfArticles = 0;
        String input;

        System.out.println("Introduceti un numar de articole ce vor fi generate si trimise");
        input = scannerTastatura.nextLine();
        if (input.matches("^[1-9][0-9]*") == false) {
            System.out.println("Nu a-ti introdus un numar intreg pozitiv nenul!");
            return 0;
        }

        try {
            numberOfArticles = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("EROARE: Nu a-ti introdus un numar intreg pozitiv");
            return 0;
        }

        if (numberOfArticles >= 1000) {
            System.out.println("Numarul de articole propus trebuie sa fie mai mic de 1000!");
            return 0;
        }

        return numberOfArticles;
    }

    public void inchideInterfata () {
        scannerTastatura.close();
    }

}