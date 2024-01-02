package model.publisher;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import model.broker.MesajPachet;
import model.news.NewsStory;
import ui.InterfataPublisher;

public class Publisher {
    private InterfataPublisher uiPublisher;

    public void trimiteStirea (InetAddress destinatie, NewsStory stirea) throws ClassNotFoundException {
        try {
            ObjectOutputStream oos;
            ObjectInputStream ois;
            MesajPachet msg = new MesajPachet("Hello broker de inel", destinatie);
            Socket socketComuicare = new Socket(destinatie, 9700);
            MesajPachet raspuns;

            msg.seteazaComanda("publica");
            msg.seteazaStirea(stirea);

            oos = new ObjectOutputStream(socketComuicare.getOutputStream());
            ois = new ObjectInputStream(socketComuicare.getInputStream());
            
            oos.writeObject(msg);
            oos.flush();

            System.out.println("Publicare trimisa");

            raspuns = (MesajPachet) ois.readObject();
            System.out.println("Raspunsul server-ului: " + raspuns.primesteMesaj());

            socketComuicare.close();
            oos.close();
            ois.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start () throws UnknownHostException, ClassNotFoundException {
        InterfataPublisher uiPublisher = new InterfataPublisher();
        boolean terminat = false;

        while (terminat != true) {
            switch (uiPublisher.afiseazaInterfata()) {
                case "c": {
                    NewsStory stireaCreata = uiPublisher.creeazaArticol();
                    if (stireaCreata != null) {
                        trimiteStirea(InetAddress.getByName("192.168.30.10"), stireaCreata);
                    }

                    break;
                }

                case "g": {
                    uiPublisher.genereazaArticole();
                    break;
                }

                case "x": {
                    // Închidere program
                    terminat = true;
                    uiPublisher.inchideInterfata();
                }
            }
        }
    }
}
