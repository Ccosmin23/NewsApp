package service;

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

import model.broker.BrokerMessage;
import model.news.NewsField;

public final class BrokerService {
    InetAddress adresaPersonala;
    InetAddress nodUrmator;
    
    ServerSocket receiverSocket;
    ArrayList<InetAddress> adreseNoduri;
    AtomicBoolean ruleaza;
    NewsField listaStiri;

    public static BrokerService shared = new BrokerService();

    public BrokerService() {
        this.adreseNoduri = getInetAddresses();
        this.listaStiri = new NewsField(1, "Stiri");
    }

    public void start () throws SocketException, UnknownHostException {
        Enumeration<NetworkInterface> interfeteRetea = NetworkInterface.getNetworkInterfaces();
        InetAddress adresaGazda = null;

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

        if (adreseNoduri.contains(adresaGazda)) {
            this.nodUrmator = adreseNoduri.get((adreseNoduri.indexOf(adresaGazda) + 1) % adreseNoduri.size());
        }

        if (adresaGazda != null) {
            adresaPersonala = InetAddress.getByAddress(adresaGazda.getAddress());
        } else {
            throw new IllegalStateException("Could not find a suitable address for adresaGazda");
        }

        this.ruleaza = new AtomicBoolean(true);
        receive().start();
        while (this.ruleaza.get() == true) {
            Console consola = System.console();
            switch (consola.readLine("-> ")) {
                case "s": {
                    try {
                        send(InetAddress.getByName("192.168.30.10"));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                }

                case "x": {
                    this.ruleaza.set(false);
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

        System.out.println("AM TERMINAT!");
    }

    public void send (InetAddress destinatie) throws ClassNotFoundException {
        try {
            ObjectOutputStream oos;
            ObjectInputStream ois;
            BrokerMessage msg = new BrokerMessage("Hello", destinatie);
            BrokerMessage raspuns;
            Socket socketComuicare = new Socket(nodUrmator, 9700);

            oos = new ObjectOutputStream(socketComuicare.getOutputStream());
            ois = new ObjectInputStream(socketComuicare.getInputStream());

            oos.writeObject(msg);
            oos.flush();

            raspuns = (BrokerMessage) ois.readObject();
            System.out.println("Mesajul de la vecin: " + raspuns.primesteMesaj());

            oos.close();
            ois.close();
            socketComuicare.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void replicaArticolLaVecin (BrokerMessage pachetReplica) throws ClassNotFoundException {
        try {
            ObjectOutputStream oos;
            ObjectInputStream ois;
            BrokerMessage raspuns;
            Socket socketComuicare = new Socket(nodUrmator, 9700);

            oos = new ObjectOutputStream(socketComuicare.getOutputStream());
            ois = new ObjectInputStream(socketComuicare.getInputStream());

            oos.writeObject(pachetReplica);
            oos.flush();

            raspuns = (BrokerMessage) ois.readObject();
            System.out.println("Mesajul de la vecin: " + raspuns.primesteMesaj());

            oos.close();
            ois.close();
            socketComuicare.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Thread receive () {
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
                    while (ruleaza.get() == true) {
                        try {
                            clientSocket = receiverSocket.accept();
                            System.out.println("Mesajul de la " + clientSocket.getInetAddress().toString() + " este receptionat!");

                            clientIStream = new DataInputStream(clientSocket.getInputStream());
                            clientOStream = new DataOutputStream(clientSocket.getOutputStream());

                            ois = new ObjectInputStream(clientIStream);
                            oos = new ObjectOutputStream(clientOStream);

                            mesajReceptionat = (BrokerMessage) ois.readObject();

                            System.out.println(mesajReceptionat.primesteComanda());
                            switch (mesajReceptionat.primesteComanda()) {
                                case "publica": {
                                    BrokerMessage raspuns = new BrokerMessage("Ti-am receptionat publicarea!", adresaPersonala);
                                    BrokerMessage replica = new BrokerMessage("Replica mesaj, replicare articol!", adresaPersonala);

                                    System.out.println("Am primit mesaj de la PUBLISHER (publicator)!");
                                    listaStiri.adaugaStire(mesajReceptionat.primesteStirea());
                                    oos.writeObject(raspuns);

                                    System.out.println("Aduc si la restul sistemului articolul introdus.");
                                    replica.seteazaComanda("replicare");
                                    replica.seteazaStirea(mesajReceptionat.primesteStirea());

                                    replicaArticolLaVecin(replica);

                                    break;
                                }

                                case "articole": {
                                    BrokerMessage raspuns = new BrokerMessage("Ti-am receptionat nevoia de date (articolele in acest caz)!", adresaPersonala);

                                    raspuns.seteazaListaStiri(listaStiri);

                                    System.out.println("Am primit mesaj de la SUBSCRIBER (abonat)!");
                                    oos.writeObject(raspuns);
                                    break;
                                }

                                case "replicare": {
                                    BrokerMessage raspuns = new BrokerMessage("Am replicat stirea \"" + mesajReceptionat.primesteStirea().getTitlu() + "\"", adresaPersonala);

                                    System.out.println("Am primit cerere de replicare de la broker-ul vecin " + clientSocket.getInetAddress().toString());

                                    listaStiri.adaugaStire(mesajReceptionat.primesteStirea());
                                    oos.writeObject(raspuns);

                                    if (mesajReceptionat.primesteAdresa().equals(nodUrmator) != true) {
                                        System.out.println("Îl replic articolul la următorul vecin.");
                                        replicaArticolLaVecin(mesajReceptionat);
                                    } else {
                                        System.out.println("Nu mai replic și la nodul originar.");
                                    }

                                    break;
                                }

                                default: {
                                    if (mesajReceptionat.primesteAdresa().equals(adresaPersonala)) {
                                        BrokerMessage raspuns = new BrokerMessage("Sunt eu. Mulțumesc pentru mesaj!", adresaPersonala);

                                        System.out.println("Sunt eu!");
                                        oos.writeObject(raspuns);
                                    } else {
                                        BrokerMessage raspuns = new BrokerMessage("Nu sunt eu. Însă am trimis mai departe", adresaPersonala);

                                        System.out.println("Nu sunt eu!");
                                        oos.writeObject(raspuns);
                                        Thread.sleep(500);
                                        send(mesajReceptionat.primesteAdresa());
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

                    System.out.println("AM ÎNCHEIAT ASCULTAREA!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private ArrayList<InetAddress> getInetAddresses() {
        ArrayList<InetAddress> inetAddressList = new ArrayList<>();

        try {
            inetAddressList.add(InetAddress.getByName("192.168.30.4"));
            inetAddressList.add(InetAddress.getByName("192.168.30.7"));
            inetAddressList.add(InetAddress.getByName("192.168.30.9"));
            inetAddressList.add(InetAddress.getByName("192.168.30.10"));
            inetAddressList.add(InetAddress.getByName("192.168.30.12"));

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return inetAddressList;
    }
}
