package com.example.climaapp.singletons;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.climaapp.models.enums.TipoUnit;
import com.example.climaapp.util.OpenWeatherMapUtil;

public class UserVariables {

    private static UserVariables ourInstance;
    private static Context ctx;

    private TipoUnit unidade;
    private String unidadeStr;

    public static synchronized UserVariables getInstance() {

        if (ourInstance == null) {

            ourInstance = new UserVariables();
        }

        return ourInstance;
    }

    private UserVariables() {

    }

    public TipoUnit getTipoUnidade() {

        return unidade;
    }

    public String getTipoUnidadeStr() {

        return unidadeStr;
    }


    public void setTipoUnidade(String unitStr) {

        TipoUnit unit = TipoUnit.fromString(unitStr);
        if (unit != null) {

            this.unidade = unit;
        } else {

            this.unidade = TipoUnit.STANDARD;
        }

        unidadeStr = OpenWeatherMapUtil.getStringUnit(unidade);
    }
}
