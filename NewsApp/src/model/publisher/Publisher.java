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
    private PublisherView publisherUI;

    public void sendNews(InetAddress destination, NewsStory newsStory) throws ClassNotFoundException {
        try {
            ObjectOutputStream oos;
            ObjectInputStream ois;
            BrokerMessage msg = new BrokerMessage("Hello broker ring", destination);
            Socket communicationSocket = new Socket(destination, 9700);
            BrokerMessage response;

            msg.setCommand("publish");
            msg.setStory(newsStory);

            oos = new ObjectOutputStream(communicationSocket.getOutputStream());
            ois = new ObjectInputStream(communicationSocket.getInputStream());

            oos.writeObject(msg);
            oos.flush();

            System.out.println("Publication sent");

            response = (BrokerMessage) ois.readObject();
            System.out.println("Server response: " + response.receiveMessage());

            communicationSocket.close();
            oos.close();
            ois.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() throws UnknownHostException, ClassNotFoundException {
        publisherUI = new PublisherView();
        boolean finished = false;

        while (!finished) {
            switch (publisherUI.displayInterface()) {
                case "c": {
                    NewsStory createdNewsStory = publisherUI.createArticle();
                    if (createdNewsStory != null) {
                        sendNews(InetAddress.getByName("192.168.30.10"), createdNewsStory);
                    }

                    break;
                }

                case "g": {
                    publisherUI.createMultipleArticles();
                    break;
                }

                case "x": {
                    // Close program
                    finished = true;
                    publisherUI.closeInterface();
                }
            }
        }
    }
}
