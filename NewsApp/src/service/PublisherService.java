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
import utils.InetAddressUtils;
import utils.StringUtils;
import utils.SystemSetup;

public final class PublisherService {
    public static PublisherService shared = new PublisherService();

    private PublisherView publisherView;
    boolean programIsRunning = true;

    public PublisherService() {
        publisherView = new PublisherView();
    }

    public void start() throws UnknownHostException, ClassNotFoundException {
        LoggerService.shared.sendLogToLogger("A fost creat un publisher cu adresa IP " + InetAddressUtils.boldedHostAddress());

        while (programIsRunning) {
            switch (publisherView.afiseazaInterfata()) {
                case "c":
                    createArticle();
                    break;
                case "g":
                    generateArticles();
                    break;
                case "x":
                    closeProgram();
                    break;
                case "d":
                    getFirstBrokerInetAddress();
                    break;
                default:
                    System.out.println("Optiune invailda");
            }
        }
    }

    public InetAddress getFirstBrokerInetAddress() throws ClassNotFoundException {
        InetAddress inetAddress = null;

        try {
            ObjectOutputStream objectOutputStream;
            ObjectInputStream objectInputStream;

            Socket socketComunicare = new Socket(SystemSetup.ringManagerIpAddress, SystemSetup.port);

            objectOutputStream = new ObjectOutputStream(socketComunicare.getOutputStream());
            objectInputStream = new ObjectInputStream(socketComunicare.getInputStream());

            objectOutputStream.writeObject("get first broker");
            objectOutputStream.flush();

            inetAddress = (InetAddress) objectInputStream.readObject();
            System.out.println("am primit de la BE adresa = " + inetAddress.getHostAddress());

            socketComunicare.close();
            objectOutputStream.close();
            objectInputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return inetAddress;
    }

    public void trimiteStirea(InetAddress destinatie, NewsStory stirea) throws ClassNotFoundException {
        try {
            ObjectOutputStream objectOutputStream;
            ObjectInputStream objectInputStream;

            BrokerMessage brokerMessage = new BrokerMessage("Hello broker de inel", destinatie);
            Socket socketComuicare = new Socket(destinatie, SystemSetup.port);
            BrokerMessage raspuns;

            brokerMessage.seteazaComanda("publica");
            brokerMessage.seteazaStirea(stirea);

            objectOutputStream = new ObjectOutputStream(socketComuicare.getOutputStream());
            objectInputStream = new ObjectInputStream(socketComuicare.getInputStream());

            objectOutputStream.writeObject(brokerMessage);
            objectOutputStream.flush();

            LoggerService.shared.sendLogToLogger("\nPublisher-ul " + InetAddressUtils.boldedHostAddress() + " a trimis o stire");

            raspuns = (BrokerMessage) objectInputStream.readObject();
            LoggerService.shared.sendLogToLogger("\nRaspunsul server-ului pentru publisher-ul " + InetAddressUtils.boldedHostAddress() + ": " + raspuns.primesteMesaj());

            socketComuicare.close();
            objectOutputStream.close();
            objectInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createArticle() throws UnknownHostException, ClassNotFoundException {
        NewsStory stireaCreata = publisherView.creeazaArticol();

        if (stireaCreata != null) {
            try {
                trimiteStirea(getFirstBrokerInetAddress(), stireaCreata);
                LoggerService.shared.sendLogToLogger("\nPublisher-ul " + InetAddressUtils.boldedHostAddress() + " a creat cu succes stirea cu ID-ul " + stireaCreata.getId());

            } catch (ClassNotFoundException e) {
                LoggerService.shared.sendLogToLogger("\nPublisher-ul " + InetAddressUtils.boldedHostAddress() + " a incercat sa creeze stirea" + stireaCreata.getId());
                throw new RuntimeException(e);
            }
        }
    }

    private void generateArticles () {
        int numberOfArticles = publisherView.getDesiredNumberOfArticles();
        if (numberOfArticles == 0) {
            return;
        }

        NewsStory[] articles = new NewsStory[numberOfArticles];
        String content;
        String title;
        int i = 0;

        try {
            while (i < numberOfArticles) {
                title = StringUtils.randomString(3, 32);
                content = StringUtils.randomString(1, 80);

                articles[i] = new NewsStory(null, title, content);

                trimiteStirea(getFirstBrokerInetAddress(), articles[i]);
                
                i += 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeProgram() throws UnknownHostException {
        programIsRunning = false;
        publisherView.inchideInterfata();
        LoggerService.shared.sendLogToLogger("Publisher-ul cu adresa IP " + InetAddressUtils.boldedHostAddress() + " a fost inchis");
    }
}
