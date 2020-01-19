package com.example.climaapp.models;

import com.example.climaapp.singletons.UserVariables;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Cidade implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("coord")
    private CoordenadaLatLon coordendaCidade;

    @SerializedName("main")
    private Clima climaPrincipal;

    @SerializedName("sys")
    private SystemInfo sysConfig;

    @SerializedName("weather")
    private List<DescricaoClima> climaDescricao;

    public SystemInfo getSysConfig() {
        return sysConfig;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CoordenadaLatLon getCoordendaCidade() {
        return coordendaCidade;
    }

    public Clima getClimaPrincipal() {
        return climaPrincipal;
    }

    public List<DescricaoClima> getClimaDescricao() {
        return climaDescricao;
    }

    public String getNomeCidade() {

        StringBuilder nome = new StringBuilder("");
        if (name != null && !name.equalsIgnoreCase("")) {

            nome.append(name);
        }

        if (sysConfig != null && sysConfig.getPais() != null && !sysConfig.getPais().equalsIgnoreCase("")) {

            if (!name.isEmpty()) {

                nome.append(" - ");
            }

            nome.append(sysConfig.getPais());
        }

        return nome.toString();
    }

    public String getdDescricaoClimaCidade() {

        StringBuilder desc = new StringBuilder("");

        if (climaDescricao != null && !climaDescricao.isEmpty() && !climaDescricao.get(0).getDescPrincipal().equalsIgnoreCase("")) {

            desc.append(climaDescricao.get(0).getDescPrincipal());
        }

        if (climaPrincipal != null) {

            desc.append(" - ");

            desc.append(climaPrincipal.getTemp());

            desc.append(" " + UserVariables.getInstance().getTipoUnidadeStr());
        }

        return desc.toString();
    }
}
