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

public final class PublisherService {
    public static PublisherService shared = new PublisherService();

    private PublisherView publisherView;
    boolean programIsRunning = true;

    String hostAddress = ""; {
        try {
            hostAddress = InetAddressUtils.getLocalAddress().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    String boldedHostAddress = StringUtils.applyBoldTo(hostAddress, false);

    public PublisherService() {
        publisherView = new PublisherView();
    }

    public void start() throws UnknownHostException, ClassNotFoundException {
        LoggerService.shared.sendLogToLogger("A fost creat un publisher cu adresa IP " + boldedHostAddress);

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

            LoggerService.shared.sendLogToLogger("\nPublisher-ul " + boldedHostAddress + " a trimis o stire");

            raspuns = (BrokerMessage) objectInputStream.readObject();
            LoggerService.shared.sendLogToLogger("\nRaspunsul server-ului pentru publisher-ul " + boldedHostAddress + ": " + raspuns.primesteMesaj());

            socketComuicare.close();
            objectOutputStream.close();
            objectInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createArticle() throws UnknownHostException {
        NewsStory stireaCreata = publisherView.creeazaArticol();
        String brokerIPAddress = "192.168.30.10";

        if (stireaCreata != null) {
            try {
                trimiteStirea(InetAddress.getByName(brokerIPAddress), stireaCreata);
                LoggerService.shared.sendLogToLogger("\nPublisher-ul " + boldedHostAddress + " a creat cu succes stirea cu ID-ul " + stireaCreata.getId());

            } catch (ClassNotFoundException e) {
                LoggerService.shared.sendLogToLogger("\nPublisher-ul " + boldedHostAddress + " a incercat sa creeze stirea" + stireaCreata.getId());
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
        String brokerIPAddress = "192.168.30.10";
        String content;
        String title;
        int i = 0;

        try {
            while (i < numberOfArticles) {
                title = StringUtils.randomString(3, 32);
                content = StringUtils.randomString(1, 80);

                articles[i] = new NewsStory(null, title, content);

                trimiteStirea(InetAddress.getByName(brokerIPAddress), articles[i]);
                
                i += 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeProgram() throws UnknownHostException {
        programIsRunning = false;
        publisherView.inchideInterfata();
        LoggerService.shared.sendLogToLogger("Publisher-ul cu adresa IP " + boldedHostAddress + " a fost inchis");
    }
}
