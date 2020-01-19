package com.example.climaapp.util;

import com.example.climaapp.models.enums.TipoUnit;

public class OpenWeatherMapUtil {

    public static String getStringUnit(TipoUnit tipoUnit) {

        if (TipoUnit.METRIC == tipoUnit) {

            return "°C";
        } else if (TipoUnit.IMPERIAL == tipoUnit) {

            return "°F";
        }

        return "K";
    }
}
