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
import model.news.NewsField;
import utils.InetAddressUtils;
import utils.StringUtils;

public final class BrokerService {
    public static BrokerService shared = new BrokerService();

    InetAddress adresaPersonala;
    InetAddress nodUrmator;
    ArrayList<InetAddress> adreseNoduri;

    ServerSocket receiverSocket;
    AtomicBoolean programIsRunning;
    NewsField listaStiri;

    private RingManager ringManager;
    private String boldedHostAddress; {
        try {
            boldedHostAddress = StringUtils.applyBoldTo(InetAddressUtils.getLocalAddress().getHostAddress(), false);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public BrokerService() {
        this.adreseNoduri = getInetAddresses();
        this.listaStiri = new NewsField(1, "Stiri");
        this.ringManager = new RingManager(this);
    }

    // ========================================== start() ==========================================
    public void start() throws UnknownHostException, SocketException {
        findSuitableHostAddress();
        ringManager.startHeartbeat();
        receive().start();
        userInputHandler();
    }

    private void findSuitableHostAddress() throws SocketException, UnknownHostException {
        InetAddress adresaGazda = searchHostAddressIntoLocalMachine();

        if (adreseNoduri.contains(adresaGazda)) {
            this.nodUrmator = adreseNoduri.get((adreseNoduri.indexOf(adresaGazda) + 1) % adreseNoduri.size());
        }

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
                if (adreseNoduri.contains(inetAddress) && !inetAddress.isLoopbackAddress()) {
                    adresaGazda = inetAddress;
                    break;
                }
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
                    receiverSocket = new ServerSocket(9700);
                    LoggerService.shared.sendLogToLogger("Broker-ul " + boldedHostAddress + " a fost pornit");

                    while (programIsRunning.get()) {
                        try {
                            clientSocket = receiverSocket.accept();
//                            LoggerService.shared.sendLogToLogger("Mesajul de la " + clientSocket.getInetAddress().toString() + " este receptionat!");

                            clientIStream = new DataInputStream(clientSocket.getInputStream());
                            clientOStream = new DataOutputStream(clientSocket.getOutputStream());

                            ois = new ObjectInputStream(clientIStream);
                            oos = new ObjectOutputStream(clientOStream);

                            mesajReceptionat = (BrokerMessage) ois.readObject();

                            if (mesajReceptionat != null) {
                                LoggerService.shared.sendLogToLogger(mesajReceptionat.primesteComanda());
                            }

                            try {
                                if (mesajReceptionat.primesteMesaj() == "Heartbeat") {
                                    LoggerService.shared.sendLogToLogger("- - - Heartbeat successful");
                                } else {
                                    process(mesajReceptionat, oos, ois, clientSocket);
                                }
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            } finally {
                                ois.close();
                                oos.close();
                                clientSocket.close();
                            }

                        } catch (SocketException e) {
                            System.out.println(e.getMessage());
                        } catch (ClassNotFoundException e) {
                            LoggerService.shared.sendLogToLogger(e.getMessage());
                        }
                    }

                    LoggerService.shared.sendLogToLogger("Broker-ul " + boldedHostAddress + " a fost oprit");

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
            default:
                break;
        }
    }

    private void publish(BrokerMessage mesajReceptionat, ObjectOutputStream oos) throws ClassNotFoundException, IOException {
        BrokerMessage raspuns = new BrokerMessage("Publicare receptionata", adresaPersonala);
        BrokerMessage replica = new BrokerMessage("Replica mesaj, replicare articol!", adresaPersonala);

        LoggerService.shared.sendLogToLogger("\nBroker-ul " + boldedHostAddress  + " a primit mesaj de la publisher");

        listaStiri.adaugaStire(mesajReceptionat.primesteStirea());
        oos.writeObject(raspuns);

        LoggerService.shared.sendLogToLogger("Broker-ul " + boldedHostAddress + " transmite articolul introdus in intreg sistemul");
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
                + "Broker-ul " + boldedHostAddress
                + "\n - a primit cerere de replicare de la broker-ul vecin " + clientSocket.getInetAddress().getHostAddress().toString());

        listaStiri.adaugaStire(mesajReceptionat.primesteStirea());
        oos.writeObject(raspuns);

        if (mesajReceptionat.primesteAdresa().equals(nodUrmator) != true) {
            LoggerService.shared.sendLogToLogger(" - incearca replicarea articolului la urmatorul vecin " + nodUrmator);
            replicaArticolLaVecin(mesajReceptionat);
        } else {
            LoggerService.shared.sendLogToLogger(" - a ajuns la capat si nu mai replica si la nodul originar");
        }
    }

