package model;

import java.io.Serializable;
import java.net.InetAddress;

public class TestData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String mesaj;
    private String comanda;
    private InetAddress destinatie;

    public String primesteMesaj () {
        return this.mesaj;
    }

    public InetAddress primesteAdresa () {
        return this.destinatie;
    }

    public void seteazaComanda (String comandaData) {
        this.comanda = comandaData;
    }

    public String primesteComanda () {
        return this.comanda;
    }

    public TestData (String msg, InetAddress dest) {
        this.destinatie = dest;
        this.mesaj = msg;
        this.comanda = "";
    }
}