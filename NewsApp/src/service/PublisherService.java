package service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import model.broker.BrokerMessage;
import model.news.NewsStory;
import ui.PublisherView;

public final class PublisherService {
    private PublisherView uiPublisher;
    boolean programClosed = false;
    public static PublisherService shared = new PublisherService();

    public PublisherService() {
        uiPublisher = new PublisherView();
    }

    public void trimiteStirea (InetAddress destinatie, NewsStory stirea) throws ClassNotFoundException {
        try {
            ObjectOutputStream objectOutputStream;
            ObjectInputStream objectInputStream;

            BrokerMessage brokerMessage = new BrokerMessage("Hello broker de inel", destinatie);
            Socket socketComuicare = new Socket(destinatie, 9700);
            BrokerMessage raspuns;

            brokerMessage.seteazaComanda("publica");
            brokerMessage.seteazaStirea(stirea);

            objectOutputStream = new ObjectOutputStream(socketComuicare.getOutputStream());
            objectInputStream = new ObjectInputStream(socketComuicare.getInputStream());

            objectOutputStream.writeObject(brokerMessage);
            objectOutputStream.flush();

            System.out.println("Publicare trimisa");

            raspuns = (BrokerMessage) objectInputStream.readObject();
            System.out.println("Raspunsul server-ului: " + raspuns.primesteMesaj());

            socketComuicare.close();
            objectOutputStream.close();
            objectInputStream.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() throws UnknownHostException, ClassNotFoundException {
        while (programClosed != true) {
            switch (uiPublisher.afiseazaInterfata()) {
                case "c": {
                    createArticle();
                    break;
                }
                case "g": {
                    uiPublisher.genereazaArticole();
                    break;
                }
                case "x":
                    closeProgram();
                    break;
                default:
                    System.out.println("Optiune invailda");
            }
        }
    }

    private void createArticle() throws UnknownHostException {
        NewsStory stireaCreata = uiPublisher.creeazaArticol();

        if (stireaCreata != null) {
            try {
                trimiteStirea(InetAddress.getByName("192.168.30.10"), stireaCreata);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void closeProgram() {
        programClosed = true;
        uiPublisher.inchideInterfata();
    }
}
