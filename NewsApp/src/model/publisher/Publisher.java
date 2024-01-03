package model.publisher;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import model.broker.BrokerMessage;
import model.news.NewsStory;
import ui.PublisherView;

public class Publisher {
    private PublisherView uiPublisher;

    public void trimiteStirea (InetAddress destinatie, NewsStory stirea) throws ClassNotFoundException {
        try {
            ObjectOutputStream oos;
            ObjectInputStream ois;
            BrokerMessage msg = new BrokerMessage("Hello broker de inel", destinatie);
            Socket socketComuicare = new Socket(destinatie, 9700);
            BrokerMessage raspuns;

            msg.seteazaComanda("publica");
            msg.seteazaStirea(stirea);

            oos = new ObjectOutputStream(socketComuicare.getOutputStream());
            ois = new ObjectInputStream(socketComuicare.getInputStream());
            
            oos.writeObject(msg);
            oos.flush();

            System.out.println("Publicare trimisa");

            raspuns = (BrokerMessage) ois.readObject();
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
        PublisherView uiPublisher = new PublisherView();
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
