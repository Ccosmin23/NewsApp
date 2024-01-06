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
        LoggerService.shared.sendLogToLogger("a fost creat un publisher cu adresa IP " + boldedHostAddress);

        while (programIsRunning) {
            switch (publisherView.afiseazaInterfata()) {
                case "c":
                    createArticle();
                    break;
                case "g":
                    publisherView.genereazaArticole();
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

            LoggerService.shared.sendLogToLogger("publisher-ul " + boldedHostAddress + " a trimis o stire");

            raspuns = (BrokerMessage) objectInputStream.readObject();
            LoggerService.shared.sendLogToLogger("Raspunsul server-ului pentru " + boldedHostAddress + ": " + raspuns.primesteMesaj());

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
                LoggerService.shared.sendLogToLogger("publisher-ul " + boldedHostAddress + " a creat stirea cu ID-ul " + stireaCreata.getId());

            } catch (ClassNotFoundException e) {
                LoggerService.shared.sendLogToLogger("publisher-ul " + boldedHostAddress + " a incercat sa creeze stirea" + stireaCreata.getId());
                throw new RuntimeException(e);
            }
        }
    }

    private void closeProgram() throws UnknownHostException {
        programIsRunning = false;
        publisherView.inchideInterfata();
        LoggerService.shared.sendLogToLogger("publisher-ul cu adresa IP " + boldedHostAddress + " a fost inchis");
    }
}
