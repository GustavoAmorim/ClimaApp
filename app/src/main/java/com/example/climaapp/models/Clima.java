package com.example.climaapp.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Clima implements Serializable {
    

    @SerializedName("temp")
    private double temp;

    @SerializedName("temp_min")
    private double tempMin;

    @SerializedName("temp_max")
    private double tempMax;

    public double getTemp() {
        return temp;
    }

    public double getTempMin() {
        return tempMin;
    }

    public double getTempMax() {
        return tempMax;
    }
}
