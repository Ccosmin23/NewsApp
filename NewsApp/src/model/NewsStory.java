package model;

import java.io.Serializable;

public class NewsStory implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String titlu;
    private String continut;

    public NewsStory(Integer id, String titlu, String continut) {
        this.id = id;
        this.titlu = titlu;
        this.continut = continut;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitlu() {
        return titlu;
    }

    public void setTitlu(String titlu) {
        this.titlu = titlu;
    }

    public String getContinut () {
        return this.continut;
    }

    public void setContinut (String continut) {
        this.continut = continut;
    }
}
