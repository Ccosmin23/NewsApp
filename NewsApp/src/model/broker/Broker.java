package model.broker;

import java.io.Console;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;

import model.news.NewsField;

public class Broker {
    InetAddress personalAddress;
    InetAddress nextNode;
    ServerSocket receiverSocket;
    ArrayList<InetAddress> nodeAddresses;
    AtomicBoolean isRunning;
    NewsField newsList;

    public void send(InetAddress destination) throws ClassNotFoundException {
        try {
            ObjectOutputStream oos;
            ObjectInputStream ois;
            BrokerMessage msg = new BrokerMessage("Hello", destination);
            BrokerMessage response;
            Socket communicationSocket = new Socket(nextNode, 9700);

            oos = new ObjectOutputStream(communicationSocket.getOutputStream());
            ois = new ObjectInputStream(communicationSocket.getInputStream());

            oos.writeObject(msg);
            oos.flush();

            response = (BrokerMessage) ois.readObject();
            System.out.println("Message from the neighbor: " + response.receiveMessage());

            oos.close();
            ois.close();
            communicationSocket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void replicateArticleToNeighbor(BrokerMessage replicaPackage) throws ClassNotFoundException {
        try {
            ObjectOutputStream oos;
            ObjectInputStream ois;
            BrokerMessage response;
            Socket communicationSocket = new Socket(nextNode, 9700);

            oos = new ObjectOutputStream(communicationSocket.getOutputStream());
            ois = new ObjectInputStream(communicationSocket.getInputStream());

            oos.writeObject(replicaPackage);
            oos.flush();

            response = (BrokerMessage) ois.readObject();
            System.out.println("Message from the neighbor: " + response.receiveMessage());

            oos.close();
            ois.close();
            communicationSocket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Thread receive() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                Socket clientSocket;
                DataInputStream clientIStream;
                DataOutputStream clientOStream;
                ObjectOutputStream oos;
                ObjectInputStream ois;
                BrokerMessage receivedMessage;

                try {
                    receiverSocket = new ServerSocket(9700);
                    while (isRunning.get() == true) {
                        try {
                            clientSocket = receiverSocket.accept();
                            System.out.println("Message from " + clientSocket.getInetAddress().toString() + " is received!");

                            clientIStream = new DataInputStream(clientSocket.getInputStream());
                            clientOStream = new DataOutputStream(clientSocket.getOutputStream());

                            ois = new ObjectInputStream(clientIStream);
                            oos = new ObjectOutputStream(clientOStream);

                            receivedMessage = (BrokerMessage) ois.readObject();

                            System.out.println(receivedMessage.receiveCommand());
                            switch (receivedMessage.receiveCommand()) {
                                case "publish": {
                                    BrokerMessage response = new BrokerMessage("Received your publication!", personalAddress);
                                    BrokerMessage replica = new BrokerMessage("Reply message, article replication!", personalAddress);

                                    System.out.println("Received message from the PUBLISHER!");
                                    newsList.addNewsStory(receivedMessage.receiveStory());
                                    oos.writeObject(response);

                                    System.out.println("Bringing the introduced article to the rest of the system.");
                                    replica.setCommand("replicate");
                                    replica.setStory(receivedMessage.receiveStory());

                                    replicateArticleToNeighbor(replica);

                                    break;
                                }

                                case "articles": {
                                    BrokerMessage response = new BrokerMessage("Received your data request (articles in this case)!", personalAddress);

                                    response.setNewsList(newsList);

                                    System.out.println("Received message from the SUBSCRIBER!");
                                    oos.writeObject(response);
                                    break;
                                }

                                case "replicate": {
                                    BrokerMessage response = new BrokerMessage("Replicated the article \"" + receivedMessage.receiveStory().getTitle() + "\"", personalAddress);

                                    System.out.println("Received replication request from the neighbor broker " + clientSocket.getInetAddress().toString());

                                    newsList.addNewsStory(receivedMessage.receiveStory());
                                    oos.writeObject(response);

                                    if (!receivedMessage.receiveAddress().equals(nextNode)) {
                                        System.out.println("Replicating the article to the next neighbor.");
                                        replicateArticleToNeighbor(receivedMessage);
                                    } else {
                                        System.out.println("No longer replicating to the original node.");
                                    }

                                    break;
                                }

                                default: {
                                    if (receivedMessage.receiveAddress().equals(personalAddress)) {
                                        BrokerMessage response = new BrokerMessage("It's me. Thanks for the message!", personalAddress);

                                        System.out.println("It's me!");
                                        oos.writeObject(response);
                                    } else {
                                        BrokerMessage response = new BrokerMessage("It's not me. But I forwarded it further", personalAddress);

                                        System.out.println("It's not me!");
                                        oos.writeObject(response);
                                        Thread.sleep(500);
                                        send(receivedMessage.receiveAddress());
                                    }
                                }
                            }

                            ois.close();
                            oos.close();
                            clientSocket.close();
                        } catch (SocketException e) {
                            System.out.println(e.getMessage());
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    System.out.println("FINISHED LISTENING!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void startMessage() {
        System.out.println("Broker has started.\n" +
                " - please wait until it receives a message...\n" +
                " - meanwhile if you want to stop this process, please press 'x' button\n");
    }

    public void start() throws SocketException, UnknownHostException {
        startMessage();

        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        InetAddress hostAddress = null;

        // Check if the machine running the program has an interface with an IP address
        // from the list of addresses of machines participating in the ring topology network
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();

            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                if (nodeAddresses.contains(inetAddress) && !inetAddress.isLoopbackAddress()) {
                    hostAddress = inetAddress;
                    break;
                }
            }

            if (hostAddress != null) {
                break;
            }
        }

        // Determine the next node with which the broker machine will communicate using
        // the list of defined IP addresses
        if (nodeAddresses.contains(hostAddress)) {
            this.nextNode = nodeAddresses.get((nodeAddresses.indexOf(hostAddress) + 1) % nodeAddresses.size());
        }

        personalAddress = InetAddress.getByAddress(hostAddress.getAddress());

        this.isRunning = new AtomicBoolean(true);
        receive().start();
        while (this.isRunning.get() == true) {
            Console console = System.console();
            switch (console.readLine("-> ")) {
                case "s": {
                    try {
                        send(InetAddress.getByName("192.168.30.10"));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                }

                case "x": {
                    this.isRunning.set(false);
                    try {
                        receiverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                }

                default: {
                    break;
                }
            }
        }

        System.out.println("FINISHED!");
    }

    public Broker(ArrayList<InetAddress> machineAddresses) {
        this.nodeAddresses = machineAddresses;
        this.newsList = new NewsField(1, "News");
    }
}
