package com.example.climaapp.models;

import com.google.android.gms.maps.model.LatLng;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CoordenadaLatLon implements Serializable {


    @SerializedName("lat")
    private double latitude;

    @SerializedName("lon")
    private double longitude;

    private LatLng coordenadaMaps;

    public LatLng getCoordenadaMaps() {

        if (coordenadaMaps == null) {

            if (longitude != 0 && latitude != 0) {

                return new LatLng(latitude, longitude);
            }

            return null;
        }

        return coordenadaMaps;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
