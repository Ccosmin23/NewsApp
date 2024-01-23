package architecture;

import model.broker.BrokerMessage;
import model.broker.RunningBroker;
import service.BrokerService;
import service.LoggerService;
import utils.InetAddressUtils;
import utils.StringUtils;
import utils.SystemSetup;

import java.io.*;
import java.net.*;
import java.util.*;

public final class RingManager {
    public static RingManager shared = new RingManager();
    Boolean programIsRunning = true;

    private Socket clientSocket;
    private ServerSocket serverSocket;

    private final Timer heartbeatTimer;
    public ArrayList<BrokerService> listOfBrokers = new ArrayList<>();

    private BrokerService nodCurrent;
    private BrokerService nodUrmator;

    public RingManager() {
        this.heartbeatTimer = new Timer();
        startHeartbeat();
    }

    public void start() throws IOException, ClassNotFoundException {
        serverSocket = new ServerSocket(SystemSetup.port);
        DataInputStream clientIStream;
        DataOutputStream clientOStream;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        System.out.println("=======================================================================================");
        System.out.println("\tSalutare! Eu sunt ring manager-ul cu adresa IP " + InetAddressUtils.boldedHostAddress());
        System.out.println("\t\tpentru ca sa inchizi executia apasa CTRL+C apoi ENTER");
        System.out.println("========================================================================================\n");

        while (programIsRunning) {
            clientSocket = serverSocket.accept();

            clientIStream = new DataInputStream(clientSocket.getInputStream());
            clientOStream = new DataOutputStream(clientSocket.getOutputStream());

            ois = new ObjectInputStream(clientIStream);
            oos = new ObjectOutputStream(clientOStream);

            Object receivedObject = ois.readObject();

            if (receivedObject instanceof InetAddress) {
                createAndAppendNewBrokerWith(((InetAddress) receivedObject));
                initializeCurrentNode();
            } else if (receivedObject instanceof String && receivedObject.equals("get first broker")) {
                getFirstBrokerFromListWith(oos);
            } else if (receivedObject instanceof String && receivedObject.equals("get nod curent")) {
                getNodCurent(oos);
            } else if (receivedObject instanceof String && receivedObject.equals("get nod urmator")) {
                getNodUrmator(oos);
            }
        }

        ois.close();
        oos.close();
        clientSocket.close();
    }

    private void createAndAppendNewBrokerWith(InetAddress inetAddress) {
        BrokerService newBroker = new BrokerService(inetAddress);

        System.out.println("Am primit adresa IP " + newBroker.getAdresaPersonala() + " si am creat un broker cu aceasta adresa");
        listOfBrokers.add(newBroker);

        // daaca e primul broker din lista atunci setam nodul curent si urmator cu adresa broker-ului
        if (listOfBrokers.size() == 1) {
            nodCurrent = newBroker;
            nodUrmator = newBroker;
        } else {
            //altfel facem update si reconfiguram sistemul
            buildRingArchitecture();
        }
    }

    private void getFirstBrokerFromListWith(ObjectOutputStream oos) throws IOException {
        System.out.println("un publisher imi cere primul broker din lista");

        if (listOfBrokers.get(0) != null) {
            System.out.println("intr-adevar il avem si o sa il dam stiind ca are adresa IP " + listOfBrokers.get(0).getAdresaPersonala());
            System.out.println("LISTA NOASTRA CONTINE URMATOARELE:");
            printAll();

            oos.writeObject(listOfBrokers.get(0).getAdresaPersonala());
            buildRingArchitecture();
        } else {
            System.out.println("dar nu avem nicun broker momentan");
        }
    }

    private void getNodCurent(ObjectOutputStream oos) throws IOException {
        if (nodCurrent != null) {
//            System.out.println("avem nodul curent cu adresa IP = " + nodCurrent.getAdresaPersonala());
            oos.writeObject(nodCurrent.getAdresaPersonala());
        } else {
            System.out.println("nu avem nod curent");
        }
    }

    private void getNodUrmator(ObjectOutputStream oos) throws IOException {
        if (nodUrmator != null) {
//            System.out.println("avem un nod urmator care are adresa IP = " + nodUrmator.getAdresaPersonala());
            oos.writeObject(nodUrmator.getAdresaPersonala());
            selectNewSuccessor();

        } else {
            System.out.println("nu avem un nod urmator");
        }
    }

    private void printAll() {
        for(BrokerService br: listOfBrokers) {
            System.out.println(br.getAdresaPersonala());
        }
        System.out.println("\n============\n");
    }


