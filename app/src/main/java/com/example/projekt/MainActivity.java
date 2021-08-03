package com.example.projekt;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {



    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FAST_UPDATE_INTERVAL = 5;
    private static final int PERMISSIONS_FINE_LOCATION = 99;

    TextView tvLat, tvLon, tvAltitude, tvAccuracy, tvSpeed, tvSensor, tvUpdates, tvAddress, tvWayPointCounts;

    Button btnNewWaypoint, btnShowWaypointList, btnShowMap, btnshowWeather;

    Switch swLocationUpdates, swGps;


    //aktualna lokalizacja
    Location currentLocation;

    //lista zapisanych lokalizacji
    List<Location> savedLocations;


    //Zapytanie o lokalizacje
    LocationRequest locationRequest;

    LocationCallback locationCallBack;

    //Google Api do lokalizacji, główna funkcjonalność
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        tvLat = findViewById(R.id.tv_lat);
        tvLon = findViewById(R.id.tv_lon);
        tvAltitude = findViewById(R.id.tv_altitude);
        tvAccuracy = findViewById(R.id.tv_accuracy);
        tvSpeed = findViewById(R.id.tv_speed);
        tvSensor = findViewById(R.id.tv_sensor);
        tvUpdates = findViewById(R.id.tv_updates);
        tvAddress = findViewById(R.id.tv_address);
        tvWayPointCounts = findViewById(R.id.tv_countOfCrumbs);

        swGps = findViewById(R.id.sw_gps);
        swLocationUpdates = findViewById(R.id.sw_locationsupdates);

        btnNewWaypoint = findViewById(R.id.btn_newWayPoint);
        btnShowWaypointList = findViewById(R.id.btn_showWayPointList);
        btnShowMap = findViewById(R.id.btn_showMap);
        btnshowWeather = findViewById(R.id.btn_showWeather);


        //właściwości LocationRequest

        locationRequest = new LocationRequest();

        //Co ile defaultowo ma ma sprawdzać lokalizacje
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);

        //co ile ma sprawdzać lokalizacje jeśli jest ustawienie na większą dokładność
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);

        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        //odświeża aktualną lokalizacje co 5 lub 30 sekund
        locationCallBack = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);


                //zapisanie lokalizacji
                updateUIValues(locationResult.getLastLocation());

            }
        };

        btnNewWaypoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dostanie lokalizacji GPS


                //dodanie nowej lokalizacji do listy
                MyApplication myApplication = (MyApplication) getApplicationContext();
                savedLocations = myApplication.getMyLocations();
                savedLocations.add(currentLocation);
            }
        });

        btnShowWaypointList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ShowSavedLocationsList.class);
                startActivity(i);
            }
        });

        btnShowMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(i);
            }
        });

        btnshowWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, WeatherActivity.class);
                startActivity(i);
            }
        });


        swGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (swGps.isChecked()) {
                    //więszka dokładność - używa GPSa
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    tvSensor.setText(R.string.gps_sensors);

                } else {
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tvSensor.setText(R.string.cell_tower_wifi);
                }
            }
        });

        swLocationUpdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (swLocationUpdates.isChecked()) {
                    //włącza nawigowanie
                    startLocationUpdates();
                } else {
                    //wyłącza nawigowanie
                    stopLocationUpdates();
                }
            }
        });


        updateGPS();


    }

    private void stopLocationUpdates() {
        tvUpdates.setText(R.string.trackingOff);
        tvLat.setText(R.string.trackingOff);
        tvLon.setText(R.string.trackingOff);
        tvSpeed.setText(R.string.trackingOff);
        tvAddress.setText(R.string.trackingOff);
        tvAccuracy.setText(R.string.trackingOff);
        tvAltitude.setText(R.string.trackingOff);
        tvSensor.setText(R.string.trackingOff);

        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);

    }

    private void startLocationUpdates() {
        tvUpdates.setText(R.string.trackingOn);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        updateGPS();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        switch (requestCode)
        {
            case PERMISSIONS_FINE_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    updateGPS();
                }
                else
                {
                    Toast.makeText(this,R.string.requires_permission, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    private void updateGPS()
    {
        //Dodatnie uprawnień od usera do GPSa
        //Dodanie aktualnej lokacji usera
        //Ustawia wszystkie właściwości textView

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    updateUIValues(location);
                    currentLocation = location;
                }
            });
        }
        else {
            //user nie dostaje jeszcze uprawnień
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }

    }

    private void updateUIValues(Location location)
    {
        //aktualizacja lokalizacji we wszystkich widokach
        tvLat.setText(String.valueOf(location.getLatitude()));
        tvLon.setText(String.valueOf(location.getLongitude()));
        tvAccuracy.setText(String.valueOf(location.getAccuracy()));


        //ponieważ nie każdy telefon ma takie pomiary
        if(location.hasAltitude())
        {
            tvAltitude.setText(String.valueOf(location.getAltitude()));
        }
        else
        {
            tvAltitude.setText(R.string.noAltitude);
        }

        if(location.hasSpeed())
        {
            tvSpeed.setText(String.valueOf(location.getSpeed()));
        }
        else
        {
            tvSpeed.setText(R.string.noSpeed);
        }

        Geocoder geocoder = new Geocoder(MainActivity.this);
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            tvAddress.setText(addresses.get(0).getAddressLine(0));
        }
        catch (Exception e)
        {
            tvAddress.setText(R.string.noAddress);
        }

        MyApplication myApplication = (MyApplication)getApplicationContext();
        savedLocations = myApplication.getMyLocations();

        //pokaż liczbe zapisanych punktów
        tvWayPointCounts.setText(Integer.toString(savedLocations.size()));
    }

}