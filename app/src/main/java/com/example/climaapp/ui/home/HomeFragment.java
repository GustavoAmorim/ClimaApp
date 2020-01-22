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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("LIFECYCLE", "OnCreate");

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        listPlacesEmpty = (TextView) root.findViewById(R.id.listPlacesEmpty);
        listPlacesEmpty.setVisibility(View.VISIBLE);

        // Set up the views
        listPlaces = (ListView) root.findViewById(R.id.listPlaces);
        mapMarkersCidadesClima = new LinkedList<Marker>();

        buscarButtonFab = root.findViewById(R.id.fab);
        buscarButtonFab.setEnabled(false);
        buscarButtonFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                if (mapLastSelectionLocation != null) {

                   // buscarButtonFab.setEnabled(false);

                    // Instantiate the RequestQueue.
                    String url ="http://api.openweathermap.org/data/2.5/find?"
                            + "lat=" + mapLastSelectionLocation.latitude
                            + "&lon=" + mapLastSelectionLocation.longitude
                            + "&cnt=" + QNT_CIDADES_REQUEST
                            + "&APPID=" + getString(R.string.open_weather_map_api)
                            + "&units=" + UserVariables.getInstance().getTipoUnidade().getUnit();

                    // Request a string response from the provided URL.
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {

                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.

                                cidadesClima = getListaCidadesClima(response);
                                fillCidadesList();

                                buscarButtonFab.setEnabled(true);
                                listPlaces.setVisibility(View.VISIBLE);
                                listPlacesEmpty.setVisibility(View.GONE);

                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {

                                // Snackbar.make(view, getString(R.string.error_bad_location), Snackbar.LENGTH_LONG)
                                //        .setAction("Action", null).show();

                                buscarButtonFab.setEnabled(true);
                                Toast.makeText(view.getContext(), getString(R.string.error_bad_location), Toast.LENGTH_SHORT).show();
                            }
                    });

                    // Add the request to the RequestQueue.
                    MainRequestQueue.getInstance(getActivity().getApplicationContext()).addToRequestQueue(stringRequest);
                }

            }
        });

    /*
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(this, new Observer<String>() {

            @Override
            public void onChanged(@Nullable String s) {

                textView.setText(s);
            }
        });
*/

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        if (mapFragment == null) {

            mapFragment = SupportMapFragment.newInstance();
            mapFragment.getMapAsync(this);
        }

        getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();

        mapFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.getActivity());
        Log.d("LIFECYCLE", "OnCreateView");

        return root;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        this.googleMap = googleMap;

        // Enable the zoom controls for the map
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Prompt the user for permission.
        getLocationPermission();

        if (mapLocationPermissionGranted) {

            addMarkerByDeviceLocation(googleMap);

        } else {

            // Add a default marker, because the user hasn't selected a place.
            addMarkerAndMovecamera(googleMap, mapDefaultLocation,
                    getString(R.string.default_info_title), DEFAULT_ZOOM,true);
        }

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
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

            addMarker(googleMap, point, localityName, true, true);
            }
        });
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
                locationResult.addOnCompleteListener(this.getActivity(), new OnCompleteListener<Location>() {

                    @Override
                    public void onComplete(@NonNull Task<Location> task) {

                        if (task.isSuccessful()) {

                            // Set the map's camera position to the current location of the device.
                            mapLastKnownLocation = task.getResult();

                            Log.d(TAG, "Latitude: " + mapLastKnownLocation.getLatitude());
                            Log.d(TAG, "Longitude: " + mapLastKnownLocation.getLongitude());

                            addMarkerAndMovecamera(googleMap,
                                    new LatLng(mapLastKnownLocation.getLatitude(), mapLastKnownLocation.getLongitude()),
                                    "Localização Atual",
                                    DEFAULT_ZOOM,
                                    true);
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());

                            addMarkerAndMovecamera(googleMap,
                                    mapDefaultLocation,
                                    getString(R.string.default_info_title),
                                    DEFAULT_ZOOM,
                                    true);
                        }

                        // getCurrentPlaceLikelihoods();
                    }
                });
            }
        } catch (SecurityException e)  {

            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void moveCameraLocation(GoogleMap googleMap, LatLng mapLocation, int zoomOpt) {

        googleMap.moveCamera(CameraUpdateFactory
                .newLatLngZoom(mapLocation, zoomOpt));
    }

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

    private void removeMarkers() {

        googleMap.clear();
        mapMarkersCidadesClima.clear();

        mapLastSelectionMarker = null;
        mapLastSelectionLocation = null;

        listPlaces.setVisibility(View.GONE);
        listPlacesEmpty.setVisibility(View.VISIBLE);
    }

    private Marker addMarker(GoogleMap googleMap, LatLng mapLocation, String markerTitle, boolean searchMark, boolean removeOthers) {

        if (removeOthers) {

            removeMarkers();
        }

        MarkerOptions markOpt = new MarkerOptions()
                .title(markerTitle)
                .position(mapLocation);

        Marker createdMarker = googleMap.addMarker(markOpt);

        if (searchMark) {

            buscarButtonFab.setEnabled(true);

            changeMarkAndLocationClicked(createdMarker);
        } else {

            createdMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        }

        return createdMarker;
    }

    private void addMarkerAndMovecamera(GoogleMap googleMap, LatLng mapLocation, String markerTitle, int zoomOpt, boolean removeOthers) {

        addMarker(googleMap, mapLocation, markerTitle, true, removeOthers);

        moveCameraLocation(googleMap, mapLocation, zoomOpt);
    }

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

        LinkedList<Map<String,Object>> itemDataList = new LinkedList<Map<String,Object>>();
        for(Cidade cidade: cidadesClima) {

            Map<String,Object> listItemMap = new HashMap<String, Object>();
            int iconLink = R.mipmap.ic_launcher;

            String nome = cidade.getNomeCidade();
            listItemMap.put(LIST_CITY_ITEM_TITLE_PROP, nome);
            listItemMap.put(LIST_CITY_ITEM_DESC_PROP, cidade.getdDescricaoClimaCidade());
            listItemMap.put(LIST_CITY_ITEM_ICON_PROP, iconLink);
            itemDataList.add(listItemMap);

            Marker mark = addMarker(googleMap, cidade.getCoordendaCidade().getCoordenadaMaps(), nome, false, false);
            mapMarkersCidadesClima.add(mark);
        }

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
     * When user taps an item in list of cities, add a marker to the map with the place details
     */
    private GoogleMap.OnMarkerClickListener  markerClickedHandler = new GoogleMap.OnMarkerClickListener() {
        public boolean onMarkerClick(Marker mark) {

            changeMarkAndLocationClicked(mark);

            moveCameraLocation(googleMap, mapMarkersCidadesClima, DEFAULT_ZOOM_GROUP_MARKERS);

            int positionList = mapMarkersCidadesClima.indexOf(mark);
            if (positionList >= 0 && cidadesClima.size() > positionList) {

                listPlaces.smoothScrollToPosition(positionList);

                return true;
            }

            return false;
        }
    };

    private void changeMarkAndLocationClicked(Marker mark) {

        if (!mapMarkersCidadesClima.isEmpty() && mapLastSelectionMarker != null && mapMarkersCidadesClima.contains(mapLastSelectionMarker)) {

            mapLastSelectionMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        }

        mark.showInfoWindow();

        mark.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        mapLastSelectionLocation = mark.getPosition();
        mapLastSelectionMarker = mark;
    }
}