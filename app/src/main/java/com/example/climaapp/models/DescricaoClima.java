package com.example.climaapp.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DescricaoClima implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("main")
    private String descPrincipal;

    @SerializedName("description")
    private String descricao;

    @SerializedName("icon")
    private String icon;

    public int getId() {
        return id;
    }

    public String getDescPrincipal() {
        return descPrincipal;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getIcon() {
        return icon;
    }
}
