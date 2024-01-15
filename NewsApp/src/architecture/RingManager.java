package architecture;

import model.broker.BrokerMessage;
import service.BrokerService;
import service.LoggerService;
import utils.InetAddressUtils;
import utils.SystemSetup;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;

public final class RingManager {
    public static RingManager shared = new RingManager();

    private String ringManagerIpAddress = "192.168.30.4";

    private Socket clientSocket;
    private ServerSocket serverSocket;

    private final Timer heartbeatTimer;
    public ArrayList<BrokerService> listOfBrokers = new ArrayList<>();

    //poate ca sunt inutile
    private BrokerService brokerService = null;

    public RingManager() {
        this.heartbeatTimer = new Timer();
    }

    public void start() throws IOException, ClassNotFoundException {
        serverSocket = new ServerSocket(SystemSetup.port);

        System.out.println("=======================================================================================");
        System.out.println("\tSalutare! Eu sunt ring manager-ul cu adresa IP " + InetAddressUtils.boldedHostAddress());
        System.out.println("\t\tpentru ca sa inchizi executia apasa CTRL+C apoi ENTER");
        System.out.println("========================================================================================\n");

        while (true) {
            clientSocket = serverSocket.accept();
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());

            //primeste si afiseaza mesajul
            BrokerService brokerService = (BrokerService) ois.readObject();

            if (brokerService != null) {
                listOfBrokers.add(brokerService);
                System.out.println("am primit un nou broker");
            }
        }
    }

    public void appendToRingManagerThis(BrokerService brokerService) {
        try (Socket socket = new Socket(ringManagerIpAddress, SystemSetup.port);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
            oos.writeObject(brokerService);

        } catch (IOException e) {
            //daca nu ramane commentat codul de mai jos o sa avem print-uri cand logger-ul nu e rulat
//            System.out.println("Nu putem loga mesajul datorita: " + e.getMessage());
        }
    }



    public BrokerService starPointBroker() {
        if (listOfBrokers.contains(0)) {
            return listOfBrokers.get(0);
        } else {
            return null;
        }
    }


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

//            if (brokerService.getNodUrmator() != null) {
//                nextNode = brokerService.getNodUrmator();
//            } else {
//                nextNode = brokerService.getAdreseNoduri().get(0);
//            }

            //doar pentru teste
//            if (!InetAddress.getLocalHost().getHostAddress().equals("192.168.30.10")) {
//                brokerService.send(InetAddress.getByName("192.168.30.10"));
//                send(InetAddress.getByName("192.168.30.10"));
//            }

//            send(nextNode);

        } catch (Exception e) {
            LoggerService.shared.sendLogToLogger2("Avem o eroare neasteptata in metoda heartbeat: " + e);
            e.printStackTrace();
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
//            LoggerService.shared.sendLogToLogger2(raspuns.primesteMesaj());

            socketComuicare.close();
            objectOutputStream.close();
            objectInputStream.close();

        } catch (IOException e) {
            // aici avem eroare de Connection refused
            //ToDo: - de reconfigurat sistemul
            handleNodeFailure();

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
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
    private void selectNewSuccessor() {
//        int currentSuccessorIndex = brokerService.getAdreseNoduri().indexOf(brokerService.getNodUrmator());
//        int lengthOfAddressList = brokerService.getAdreseNoduri().size();
//        int newSuccessorIndex = (currentSuccessorIndex + 1) % lengthOfAddressList;
//
//        InetAddress newSuccessor = brokerService.getAdreseNoduri().get(newSuccessorIndex);
//        brokerService.setNodUrmator(newSuccessor);
    }

    // aici demonstram ca s-a facut update-ul in arhitectura
    private void updateRingStructure() {

        LoggerService.shared.sendLogToLogger2("\n==========================================================\n" +
                "Sistemul reconfigurat:\n");

//        for (RunningBroker runningBroker : listOfRunningRunningBrokers) {
//            boolean isReachable = isReachable(runningBroker.getAddress());
//            boolean hasARunningBroker = hasABrokerAssignedFor(runningBroker.getAddress());
//
//            if (isReachable && hasARunningBroker) {
//                String hostAddress = runningBroker.getAddress().getHostAddress();
//                LoggerService.shared.sendLogToLogger2(hostAddress);
//            }
//        }
    }

    private boolean isReachable(InetAddress address) {
        try {
            return address.isReachable(1000);
        } catch (IOException e) {
            return false;
        }
    }

    private boolean hasABrokerAssignedFor(InetAddress address) {
        return true;
    }

    public void stopHeartbeat() {
//        heartbeatTimer.cancel();
//        heartbeatTimer.purge();
    }
}
