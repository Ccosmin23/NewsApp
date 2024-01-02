package model.broker;

import model.news.NewsField;
import model.news.NewsStory;

import java.io.Serializable;
import java.net.InetAddress;

public class MesajPachet implements Serializable {
    private static final long serialVersionUID = 1L;

    private String mesaj;
    private String comanda;
    private InetAddress destinatie;
    private NewsStory stire;
    private NewsField listaStiri;

    public NewsStory primesteStirea () {
        return this.stire;
    }

    public void seteazaStirea (NewsStory stirea) {
        this.stire = stirea;
    }

    public NewsField primesteListaStiri () {
        return this.listaStiri;
    }

    public void seteazaListaStiri (NewsField listaStiri) {
        this.listaStiri = listaStiri;
    }

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

    public MesajPachet (String msg, InetAddress dest) {
        this.destinatie = dest;
        this.mesaj = msg;
        this.comanda = "";
    }
}