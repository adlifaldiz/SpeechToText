package com.example.ocrtest.Model;

import com.google.gson.annotations.SerializedName;

public class IdiomModel {
    @SerializedName("id")
    private String id;

    @SerializedName("pribasa")
    private String pribasa;

    @SerializedName("ejaan")
    private String ejaan;


    @SerializedName("makna")
    private String makna;

    public IdiomModel(String id, String pribasa, String ejaan, String makna) {
        this.id = id;
        this.pribasa = pribasa;
        this.ejaan = ejaan;
        this.makna = makna;
    }

    public String getEjaan() {
        return ejaan;
    }

    public void setEjaan(String ejaan) {
        this.ejaan = ejaan;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPribasa() {
        return pribasa;
    }

    public void setPribasa(String pribasa) {
        this.pribasa = pribasa;
    }

    public String getMakna() {
        return makna;
    }

    public void setMakna(String makna) {
        this.makna = makna;
    }
}
