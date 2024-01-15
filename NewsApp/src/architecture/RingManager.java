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
        String userInput;
        boolean continueInput = true;

        while (continueInput) {
            startMessage();
            userInput = scanner.nextLine();

            if ("da".equalsIgnoreCase(userInput)) {
                System.out.println("\nIntroduceti adresa IP address a noului broker:");
                userInput = scanner.nextLine();

                // validam adresa IP si o adaugam in lista de brokeri
                validate(userInput);

            } else if ("nu".equalsIgnoreCase(userInput)) {
              stopProgram();
            } else {
                userEnteredATypo();
            }
        }

        scanner.close();
    }

    private void validate(String userInput) {
        if (isValidIpAddress(userInput)) {
            if (!checkIfBrokerAlreadyExist(userInput)) {
                createBrokerWith(userInput);
            } else {
                userAlreadyExistsMessage();
            }

        } else {
            wrongIPAddress();
        }
    }

    // verificam daca exista deja broker-ul in lista noastra
    private boolean checkIfBrokerAlreadyExist(String ipAddress) {
        for (RunningBroker broker : listOfRunningRunningBrokers) {
            if (broker.getAddress().getHostAddress().equals(ipAddress)) {
                return true;
            }
        }

        return false;
    }

    private void createBrokerWith(String ipAddress) {
        RunningBroker newBroker = null;

        try {
            newBroker = new RunningBroker(InetAddress.getByName(ipAddress), true);

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        listOfRunningRunningBrokers.add(newBroker);
        System.out.println("Am adaugat cu succes noul broker cu adresa IP: " + ipAddress);
    }

    private void wrongIPAddress() {
        System.out.println("====================================================================" +
                "\n!!! Ati introdus o adresa IP gresita. Va rugam introduceti dinou." +
                "\n====================================================================\n");
    }

    private void userEnteredATypo() {
        System.out.println("Ati tastat gresit, va rugam introduceti unul din raspunsurile: 'da' sau 'nu'.");
    }

    private void userAlreadyExistsMessage() {
        System.out.println("Un broker cu aceeasi adresa IP exista deja. Adaugarea a fost omisa.");
    }

    private boolean isValidIpAddress(String ipAddress) {
        // Regex de la 0 pana la 255.
        String zeroTo255 = "(\\d{1,2}|(0|1)\\d{2}|2[0-4]\\d|25[0-5])";

        // Regex pentru un digit de la 0 pana la  255 si urmat de caracterul punct '.', repetat de 4 ori
        // dupa proprietatea regex o sa validam fiecare adresa IP
        String regex = zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255;

        // compilam regex-ul
        Pattern p = Pattern.compile(regex);

        // daca IP-ul este empty atunci returnam fals
        if (ipAddress == null) {
            return false;
        }

        // acum cautam intre adresa IP si expresia regulata formata pe baza regex-ului
        Matcher m = p.matcher(ipAddress);

        //si in final returnam daca ip-ul face matching cu regex-ul nostru
        return m.matches();
    }

    private void startMessage() {
        System.out.println("\nDoriti sa adaugati o noua adresa IP in sistem?" +
                "\n - daca da, atunci noi vom crea un nou broker cu aceasta adresa" +
                "\n - daca nu, vom merge mai departe" +
                "\n\n(raspundeti cu 'da' sau 'nu')");
    }

    private void stopProgram() {
        System.out.println("ati ales nu, prin urmare vom inchide executia");
        System.exit(0);
    }
}