    // =====================================    TOLERANTA LA DEFECTARE =======================================================
    public void startHeartbeat() {
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendHeartbeat();
            }
        }, 0, 5000); // la fiecare 5 secunde se trimite un semnal "heartbeat" ca sa verificam starea nodurile
    }

    private void sendHeartbeat() {
        try {
            InetAddress nextNode;

            if (getNodUrmator() != null) {
                nextNode = getNodUrmator();
                send(nextNode);
                selectNewSuccessor();
            }

        } catch (Exception e) {
            //aici intra in cazul in care nu avem conexiune intre noduri si se incerca un send() la fiecare heartbeat
//            LoggerService.shared.sendLogToLogger2("Avem o eroare neasteptata in metoda heartbeat: " + e);
//            e.printStackTrace();
        }
    }

    public void send(InetAddress destinatie) {
        try {
            BrokerMessage raspuns;
            ObjectOutputStream objectOutputStream;
            ObjectInputStream objectInputStream;

            BrokerMessage brokerMessage = new BrokerMessage(destinatie.getHostAddress(), destinatie);
            brokerMessage.seteazaComanda("heartbeat");

            Socket socketComuicare = new Socket(destinatie, SystemSetup.port);
            objectOutputStream = new ObjectOutputStream(socketComuicare.getOutputStream());
            objectInputStream = new ObjectInputStream(socketComuicare.getInputStream());

            objectOutputStream.writeObject(brokerMessage);
            objectOutputStream.flush();

            raspuns = (BrokerMessage) objectInputStream.readObject();
            LoggerService.shared.sendLogToLogger2(raspuns.primesteMesaj());

            socketComuicare.close();
            objectOutputStream.close();
            objectInputStream.close();

            // asignam nodUrmator la nodCurent ca sa verificam circular conexiunea intre noduri
            nodCurrent = nodUrmator;

        } catch (IOException e) {
            // aici avem eroare de Connection refused
            //ToDo: - de reconfigurat sistemul
            handleNodeFailure();

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeCurrentNode() {
        nodCurrent = listOfBrokers.get(0);
    }

    private void buildRingArchitecture() {
        if (!listOfBrokers.isEmpty()) {
            //verificam daca nodul curent este in lista
            int currentIndex = listOfBrokers.indexOf(nodCurrent);

            if (currentIndex != -1) {
                // calculam indexul nodului urmator
                // tinand cont ca trebuie sa ne raportam conform arhitecturii circulare
                int nextIndex = (currentIndex + 1) % listOfBrokers.size();

                //luam adresa si o asignam nodului curent
                nodCurrent = listOfBrokers.get(currentIndex);

                //setam adresa nodului urmator
                nodUrmator = listOfBrokers.get(nextIndex);
            } else {
                // un mic handling pentru cazul in care avem un singur nod in lista
                nodCurrent = listOfBrokers.get(0);
                nodUrmator = listOfBrokers.get(0);
            }
        }
    }

    public void handleNodeFailure() {
        LoggerService.shared.sendLogToLogger2("\n\t\tAvem un esec pe nod. Incepem reconfigurarea sistemului...");

        selectNewSuccessor();
        updateRingStructure();

        LoggerService.shared.sendLogToLogger2("Reconfigurare efectuata.");
    }

    // aici selectam noul nod successor dupa ce o am primit un fail
    // incercand sa selectam urmatorul nod din lista de adrese
    // in prima faza, luam index-ul de la successor-ul curent
    // calculeam indexul noului succesor prin deplasarea la urmatorul nod din lista
    // vom folosi operatorul modulo %, pentru a obtine un inel circular
    // pe urma luam adresa noului succesor si o setam la nodul urmator din BrokerService
    public void selectNewSuccessor() {
        if (!listOfBrokers.isEmpty()) {
            int currentSuccessorIndex = listOfBrokers.indexOf(nodUrmator);
            int newSuccessorIndex = (currentSuccessorIndex + 1) % listOfBrokers.size();
            nodUrmator = listOfBrokers.get(newSuccessorIndex);
        } else {
            System.out.println("EROARE FRATE");
        }
    }


    // aici demonstram ca s-a facut update-ul in arhitectura
    private void updateRingStructure() {

        LoggerService.shared.sendLogToLogger2("\n==========================================================\n" +
                "Sistemul reconfigurat:\n");

        for (BrokerService brokerService : listOfBrokers) {
            boolean isReachable = isReachable(brokerService.getAdresaPersonala());

            if (isReachable) {
                String hostAddress = brokerService.getAdresaPersonala().getHostAddress();
                LoggerService.shared.sendLogToLogger2(hostAddress);
            }
        }
    }

    private boolean isReachable(InetAddress address) {
        try {
            return address.isReachable(1000);
        } catch (IOException e) {
            return false;
        }
    }

    public void stopHeartbeat() {
        heartbeatTimer.cancel();
        heartbeatTimer.purge();
    }

    public InetAddress getNodCurrent() {
        return nodCurrent.getAdresaPersonala();
    }

    public void setNodCurrent(BrokerService nodCurrent) {
        this.nodCurrent = nodCurrent;
    }

    public InetAddress getNodUrmator() {
        return nodUrmator.getAdresaPersonala();
    }

    public void setNodUrmator(BrokerService nodUrmator) {
        this.nodUrmator = nodUrmator;
    }
}
