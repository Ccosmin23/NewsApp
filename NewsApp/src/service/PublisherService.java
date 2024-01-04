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
    private PublisherView publisherView;
    boolean programHasBeenClosed = false;
    public static PublisherService shared = new PublisherService();

    public PublisherService() {
        publisherView = new PublisherView();
    }

    public void start() throws UnknownHostException, ClassNotFoundException {
        while (programHasBeenClosed != true) {
            switch (publisherView.afiseazaInterfata()) {
                case "c": {
                    createArticle();
                    break;
                }
                case "g": {
                    publisherView.genereazaArticole();
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

    private void createArticle() throws UnknownHostException {
        NewsStory stireaCreata = publisherView.creeazaArticol();

        if (stireaCreata != null) {
            try {
                trimiteStirea(InetAddress.getByName("192.168.30.10"), stireaCreata);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void closeProgram() {
        programHasBeenClosed = true;
        publisherView.inchideInterfata();
    }
}