    // ========================================== send() ==========================================
    public void send(InetAddress destination) {
        try {
            BrokerMessage msg = new BrokerMessage("heartbeat", destination);
            Socket socketCommunication = new Socket(destination, 9700);

            try (ObjectOutputStream oos = new ObjectOutputStream(socketCommunication.getOutputStream());
                 ObjectInputStream ois = new ObjectInputStream(socketCommunication.getInputStream())
            ) {

                // setam un timeout de 5 secunde)
                socketCommunication.setSoTimeout(5000);

                oos.writeObject(msg);
                oos.flush();

                try {
                    BrokerMessage response = (BrokerMessage) ois.readObject();

                    if (response != null) {
                        LoggerService.shared.sendLogToLogger("Am primit mesaj de la vecin: " + response.primesteMesaj());
                    } else {
                        LoggerService.shared.sendLogToLogger("Am primit un mesaj null de la vecin");
                    }
                } catch (EOFException e) {
                    LoggerService.shared.sendLogToLogger("Eroare EOFException in timpul heartbeat-ului "
                            + adresaPersonala
                            + " Este posibil ca conexiunea sa fi fost inchisa.");

                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

            } catch (IOException e) {
                LoggerService.shared.sendLogToLogger("Eroare in timpul heartbeat-ului: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    socketCommunication.close();
                } catch (IOException e) {
                    LoggerService.shared.sendLogToLogger("Eroare la inchiderea socket-ului: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            LoggerService.shared.sendLogToLogger("Eroare de stabilire a conexiunii: " + e.getMessage());
        }
    }

    public void replicaArticolLaVecin(BrokerMessage pachetReplica) {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        Socket socketComuicare = null;

        try {
            BrokerMessage raspuns;
            socketComuicare = new Socket(nodUrmator, 9700);

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
                oos.close();
                ois.close();
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
                    send(InetAddress.getByName("192.168.30.10"));
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
        ringManager.stopHeartbeat();
    }

    // ========================================== IP addresses ==========================================
    private ArrayList<InetAddress> getInetAddresses() {
        ArrayList<InetAddress> inetAddressList = new ArrayList<>();

        try {
            inetAddressList.add(InetAddress.getByName("192.168.30.4"));
            inetAddressList.add(InetAddress.getByName("192.168.30.7"));
            inetAddressList.add(InetAddress.getByName("192.168.30.9"));
            inetAddressList.add(InetAddress.getByName("192.168.30.10"));
            inetAddressList.add(InetAddress.getByName("192.168.30.12"));

        } catch (UnknownHostException e) {
            LoggerService.shared.sendLogToLogger(e.getMessage());
        }

        return inetAddressList;
    }

    public ArrayList<InetAddress> getAdreseNoduri() {
        return adreseNoduri;
    }

    public void setAdreseNoduri(ArrayList<InetAddress> adreseNoduri) {
        this.adreseNoduri = adreseNoduri;
    }

    public InetAddress getNodUrmator() {
        return nodUrmator;
    }

    public void setNodUrmator(InetAddress nodUrmator) {
        this.nodUrmator = nodUrmator;
    }

    public InetAddress getAdresaPersonala() {
        return adresaPersonala;
    }

    public void setAdresaPersonala(InetAddress adresaPersonala) {
        this.adresaPersonala = adresaPersonala;
    }
}
