package architecture;

import java.io.ByteArrayInputStream;
import java.io.Console;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;

import model.TestData;

public class Broker {
    InetAddress adresaPersonala;
    InetAddress nodUrmator;
    ServerSocket receiverSocket;
    ArrayList<InetAddress> adreseNoduri;
    AtomicBoolean ruleaza;

    public void send (InetAddress destinatie) throws ClassNotFoundException {
        try {
            ObjectOutputStream oos;
            ObjectInputStream ois;
            TestData msg = new TestData("Hello", destinatie);
            TestData raspuns;
            Socket socketComuicare = new Socket(nodUrmator, 9700);

            oos = new ObjectOutputStream(socketComuicare.getOutputStream());
            ois = new ObjectInputStream(socketComuicare.getInputStream());

            oos.writeObject(msg);
            oos.flush();

            raspuns = (TestData) ois.readObject();
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
                TestData mesajReceptionat;

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

                            mesajReceptionat = (TestData) ois.readObject();

                            System.out.println(mesajReceptionat.primesteComanda());
                            switch (mesajReceptionat.primesteComanda()) {
                                case "publica": {
                                    TestData raspuns = new TestData("Ti-am receptionat publicarea!", adresaPersonala);

                                    System.out.println("Am primit mesaj de la PUBLISHER (publicator)!");
                                    oos.writeObject(raspuns);
                                    break;
                                }

                                case "articole": {
                                    TestData raspuns = new TestData("Ti-am receptionat nevoia de date (articolele in acest caz)!", adresaPersonala);

                                    System.out.println("Am primit mesaj de la SUBSCRIBER (abonat)!");
                                    oos.writeObject(raspuns);
                                    break;
                                }

                                default: {
                                    if (mesajReceptionat.primesteAdresa().equals(adresaPersonala)) {
                                        TestData raspuns = new TestData("Sunt eu. Mulțumesc pentru mesaj!", adresaPersonala);

                                        System.out.println("Sunt eu!");
                                        oos.writeObject(raspuns);
                                    } else {
                                        TestData raspuns = new TestData("Nu sunt eu. Însă am trimis mai departe", adresaPersonala);

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

    public void start () throws SocketException, UnknownHostException {
        Enumeration<NetworkInterface> interfeteRetea = NetworkInterface.getNetworkInterfaces();
        InetAddress adresaGazda = null;
        
        // Verifică dacă pe mașina care execută programul are o interfață cu adresa
        // ip din lista de adrese de mașini participante în rețeaua cu topologie de inel
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
        
        // Determină nodul următor cu care mașina broker va comunica folosind
        // lista cu adresele ip definite
        if (adreseNoduri.contains(adresaGazda)) {
            this.nodUrmator = adreseNoduri.get((adreseNoduri.indexOf(adresaGazda) + 1) % adreseNoduri.size());
        }

        adresaPersonala = InetAddress.getByAddress(adresaGazda.getAddress());

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

    public Broker (ArrayList<InetAddress> listaAdreseMasini) {
        this.adreseNoduri = listaAdreseMasini;
    }
}
