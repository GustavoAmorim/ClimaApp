package com.example.climaapp;

import android.Manifest;
import android.os.Bundle;

import com.example.climaapp.singletons.MainRequestQueue;
import com.example.climaapp.singletons.UserVariables;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import com.inlocomedia.android.core.permissions.PermissionResult;
import com.inlocomedia.android.core.permissions.PermissionsListener;
import com.inlocomedia.android.engagement.InLocoEngagement;
import com.inlocomedia.android.engagement.InLocoEngagementOptions;
import com.inlocomedia.android.engagement.request.FirebasePushProvider;
import com.inlocomedia.android.engagement.request.PushProvider;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    private final static String[] REQUIRED_PERMISSIONS = { Manifest.permission.ACCESS_FINE_LOCATION };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                // , R.id.nav_gallery, R.id.nav_slideshow,
                // R.id.nav_tools, R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        final boolean askIfDenied = true; // Will prompt the user if he has previously denied the permission

        InLocoEngagement.requestPermissions(this, REQUIRED_PERMISSIONS, askIfDenied, new PermissionsListener() {

            @Override
            public void onPermissionRequestCompleted(final HashMap<String, PermissionResult> authorized) {
                if (authorized.get(Manifest.permission.ACCESS_FINE_LOCATION).isAuthorized()) {
                    // Permission enabled
                }
            }
        });

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);

        // Retrieve the Firebase token
        String firebaseToken = FirebaseInstanceId.getInstance().getToken();

        if (firebaseToken != null && !firebaseToken.isEmpty()) {
            final PushProvider pushProvider = new FirebasePushProvider.Builder()
                    .setFirebaseToken(firebaseToken)
                    .build();
            InLocoEngagement.setPushProvider(this, pushProvider);
        }

        // Set initialization options
        InLocoEngagementOptions options = InLocoEngagementOptions.getInstance(this);

        // The App ID you obtained in the dashboard
        options.setApplicationId(getString(R.string.in_loco_key));

        // Verbose mode; enables SDK logging, defaults to true.
        // Remember to set to false in production builds.
        options.setLogEnabled(true);

        //Initialize the SDK
        InLocoEngagement.init(this, options);

        MainRequestQueue.getInstance(this);

        UserVariables.getInstance().setTipoUnidade(getString(R.string.defaul_unit_request));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onStop () {
        super.onStop();

        MainRequestQueue.getInstance(this).getRequestQueue().cancelAll("Finalizando todas requiscoes na fila!");
    }
}
