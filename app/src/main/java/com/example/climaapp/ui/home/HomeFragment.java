package com.example.climaapp.ui.home;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.climaapp.R;
import com.example.climaapp.models.Cidade;
import com.example.climaapp.models.OpenWeatherMapResponse;
import com.example.climaapp.singletons.MainRequestQueue;
import com.example.climaapp.singletons.UserVariables;
import com.example.climaapp.ui.cidade.ClimaCidadeActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private HomeViewModel homeViewModel;

    private FloatingActionButton buscarButtonFab;
    private SupportMapFragment mapFragment;
    private ListView listPlaces;
    private TextView listPlacesEmpty;
    private GoogleMap googleMap;
    private ProgressBar listPlacesProgressBar;

    private List<Cidade> cidadesClima;
    private List<Marker> mapMarkersCidadesClima;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mapLastKnownLocation;
    private final LatLng mapDefaultLocation = new LatLng(-20.385574, -43.503578); // Ouro Preto -  MG - BR

    private LatLng mapLastSelectionLocation = null;
    private Marker mapLastSelectionMarker = null;

    private static final String CITY_DETAIL_SEND_PORP = "cidadeSelect";

    private static final String LIST_CITY_ITEM_TITLE_PROP = "title";
    private static final String LIST_CITY_ITEM_DESC_PROP = "description";
    private static final String LIST_CITY_ITEM_ICON_PROP = "weatherImage";

    private static final int DEFAULT_ZOOM = 12;
    private static final int DEFAULT_ZOOM_GROUP_MARKERS = 200;
    private static final int QNT_CIDADES_REQUEST = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private boolean mapLocationPermissionGranted;

    private FusedLocationProviderClient mapFusedLocationProviderClient;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        /* define elementos View */
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        listPlacesEmpty = (TextView) root.findViewById(R.id.listPlacesEmpty);

        listPlaces = (ListView) root.findViewById(R.id.listPlaces);
        mapMarkersCidadesClima = new LinkedList<Marker>();

        listPlacesProgressBar = (ProgressBar) root.findViewById(R.id.listPlacesProgressBar);

        buscarButtonFab = root.findViewById(R.id.fab);
        buscarButtonFab.setOnClickListener(onClickPesquisar);

    /*
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(this, new Observer<String>() {

            @Override
            public void onChanged(@Nullable String s) {

                textView.setText(s);
            }
        });
*/

        if (mapFragment == null) {

            mapFragment = SupportMapFragment.newInstance();
            mapFragment.getMapAsync(this);
        }

        getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();

        mapFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.getActivity());

        return root;
    }

    /**
     *
     * Cria StringRequest para bucar dados de api web OpenWeather com dados de ultima localizacao conhecida nao nula.
     */
    private StringRequest criarRequestClimaOpenWeather() {

        String url ="http://api.openweathermap.org/data/2.5/find?"
                + "lat=" + mapLastSelectionLocation.latitude
                + "&lon=" + mapLastSelectionLocation.longitude
                + "&cnt=" + QNT_CIDADES_REQUEST
                + "&APPID=" + getString(R.string.open_weather_map_api)
                + "&units=" + UserVariables.getInstance().getTipoUnidade().getUnit();

        // Request a string response from the provided URL.
        return new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        cidadesClima = getListaCidadesClima(response);
                        fillCidadesList();

                        buscarButtonFab.setEnabled(true);
                        listPlacesProgressBar.setVisibility(View.GONE);
                        listPlaces.setVisibility(View.VISIBLE);

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                    buscarButtonFab.setEnabled(true);
                    listPlacesProgressBar.setVisibility(View.GONE);
                    listPlaces.setVisibility(View.VISIBLE);

                    Toast.makeText(getContext(), getString(R.string.error_bad_location), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        this.googleMap = googleMap;

        // Enable the zoom controls for the map
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Prompt the user for permission.
        // getLocationPermission();

        if (mapLocationPermissionGranted) {

            addMarkerByDeviceLocation(googleMap);

        } else {

            addMarkerDefaultLocation(googleMap);
        }

        googleMap.setOnMapClickListener(onClickMapa);
    }

    /**
     *
     * Metodo adiciona marcador no mapa em localizacao default
     */
    private void addMarkerDefaultLocation(GoogleMap googleMap) {

        // Add a default marker, because the user hasn't selected a place.
        addMarkerAndMovecamera(googleMap, mapDefaultLocation,
                getString(R.string.default_info_title), DEFAULT_ZOOM,true, true, true);
    }

    /*
     *
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     *
     */
    private void getLocationPermission() {

        mapLocationPermissionGranted = false;
        if (ContextCompat.checkSelfPermission(
                this.getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mapLocationPermissionGranted = true;
        } else {

            ActivityCompat.requestPermissions(this.getActivity(),
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /*
     *
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     *
     */
    private void addMarkerByDeviceLocation(final GoogleMap googleMap) {

        try {
            if (mapLocationPermissionGranted) {

                Task<Location> locationResult = mapFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this.getActivity(), requestCallbackDeviceLocation);
            }
        } catch (SecurityException e)  {

            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     *
     * Move posicao da camera/view do mapa para localizacao desejada no centro e com zoomOpt desejado
     */
    private void moveCameraLocation(GoogleMap googleMap, LatLng mapLocation, int zoomOpt) {

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapLocation, zoomOpt));
    }

    /**
     *
     * Move posicao da camera/view do mapa para localizacao centralizada aos marcadores passados e com zoomOpt desejado
     */
    private void moveCameraLocation(GoogleMap googleMap, List<Marker> markers, int zoomOpt) {

        if (!markers.isEmpty()) {

            LatLngBounds.Builder b = new LatLngBounds.Builder();
            for (Marker m : markers) {

                b.include(m.getPosition());
            }
            LatLngBounds bounds = b.build();

            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, zoomOpt));
        }
    }

    /**
     *
     * Limpa listas, listview, marcadores no mapa, ultimas posicoes clicadas.
     */
    private void removeMarkers() {

        googleMap.clear();
        mapMarkersCidadesClima.clear();

        listPlaces.setVisibility(View.GONE);
        listPlacesEmpty.setVisibility(View.VISIBLE);
        listPlacesProgressBar.setVisibility(View.GONE);
    }

    /**
     *
     * Limpa ultimo marcador de pesquisa.
     */
    private void removeLastMarker() {

        if (mapLastSelectionMarker != null) {

            mapLastSelectionMarker.remove();
        }

        mapLastSelectionMarker = null;
        mapLastSelectionLocation = null;
    }

    /**
     *
     * Retorna marcador adicionado no mapa na posicao e title desejada.
     * Flag searchMark, define se marcador selecionado ou lista de cidades (vermelhos e laranjas).
     * E flag removeOthers, define se mapa deve ser limpo antes de adicionar marcador.
     */
    private Marker addMarker(GoogleMap googleMap, LatLng mapLocation,
            String markerTitle, boolean searchMark, boolean removeOthers, boolean removeLast) {

        if (removeOthers) {

            removeMarkers();
        }

        MarkerOptions markOpt = new MarkerOptions()
                .title(markerTitle)
                .position(mapLocation);

        Marker createdMarker = googleMap.addMarker(markOpt);

        if (searchMark) {

            buscarButtonFab.setEnabled(true);

            changeMarkAndLocationClicked(createdMarker, removeLast);
        } else {

            createdMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        }

        return createdMarker;
    }

    /**
     *
     * Adiciona marcador no mapa e move camera para sua posicao.
     */
    private void addMarkerAndMovecamera(GoogleMap googleMap, LatLng mapLocation, String markerTitle, int zoomOpt,
            boolean searchMark, boolean removeOthers, boolean removeLast) {

        addMarker(googleMap, mapLocation, markerTitle, searchMark, removeOthers, removeLast);

        moveCameraLocation(googleMap, mapLocation, zoomOpt);
    }

    /**
     *
     * Metodo faz parse de json string recebido para objeto java. Retorna propriedade do com listas de cidades
     * caso nao exista, retorna lista vazia.
     */
    private List<Cidade> getListaCidadesClima(String jsonObj) {

        Gson gsonParse = new Gson();

        OpenWeatherMapResponse weatherResponse = null;
        try {

            weatherResponse = gsonParse.fromJson(jsonObj, OpenWeatherMapResponse.class);
            return weatherResponse.getCidadesClima();

        } catch(Exception e) {

            Log.e("ERROR", "Falha ao tentar parse de stringObj para JavaObj");
            e.printStackTrace();
        }

        return new LinkedList<Cidade>();
    }

    /**
     * Display a list allowing the user to select a place from a list of cidadesClima.
     */
    private void fillCidadesList() {

        removeMarkers();

        LinkedList<Map<String,Object>> itemDataList = criaMapaListItens();

        // seleciona primeira cidade da requisicao
        mapMarkersCidadesClima.get(0).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mapMarkersCidadesClima.get(0).showInfoWindow();
        mapLastSelectionMarker = mapMarkersCidadesClima.get(0);

        moveCameraLocation(googleMap, mapMarkersCidadesClima, DEFAULT_ZOOM_GROUP_MARKERS);

        SimpleAdapter simpleAdapter = new SimpleAdapter(this.getActivity(), itemDataList, R.layout.list_item_cidade_clima_layout,
                new String[]{LIST_CITY_ITEM_TITLE_PROP, LIST_CITY_ITEM_DESC_PROP, LIST_CITY_ITEM_ICON_PROP},
                new int[]{R.id.cidadeTitle, R.id.cidadeDesc, R.id.weatherImage});

        listPlaces.setAdapter(simpleAdapter);

        listPlaces.setOnItemClickListener(listClickedHandler);
        googleMap.setOnMarkerClickListener(markerClickedHandler);
    }

    /**
     *
     * Cria mapa de itens no padrao para uso de simple adapter para preencher list de cidades.
     */
    private LinkedList<Map<String, Object>> criaMapaListItens() {

        LinkedList<Map<String,Object>> itemDataList = new LinkedList<Map<String,Object>>();
        for(Cidade cidade: cidadesClima) {

            Map<String,Object> listItemMap = new HashMap<String, Object>();
            int iconLink = R.mipmap.ic_launcher;

            String nome = cidade.getNomeCidade();
            listItemMap.put(LIST_CITY_ITEM_TITLE_PROP, nome);
            listItemMap.put(LIST_CITY_ITEM_DESC_PROP, cidade.getdDescricaoClimaCidade());
            listItemMap.put(LIST_CITY_ITEM_ICON_PROP, iconLink);
            itemDataList.add(listItemMap);

            Marker mark = addMarker(googleMap, cidade.getCoordendaCidade().getCoordenadaMaps(), nome, false, false, false);
            mapMarkersCidadesClima.add(mark);
        }

        return itemDataList;
    }

    /**
     * When user taps an item in list of cities, add a marker to the map with the place details
     */
    private AdapterView.OnItemClickListener listClickedHandler = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {

            Intent climaInfo = new Intent(getActivity(), ClimaCidadeActivity.class);
            climaInfo.putExtra(CITY_DETAIL_SEND_PORP, cidadesClima.get(position));
            startActivity(climaInfo);

            Marker mark = mapMarkersCidadesClima.get(position);
            changeMarkAndLocationClicked(mark);

            moveCameraLocation(googleMap, mapMarkersCidadesClima, DEFAULT_ZOOM_GROUP_MARKERS);
        }
    };

    /**
     *
     * Metodo define acoes de clique no mapa. Ao clicar, busca nome da localizacao usando GeoCord, e adiciona
     * marcador com o nome na posicao clicada.
     */
    private GoogleMap.OnMapClickListener onClickMapa = new GoogleMap.OnMapClickListener() {

        @Override
        public void onMapClick(LatLng point) {

            String localityName = getString(R.string.selected_location);

            Geocoder gcd = new Geocoder(getContext(), Locale.getDefault());
            List<Address> addresses = null;

            try {

                addresses = gcd.getFromLocation(point.latitude, point.longitude, 1);
            } catch (IOException e) {

                Log.d(TAG, "Localização não encontrada com Geocoder: " + point);
                e.printStackTrace();
            }

            if (addresses != null && addresses.size() > 0) {

                if (addresses.get(0).getSubAdminArea() != null && !addresses.get(0).getSubAdminArea().equals("")) {

                    localityName = addresses.get(0).getSubAdminArea();
                } else if (addresses.get(0).getLocality() != null && !addresses.get(0).getLocality().equals("")) {

                    localityName = addresses.get(0).getLocality();
                }
            }

            addMarker(googleMap, point, localityName, true, false, true);
        }
    };

    /**
     *
     * Declaracao do clique nos marcadores presentes no mapa. Seleciona o marcador como ultima poiscao valida
     * e rola o scroll da lista para a posicao coorespondente do item.
     */
    private GoogleMap.OnMarkerClickListener  markerClickedHandler = new GoogleMap.OnMarkerClickListener() {
        public boolean onMarkerClick(Marker mark) {

            if (mapLastSelectionMarker == mark) {
                return true;
            }

            changeMarkAndLocationClicked(mark, true);

            moveCameraLocation(googleMap, mapMarkersCidadesClima, DEFAULT_ZOOM_GROUP_MARKERS);

            int positionList = mapMarkersCidadesClima.indexOf(mark);
            if (positionList >= 0 && cidadesClima.size() > positionList) {

                listPlaces.smoothScrollToPosition(positionList);

                return true;
            }

            return false;
        }
    };

    /**
     *
     * Metodo defini listener para botao pesquisar. Em caso de haver cidade escolhida, requsicao eh criada e adicionada na fila.
     */
    private View.OnClickListener onClickPesquisar = new View.OnClickListener() {

        @Override
        public void onClick(final View view) {

            if (mapLastSelectionLocation != null) {

                buscarButtonFab.setEnabled(false);
                listPlaces.setVisibility(View.GONE);
                listPlacesEmpty.setVisibility(View.GONE);
                listPlacesProgressBar.setVisibility(View.VISIBLE);

                StringRequest stringRequest = criarRequestClimaOpenWeather();

                // Add the request to the RequestQueue.
                MainRequestQueue.getInstance(getContext()).addToRequestQueue(stringRequest);
            }

        }
    };

    /**
     *
     * Metodo callback de requsicao de locali
     */
    private OnCompleteListener requestCallbackDeviceLocation =  new OnCompleteListener<Location>() {

        @Override
        public void onComplete(@NonNull Task<Location> task) {

            if (task.isSuccessful()) {

                // Save last known position
                mapLastKnownLocation = task.getResult();

                Log.d(TAG, "Latitude: " + mapLastKnownLocation.getLatitude());
                Log.d(TAG, "Longitude: " + mapLastKnownLocation.getLongitude());

                addMarkerAndMovecamera(googleMap,
                        new LatLng(mapLastKnownLocation.getLatitude(), mapLastKnownLocation.getLongitude()),
                        "Localização Atual",
                        DEFAULT_ZOOM,
                        true,
                        true,
                        true);
            } else {
                Log.d(TAG, "Current location is null. Using defaults.");
                Log.e(TAG, "Exception: %s", task.getException());

                addMarkerDefaultLocation(googleMap);
            }
        }
    };

    /**
     *
     * Metodo trata nova selecao de marcador no mapa. Recebe marcador a ser marcado como a ultima posicao clicada, mas nao remove ultimo marcador
     */
    private void changeMarkAndLocationClicked(Marker mark) {
        changeMarkAndLocationClicked(mark, false);
    }

    /**
     *
     * Metodo trata nova selecao de marcador no mapa. Recebe marcador a ser marcado como a ultima posicao clicada.
     */
    private void changeMarkAndLocationClicked(Marker mark, boolean removeLast) {

        if ((!mapMarkersCidadesClima.isEmpty() && mapLastSelectionMarker != null && mapMarkersCidadesClima.contains(mapLastSelectionMarker))) {

            mapLastSelectionMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        } else if (mapLastSelectionMarker != null && removeLast) {

            removeLastMarker();
        }

        mark.showInfoWindow();

        mark.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        mapLastSelectionLocation = mark.getPosition();
        mapLastSelectionMarker = mark;
    }
}