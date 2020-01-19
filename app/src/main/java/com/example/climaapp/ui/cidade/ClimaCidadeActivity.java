package com.example.climaapp.ui.cidade;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.climaapp.R;
import com.example.climaapp.models.Cidade;
import com.example.climaapp.models.DescricaoClima;
import com.example.climaapp.singletons.UserVariables;
import com.squareup.picasso.Picasso;

import java.util.Date;

public class ClimaCidadeActivity extends AppCompatActivity {

    private static final String CITY_DETAIL_RECIVE_PORP = "cidadeSelect";

    TextView cityField;
    TextView detailsField;
    TextView currentTemperatureField;
    TextView weatherDetails;
    ImageView weatherIcon;

    String iconUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clima_cidade);

        Intent climaInfo = getIntent();
        Cidade cidade = (Cidade) climaInfo.getSerializableExtra(CITY_DETAIL_RECIVE_PORP);

        if (cidade != null) {

            weatherDetails = (TextView) findViewById(R.id.weather_details);
            weatherIcon = (ImageView) findViewById(R.id.weather_icon);

            if (!cidade.getClimaDescricao().isEmpty()) {

                DescricaoClima weather = cidade.getClimaDescricao().get(0);

                weatherDetails.setText(weather.getDescricao());

                iconUrl = "http://openweathermap.org/img/w/" + weather.getIcon() + ".png";

                Picasso.get()
                        .load(iconUrl)
                        .resize(300, 300)
                        .centerCrop()
                        .into(weatherIcon);
            }

            cityField = (TextView) findViewById(R.id.city_field);
            cityField.setText(cidade.getNomeCidade());

            detailsField = (TextView) findViewById(R.id.details_field);
            detailsField.setText(cidade.getClimaPrincipal().getTempMin() + " ~ " + cidade.getClimaPrincipal().getTempMax()
                    + " " + UserVariables.getInstance().getTipoUnidadeStr());

            currentTemperatureField = (TextView) findViewById(R.id.current_temperature_field);
            currentTemperatureField.setText(cidade.getClimaPrincipal().getTemp()
                    + " " + UserVariables.getInstance().getTipoUnidadeStr());
        }
    }
}
