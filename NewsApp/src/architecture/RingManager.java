package architecture;

import model.broker.BrokerMessage;
import model.broker.RunningBroker;
import service.BrokerService;
import service.LoggerService;
import utils.StringUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RingManager {
    public static RingManager shared = new RingManager();
    private final BrokerService brokerService;
    private final Timer heartbeatTimer;
    public ArrayList<RunningBroker> listOfRunningRunningBrokers = new ArrayList<>();

    public RingManager() {
        this.brokerService = new BrokerService();
        this.heartbeatTimer = new Timer();
    }

    public RingManager(BrokerService brokerService) {
        this.brokerService = brokerService;
        this.heartbeatTimer = new Timer();
    }

    public void start() {
        userInputHandler();
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
//            if (!InetAddress.getLocalHost().getHostAddress().equals("192.168.30.10")) {
//                brokerService.send(InetAddress.getByName("192.168.30.10"));
//                send(InetAddress.getByName("192.168.30.10"));
//            }

            send(nextNode);

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

            Socket socketComuicare = new Socket(destinatie, 9700);
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
        int currentSuccessorIndex = brokerService.getAdreseNoduri().indexOf(brokerService.getNodUrmator());
        int lengthOfAddressList = brokerService.getAdreseNoduri().size();
        int newSuccessorIndex = (currentSuccessorIndex + 1) % lengthOfAddressList;

        InetAddress newSuccessor = brokerService.getAdreseNoduri().get(newSuccessorIndex);
        brokerService.setNodUrmator(newSuccessor);
    }

    // aici demonstram ca s-a facut update-ul in arhitectura
    private void updateRingStructure() {

        LoggerService.shared.sendLogToLogger2("\n==========================================================\n" +
                "Sistemul reconfigurat:\n");

        for (RunningBroker runningBroker : listOfRunningRunningBrokers) {
            boolean isReachable = isReachable(runningBroker.getAddress());
            boolean hasARunningBroker = hasABrokerAssignedFor(runningBroker.getAddress());

            if (isReachable && hasARunningBroker) {
                String hostAddress = runningBroker.getAddress().getHostAddress();
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

    private boolean hasABrokerAssignedFor(InetAddress address) {
        return true;
    }

    public void stopHeartbeat() {
        heartbeatTimer.cancel();
        heartbeatTimer.purge();
    }

    public String hostAddress() {
        String hostAddress = "";

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();

                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();

                    if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress()) {
                        hostAddress = inetAddress.getHostAddress();
                    }
                }
            }

        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        return hostAddress;
    }

    public String boldedHostAddress() {
        return StringUtils.applyBoldTo(hostAddress(), false);
    }

    // ====================================== instantiere dinamica a brokerilor =======================================================

    public void userInputHandler() {
        Scanner scanner = new Scanner(System.in);
        String input;
        boolean continueInput = true;

        while (continueInput) {
            System.out.println("\nDoriti sa adaugati o noua adresa IP in sistem?" +
                    "\n - daca da, atunci noi vom crea un nou broker cu aceasta adresa" +
                    "\n - daca nu, vom merge mai departe" +
                    "\n\n(raspundeti cu 'da' sau 'nu')");
            input = scanner.nextLine();

            if ("da".equalsIgnoreCase(input)) {
                System.out.println("\nIntroduceti adresa IP address a noului broker:");
                input = scanner.nextLine();

                // Validate and add the IP address
                if (isValidIpAddress(input)) {
                    RunningBroker newBroker = null;

                    try {
                        newBroker = new RunningBroker(InetAddress.getByName(input), true);

                    } catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    }

                    listOfRunningRunningBrokers.add(newBroker);
                    System.out.println("Am adaugat cu succes noul broker cu adresa IP: " + input);

                } else {
                    System.out.println("====================================================================" +
                            "\n!!! Ati introdus o adresa IP gresita. Va rugam introduceti dinou." +
                            "\n====================================================================\n");
                }
            } else if ("nu".equalsIgnoreCase(input)) {
                continueInput = false;
                System.out.println("ati ales nu, prin urmare vom inchide executia");
                System.exit(0);

            } else {
                System.out.println("Ati tastat gresit, va rugam introduceti unul din raspunsurile: 'da' sau 'nu'.");
            }
        }

        scanner.close();
    }

    private boolean isValidIpAddress(String ipAddress) {
        // Regex for digit from 0 to 255.
        String zeroTo255 = "(\\d{1,2}|(0|1)\\d{2}|2[0-4]\\d|25[0-5])";
        // Regex for a digit from 0 to 255 and followed by a dot, repeated 4 times.
        // This is the regex to validate an IP address.
        String regex = zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255;

        // Compile the ReGex
        Pattern p = Pattern.compile(regex);

        // If the IP address is empty return false
        if (ipAddress == null) {
            return false;
        }

        // Pattern class contains matcher() method to find matching between given IP address and regular expression.
        Matcher m = p.matcher(ipAddress);

        // Return if the IP address matched the ReGex
        return m.matches();
    }
}
