package com.example.climaapp.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

public class Clima {
    

    @SerializedName("temp")
    private double tempFahrenheit;

    @SerializedName("temp_min")
    private double tempMinFahrenheit;

    @SerializedName("temp_max")
    private double tempMaxFahrenheit;
}
