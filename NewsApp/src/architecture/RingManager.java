package architecture;

import model.broker.RunningBroker;
import service.BrokerService;
import service.LoggerService;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class RingManager {
    private final BrokerService brokerService;
    private final Timer heartbeatTimer;
    public ArrayList<RunningBroker> listOfRunningRunningBrokers = new ArrayList<>();

    public RingManager(BrokerService brokerService) {
        this.brokerService = brokerService;
        this.heartbeatTimer = new Timer();
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

            if (brokerService.getNodUrmator() != null) {
                nextNode = brokerService.getNodUrmator();
            } else {
                nextNode = brokerService.getAdreseNoduri().get(0);
            }

            //doar pentru teste
<<<<<<< HEAD
            // if (!InetAddress.getLocalHost().getHostAddress().equals("192.168.37.12")) {
            //     brokerService.send(InetAddress.getByName("192.168.37.12"));
            // }
=======
//            if (!InetAddress.getLocalHost().getHostAddress().equals("192.168.30.10")) {
//                brokerService.send(InetAddress.getByName("192.168.30.10"));
//            }
>>>>>>> origin/generare_stiri

//            brokerService.send(nextNode);

        } catch (Exception e) {
            LoggerService.shared.sendLogToLogger("Avem o eroare neasteptata in metoda heartbeat: " + e);
            e.printStackTrace();
        }
    }

    public void handleNodeFailure() {
        LoggerService.shared.sendLogToLogger("\nAvem un esec pe nod. Incepem reconfigurarea sistemului...");

        selectNewSuccessor();
        updateRingStructure();

        LoggerService.shared.sendLogToLogger("Reconfigurare efectuata cu success. Un nou successor a fost selectat.");
    }

    // aici selectam noul nod successor dupa ce o am primit un fail
    // incercand sa selectam urmatorul nod din lista de adrese
    // in prima faza, luam index-ul de la successor-ul curent
    // calculeam indexul noului succesor prin deplasarea la urmatorul nod din lista
    // vom folosi operatorul modulo %, pentru a obtine un inel circular
    // pe urma luam adresa noului succesor si o setam la nodul urmator din BrokerService
    private void selectNewSuccessor() {
        int currentSuccessorIndex = brokerService.getAdreseNoduri().indexOf(brokerService.getNodUrmator());
        int lengthOfAddressList = brokerService.getAdreseNoduri().size();
        int newSuccessorIndex = (currentSuccessorIndex + 1) % lengthOfAddressList;

        InetAddress newSuccessor = brokerService.getAdreseNoduri().get(newSuccessorIndex);
        brokerService.setNodUrmator(newSuccessor);
    }

    // aici demonstram ca s-a facut update-ul in arhitectura
    private void updateRingStructure() {

        LoggerService.shared.sendLogToLogger("\n==========================================================\n" +
                "Sistemul reconfigurat:\n");

        for (RunningBroker runningBroker : listOfRunningRunningBrokers) {
            boolean isReachable = isReachable(runningBroker.getAddress());
            boolean hasARunningBroker = hasABrokerAssignedFor(runningBroker.getAddress());

            if (isReachable && hasARunningBroker) {
                String hostAddress = runningBroker.getAddress().getHostAddress();
                LoggerService.shared.sendLogToLogger(hostAddress);
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

    private boolean hasABrokerAssignedFor(InetAddress address) {
        return true;
    }

    public void stopHeartbeat() {
        heartbeatTimer.cancel();
        heartbeatTimer.purge();
    }
}
