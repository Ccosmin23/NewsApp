package architecture;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import model.MesajPachet;
import model.NewsField;
import model.NewsStory;
import ui.InterfataAbonat;

public class Subscriber {
    private NewsField listaArticole;

    public void primesteArticole (InetAddress destinatie) throws ClassNotFoundException {
        try {
            ObjectOutputStream oos;
            ObjectInputStream ois;
            MesajPachet msg = new MesajPachet("Hello broker", destinatie);
            Socket socketComuicare = new Socket(destinatie, 9700);
            MesajPachet raspuns;

            // Comanda "articole" îi va spune broker-ului să trimită
            // o listă de știri în răspunsul pe care îl va furniza
            msg.seteazaComanda("articole");

            oos = new ObjectOutputStream(socketComuicare.getOutputStream());
            ois = new ObjectInputStream(socketComuicare.getInputStream());
            
            oos.writeObject(msg);
            oos.flush();

            // Extrage răspunsul broker-ului care are lista de știri
            raspuns = (MesajPachet) ois.readObject();
            this.listaArticole = raspuns.primesteListaStiri();

            System.out.println("Raspunsul broker-ului: " + raspuns.primesteMesaj());

            socketComuicare.close();
            oos.close();
            ois.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listeazaStiri () {
        if ((this.listaArticole != null) && (this.listaArticole.getNewsStoryList() != null) && (this.listaArticole.getNewsStoryList().size() != 0)) {
            ArrayList<NewsStory> stirileExtrase = this.listaArticole.getNewsStoryList();

            System.out.println("Articolele publicate sunt:");
            for (NewsStory stire : stirileExtrase) {
                System.out.println(stire.getTitlu() + "\n");
                System.out.println(stire.getContinut() + "\n-------------------------------");
            }
        } else {
            System.out.println("Nu exista articole publicate in sistem.");
        }
    }

    public void start () throws UnknownHostException {
        InterfataAbonat uiAbonat = new InterfataAbonat();

        // uiAbonat.afiseazaInterfata();
        // uiAbonat.inchideInterfata();

        try {
            primesteArticole(InetAddress.getByName("192.168.30.10"));
            listeazaStiri();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
