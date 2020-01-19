package com.example.climaapp.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OpenWeatherMapResponse {

    @SerializedName("cod")
    private int codigo;

    @SerializedName("message")
    private String messagem;

    @SerializedName("count")
    private int countList;

    @SerializedName("list")
    private List<Cidade> cidadesClima;

    public List<Cidade> getCidadesClima() {

        return cidadesClima;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getMessagem() {
        return messagem;
    }

    public int getCountList() {
        return countList;
    }
}
