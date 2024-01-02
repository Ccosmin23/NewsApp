package model.broker;

import model.news.NewsField;
import model.news.NewsStory;

import java.io.Serializable;
import java.net.InetAddress;

public class BrokerMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String message;
    private String command;
    private InetAddress destination;
    private NewsStory story;
    private NewsField newsList;

    public BrokerMessage(String msg, InetAddress dest) {
        this.destination = dest;
        this.message = msg;
        this.command = "";
    }

    public NewsStory receiveStory() {
        return this.story;
    }

    public void setStory(NewsStory receivedStory) {
        this.story = receivedStory;
    }

    public NewsField receiveNewsList() {
        return this.newsList;
    }

    public void setNewsList(NewsField receivedNewsList) {
        this.newsList = receivedNewsList;
    }

    public String receiveMessage() {
        return this.message;
    }

    public InetAddress receiveAddress() {
        return this.destination;
    }

    public void setCommand(String givenCommand) {
        this.command = givenCommand;
    }

    public String receiveCommand() {
        return this.command;
    }
}
