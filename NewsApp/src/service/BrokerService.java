package service;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;

import architecture.RingManager;
import model.broker.BrokerMessage;
import model.broker.RunningBroker;
import model.news.NewsField;
import model.news.NewsStory;
import utils.InetAddressUtils;
import utils.StringUtils;
import utils.SystemSetup;

import static utils.SystemSetup.port;

public class BrokerService implements Serializable {
//    public BrokerService shared = new BrokerService();

    private String operationMessage = "";
    private InetAddress adresaPersonala;
    private ServerSocket receiverSocket;
    private AtomicBoolean programIsRunning;

    public BrokerService(InetAddress adresaPersonala) {
        this.adresaPersonala = adresaPersonala;
    }

    // ===================================
    //proprietatile nefolositoare
    NewsField listaStiri;
    InetAddress nodUrmator;
//    ArrayList<InetAddress> adreseNoduri;


    public BrokerService() {
        this.listaStiri = new NewsField(1, "Stiri");
        adresaPersonala = InetAddressUtils.hostAddress();
//        RingManager.shared.appendToRingManagerThis(this);

//        this.adresaPersonala = RingManager.shared.hostAddress();
//         this.boldedHostAddress = RingManager.shared.boldedHostAddress();
//        this.ringManager = new RingManager(this);
//        this.adreseNoduri = getRingManagerAddresses();
    }

//    private ArrayList<InetAddress> getRingManagerAddresses() {
//        ArrayList<InetAddress> addresses = new ArrayList<>();
//
//        for (RunningBroker broker : RingManager.shared.listOfBrokers) {
//            addresses.add(broker.getAddress());
//        }
//
//        return addresses;
//    }


    public void appendNewBroker() {
//        BrokerService firstBrokerService = null;

        try {
            ObjectOutputStream objectOutputStream;
            ObjectInputStream objectInputStream;

            Socket socketComunicare = new Socket(SystemSetup.ringManagerIpAddress, SystemSetup.port);

            objectOutputStream = new ObjectOutputStream(socketComunicare.getOutputStream());
//            objectInputStream = new ObjectInputStream(socketComunicare.getInputStream());

            InetAddress address = InetAddressUtils.hostAddress();
            System.out.println("o sa folosim adresa = " + address);

//            BrokerService brokerService = new BrokerService("add new broker", address);
            objectOutputStream.writeObject(InetAddressUtils.hostAddress());
            objectOutputStream.flush();

//            firstBrokerService = (BrokerService) objectInputStream.readObject();
//            System.out.println("avem adresa " + firstBrokerService.adresaPersonala);

            socketComunicare.close();
            objectOutputStream.close();
//            objectInputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
//        catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }
    }

    // ========================================== start() ==========================================
    public void start() throws UnknownHostException, SocketException {
//        findSuitableHostAddress();
//        ringManager.startHeartbeat();
        receive().start();
        userInputHandler();
    }

    private void findSuitableHostAddress() throws SocketException, UnknownHostException {
        InetAddress adresaGazda = searchHostAddressIntoLocalMachine();

//        if (adreseNoduri.contains(adresaGazda)) {
//            this.nodUrmator = adreseNoduri.get((adreseNoduri.indexOf(adresaGazda) + 1) % adreseNoduri.size());
//        }

        if (adresaGazda != null) {
            adresaPersonala = InetAddress.getByAddress(adresaGazda.getAddress());
        } else {
            LoggerService.shared.sendLogToLogger("nu se poate gasi o adresa gazda");
        }
    }

    private InetAddress searchHostAddressIntoLocalMachine() throws SocketException {
        InetAddress adresaGazda = null;
        Enumeration<NetworkInterface> interfeteRetea = NetworkInterface.getNetworkInterfaces();

        while (interfeteRetea.hasMoreElements()) {
            NetworkInterface networkInterface = interfeteRetea.nextElement();
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();

            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();

//                if (adreseNoduri.contains(inetAddress) && !inetAddress.isLoopbackAddress()) {
//                    adresaGazda = inetAddress;
//                    break;
//                }
            }

            if (adresaGazda != null) {
                break;
            }
        }

        return adresaGazda;
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

    private void heartbeat(BrokerMessage mesajReceptionat, ObjectOutputStream oos) throws IOException {
        BrokerMessage raspuns = new BrokerMessage("heartbeat receptionat", adresaPersonala);;
        Boolean writeCompleted = false;

        oos.writeObject(raspuns);
        writeCompleted = true;
        String status = writeCompleted ? "DA" : "NU";
        String boldedIP = StringUtils.applyBoldTo(mesajReceptionat.primesteMesaj(), false);

        LoggerService.shared.sendLogToLogger2("Avem conexiune intre "
                + InetAddressUtils.boldedHostAddress()
                + " si "
                + boldedIP
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
        BrokerMessage raspuns = new BrokerMessage("Ti-am receptionat nevoia de date (articolele in acest caz)!", adresaPersonala);
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

        if (!mesajReceptionat.primesteAdresa().equals(nodUrmator)) {
            LoggerService.shared.sendLogToLogger(" - incearca replicarea articolului la urmatorul vecin " + nodUrmator);
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
            BrokerMessage raspuns;
            socketComuicare = new Socket(nodUrmator, SystemSetup.port);

            oos = new ObjectOutputStream(socketComuicare.getOutputStream());
            ois = new ObjectInputStream(socketComuicare.getInputStream());

            oos.writeObject(pachetReplica);
            oos.flush();

            raspuns = (BrokerMessage) ois.readObject();

            if (raspuns != null) {
                LoggerService.shared.sendLogToLogger(" - dupa replicare a primit de la vecinul urmator (" + nodUrmator + ") mesajul: " + raspuns.primesteMesaj());
            }

        } catch (IOException e) {
            LoggerService.shared.sendLogToLogger(" - replicarea articolului la vecin a esuat: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            LoggerService.shared.sendLogToLogger(" - replicarea articolului la vecin a esuat (ClassNotFoundException): " + e.getMessage());
        } finally {
            try {
                ois.close();
                oos.close();
                socketComuicare.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private void userInputHandler() throws UnknownHostException {
        while (this.programIsRunning.get()) {
            Console consola = System.console();
            switch (consola.readLine("-> ")) {
                case "s":
//                    send(InetAddress.getByName("192.168.30.10"));
                    break;
                case "d":
                    appendNewBroker();
                    break;

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

    // ========================================== IP addresses ==========================================
//    public ArrayList<InetAddress> getAdreseNoduri() {
//        return adreseNoduri;
//    }
//
//    public void setAdreseNoduri(ArrayList<InetAddress> adreseNoduri) {
//        this.adreseNoduri = adreseNoduri;
//    }

    public InetAddress getNodUrmator() {
        return nodUrmator;
    }

    public void setNodUrmator(InetAddress nodUrmator) {
        this.nodUrmator = nodUrmator;
    }

    public InetAddress getAdresaPersonala() {
        return InetAddressUtils.hostAddress();
    }

    public void setAdresaPersonala(InetAddress adresaPersonala) {
        this.adresaPersonala = adresaPersonala;
    }

    public String getOperationMessage() {
        return operationMessage;
    }

    public void setOperationMessage(String operationMessage) {
        this.operationMessage = operationMessage;
    }
}
