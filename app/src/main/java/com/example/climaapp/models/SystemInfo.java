package com.example.climaapp.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SystemInfo implements Serializable {
    

    @SerializedName("country")
    private String pais;

    @SerializedName("sunrise")
    private long sunriseTime;

    @SerializedName("sunset")
    private long sunsetTime;

    public String getPais() {
        return pais;
    }

    public long getSunriseTime() {
        return sunriseTime;
    }

    public long getSunsetTime() {
        return sunsetTime;
    }
}
