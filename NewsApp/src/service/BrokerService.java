package service;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;
import model.broker.BrokerMessage;
import model.news.NewsField;
import utils.InetAddressUtils;
import utils.StringUtils;
import utils.SystemSetup;

public class BrokerService implements Serializable {
    private InetAddress adresaPersonala;
    private ServerSocket receiverSocket;
    private AtomicBoolean programIsRunning;
    private NewsField listaStiri;

    public BrokerService(InetAddress adresaPersonala) {
        this.adresaPersonala = adresaPersonala;
    }

    public BrokerService() {
        this.listaStiri = new NewsField(1, "Stiri");
        adresaPersonala = InetAddressUtils.hostAddress();
        appendNewBroker();
    }

    // ========================================== start() ==========================================
    public void start() throws UnknownHostException, SocketException {
        receive().start();
        userInputHandler();
    }

    public void appendNewBroker() {
        try {
            Socket socketComunicare = new Socket(SystemSetup.ringManagerIpAddress, SystemSetup.port);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socketComunicare.getOutputStream());

            InetAddress address = InetAddressUtils.hostAddress();
            System.out.println("i-am trimis lui RingManager adresa = " + address);

            objectOutputStream.writeObject(address);
            objectOutputStream.flush();

            socketComunicare.close();
            objectOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public InetAddress getNodCurent() throws ClassNotFoundException {
        InetAddress nodCurent = null;

        try {
            ObjectOutputStream objectOutputStream;
            ObjectInputStream objectInputStream;

            Socket socketComunicare = new Socket(SystemSetup.ringManagerIpAddress, SystemSetup.port);

            objectOutputStream = new ObjectOutputStream(socketComunicare.getOutputStream());
            objectInputStream = new ObjectInputStream(socketComunicare.getInputStream());

            objectOutputStream.writeObject("get nod curent");
            objectOutputStream.flush();

            InetAddress inetAddress = (InetAddress) objectInputStream.readObject();
//            System.out.println("am primit de la BE nodul curent = " + inetAddress.getHostAddress());
            nodCurent = inetAddress;

            socketComunicare.close();
            objectOutputStream.close();
            objectInputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return nodCurent;
    }

    public InetAddress getNodUrmator() throws ClassNotFoundException {
        InetAddress nodUrmator = null;

        try {
            ObjectOutputStream objectOutputStream;
            ObjectInputStream objectInputStream;

            Socket socketComunicare = new Socket(SystemSetup.ringManagerIpAddress, SystemSetup.port);

            objectOutputStream = new ObjectOutputStream(socketComunicare.getOutputStream());
            objectInputStream = new ObjectInputStream(socketComunicare.getInputStream());

            objectOutputStream.writeObject("get nod urmator");
            objectOutputStream.flush();

            InetAddress inetAddress = (InetAddress) objectInputStream.readObject();
//            System.out.println("am primit de la BE nodul urmator = " + inetAddress.getHostAddress());
            nodUrmator = inetAddress;

            socketComunicare.close();
            objectOutputStream.close();
            objectInputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return nodUrmator;
    }

    // ========================================== receive() ==========================================
    public Thread receive () {
        this.programIsRunning = new AtomicBoolean(true);

        return new Thread(new Runnable() {
            @Override
            public void run () {
                Socket clientSocket;
                DataInputStream clientIStream;
                DataOutputStream clientOStream;
                ObjectOutputStream oos;
                ObjectInputStream ois;
                BrokerMessage mesajReceptionat;

                try {
                    receiverSocket = new ServerSocket(SystemSetup.port);
                    LoggerService.shared.sendLogToLogger("Broker-ul " + InetAddressUtils.boldedHostAddress() + " a fost pornit");

                    while (programIsRunning.get()) {
                        try {
                            clientSocket = receiverSocket.accept();

                            clientIStream = new DataInputStream(clientSocket.getInputStream());
                            clientOStream = new DataOutputStream(clientSocket.getOutputStream());

                            ois = new ObjectInputStream(clientIStream);
                            oos = new ObjectOutputStream(clientOStream);

                            mesajReceptionat = (BrokerMessage) ois.readObject();

                            try {
                                process(mesajReceptionat, oos, ois, clientSocket);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            } finally {
                                ois.close();
                                oos.close();
                                clientSocket.close();
                            }

                        } catch (SocketException e) {
                            System.out.println("tece pe aici1");
                            System.out.println(e.getMessage());
                        } catch (ClassNotFoundException e) {
                            System.out.println("tece pe aici2");
                            LoggerService.shared.sendLogToLogger(e.getMessage());
                        }
                    }

                    LoggerService.shared.sendLogToLogger("Broker-ul " + InetAddressUtils.boldedHostAddress() + " a fost oprit");

                } catch (IOException e) {
                    LoggerService.shared.sendLogToLogger(e.getMessage());
                }
            }
        });
    }

    private void process(BrokerMessage mesajReceptionat, ObjectOutputStream oos, ObjectInputStream ois, Socket clientSocket) throws IOException, ClassNotFoundException, InterruptedException {
        switch (mesajReceptionat.primesteComanda()) {
            case "publica":
                publish(mesajReceptionat, oos);
                break;
            case "articole":
                provideNews(oos);
                break;
            case "replicare":
                replicate(mesajReceptionat, oos, clientSocket);
                break;
            case "heartbeat":
                heartbeat(mesajReceptionat, oos);
            default:
                break;
        }
    }

    private void heartbeat(BrokerMessage mesajReceptionat, ObjectOutputStream oos) throws IOException, ClassNotFoundException {
        BrokerMessage raspuns = new BrokerMessage("heartbeat receptionat", adresaPersonala);;
        Boolean writeCompleted = false;

        oos.writeObject(raspuns);
        writeCompleted = true;
        String status = writeCompleted ? "DA" : "NU";
        String boldedIP = StringUtils.applyBoldTo(mesajReceptionat.primesteMesaj(), false);

        LoggerService.shared.sendLogToLogger2("Avem conexiune intre "
                + getNodCurent().getHostAddress()
                + " si "
                + getNodUrmator().getHostAddress()
                + "\n - raspuns: " + status + "\n");
    }

    private void publish(BrokerMessage mesajReceptionat, ObjectOutputStream oos) throws ClassNotFoundException, IOException {
        BrokerMessage raspuns = new BrokerMessage("Publicare receptionata", adresaPersonala);
        BrokerMessage replica = new BrokerMessage("Replica mesaj, replicare articol!", adresaPersonala);

        LoggerService.shared.sendLogToLogger("\nBroker-ul " + InetAddressUtils.boldedHostAddress()  + " a primit mesaj de la publisher");

        listaStiri.adaugaStire(mesajReceptionat.primesteStirea());
        oos.writeObject(raspuns);

        LoggerService.shared.sendLogToLogger("Broker-ul " + InetAddressUtils.boldedHostAddress() + " transmite articolul introdus in intreg sistemul");
        replica.seteazaComanda("replicare");
        replica.seteazaStirea(mesajReceptionat.primesteStirea());

        replicaArticolLaVecin(replica);
    }

    private void provideNews(ObjectOutputStream oos) throws IOException {
        BrokerMessage raspuns = new BrokerMessage("Ti-am trimis lista de articole.", adresaPersonala);
        raspuns.seteazaListaStiri(listaStiri);

        LoggerService.shared.sendLogToLogger("\nAm primit mesaj de la subscriber");
        oos.writeObject(raspuns);
    }

    private void replicate(BrokerMessage mesajReceptionat, ObjectOutputStream oos, Socket clientSocket) throws IOException, ClassNotFoundException {
        BrokerMessage raspuns = new BrokerMessage("Am replicat stirea \"" + mesajReceptionat.primesteStirea().getTitlu() + "\"", adresaPersonala);

        LoggerService.shared.sendLogToLogger("\n==============================================================================\n"
                + "Broker-ul " + InetAddressUtils.boldedHostAddress()
                + "\n - a primit cerere de replicare de la broker-ul vecin " + clientSocket.getInetAddress().getHostAddress().toString());

        listaStiri.adaugaStire(mesajReceptionat.primesteStirea());
        oos.writeObject(raspuns);

        if (!mesajReceptionat.primesteAdresa().equals(getNodUrmator())) {
            LoggerService.shared.sendLogToLogger(" - incearca replicarea articolului la urmatorul vecin " + getNodUrmator());
            replicaArticolLaVecin(mesajReceptionat);
        } else {
            LoggerService.shared.sendLogToLogger(" - a ajuns la capat si nu mai replica si la nodul originar");
        }
    }

    // ========================================== send() ==========================================
    public void replicaArticolLaVecin(BrokerMessage pachetReplica) {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        Socket socketComuicare = null;

        try {
//            if (RingManager.shared.getNodUrmator() == null) {
//                RingManager.shared.selectNewSuccessor();
//                LoggerService.shared.sendLogToLogger(" - replicarea articolului la vecin a esuat: nodUrmator este null" +
//                        "\n iar nodul curent este " + RingManager.shared.getNodCurrent().getHostAddress());
//                return;
//            }

            BrokerMessage raspuns;
            socketComuicare = new Socket(getNodUrmator(), SystemSetup.port);

            oos = new ObjectOutputStream(socketComuicare.getOutputStream());
            ois = new ObjectInputStream(socketComuicare.getInputStream());

            oos.writeObject(pachetReplica);
            oos.flush();

            raspuns = (BrokerMessage) ois.readObject();

            if (raspuns != null) {
                LoggerService.shared.sendLogToLogger(" - dupa replicare a primit de la vecinul urmator (" + getNodUrmator() + ") mesajul: " + raspuns.primesteMesaj());
            }

            oos.close();
            ois.close();
            socketComuicare.close();
        } catch (IOException e) {
            LoggerService.shared.sendLogToLogger(" - replicarea articolului la vecin a esuat: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            LoggerService.shared.sendLogToLogger(" - replicarea articolului la vecin a esuat (ClassNotFoundException): " + e.getMessage());
        }
    }


    private void userInputHandler() throws UnknownHostException {
        while (this.programIsRunning.get()) {
            Console consola = System.console();
            switch (consola.readLine("-> ")) {
                case "x": {
                    stopHeartbeat();
                    this.programIsRunning.set(false);

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
    }

    // ========================================== toleranta la defectare ==========================================
    private void stopHeartbeat() {
//        ringManager.stopHeartbeat();
    }

    public InetAddress getAdresaPersonala() {
        return adresaPersonala;
    }

    public void setAdresaPersonala(InetAddress adresaPersonala) {
        this.adresaPersonala = adresaPersonala;
    }
}
