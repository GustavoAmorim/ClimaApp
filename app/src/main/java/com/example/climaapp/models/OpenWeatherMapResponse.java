package com.example.climaapp.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Cidade {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("coord")
    private CoordenadaLatLon coordendaCidade;

    @SerializedName("main")
    private Clima climaPrincipal;

    @SerializedName("weather")
    private List<DescricaoClima> climaDescricao;
}

"message":"accurate","cod":"200","count":15,"list"
