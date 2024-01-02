package model.subscriber;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import model.broker.BrokerMessage;
import model.news.NewsField;
import model.news.NewsStory;
import ui.SubscriberView;

public class Subscriber {
    private NewsField articleList;

    public void receiveArticles(InetAddress destination) throws ClassNotFoundException {
        try {
            ObjectOutputStream oos;
            ObjectInputStream ois;
            BrokerMessage msg = new BrokerMessage("Hello broker", destination);
            Socket communicationSocket = new Socket(destination, 9700);
            BrokerMessage response;

            // The "articles" command will tell the broker to send
            // a list of news in the response it will provide
            msg.setCommand("articles");

            oos = new ObjectOutputStream(communicationSocket.getOutputStream());
            ois = new ObjectInputStream(communicationSocket.getInputStream());

            oos.writeObject(msg);
            oos.flush();

            // Extract the broker's response that contains the list of articles
            response = (BrokerMessage) ois.readObject();
            this.articleList = response.receiveNewsList();

            System.out.println("Broker's response: " + response.receiveMessage());

            communicationSocket.close();
            oos.close();
            ois.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listArticles() {
        if ((this.articleList != null) && (this.articleList.getNewsStoryList() != null) && (this.articleList.getNewsStoryList().size() != 0)) {
            ArrayList<NewsStory> extractedArticles = this.articleList.getNewsStoryList();

            System.out.println("Published articles are:");
            for (NewsStory article : extractedArticles) {
                System.out.println(article.getTitle() + "\n");
                System.out.println(article.getContent() + "\n-------------------------------");
            }
        } else {
            System.out.println("There are no published articles in the system.");
        }
    }

    public void start() throws UnknownHostException {
        SubscriberView subscriberUI = new SubscriberView();

        // subscriberUI.displayInterface();
        // subscriberUI.closeInterface();

        try {
            receiveArticles(InetAddress.getByName("192.168.30.10"));
            listArticles();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
