package com.example.googlemapsactivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private LocationManager locManager;
    private Marker userMarker;
    private static final int LOCATION_REQ_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Récupération du fragment de la carte
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        // Message de bienvenue
        Toast.makeText(this, "La carte est prête", Toast.LENGTH_SHORT).show();

        // Position par défaut (ex: Sydney)
        LatLng defaultPos = new LatLng(-34, 151);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPos, 10f));

        checkLocationSettingsAndPermissions();
    }

    private void checkLocationSettingsAndPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQ_CODE);
        } else {
            startTrackingLocation();
        }
    }

    private void startTrackingLocation() {
        try {
            // On utilise le NETWORK_PROVIDER comme demandé
            locManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    2000, // Mise à jour toutes les 2 secondes
                    10,   // Ou tous les 10 mètres
                    new LocationListener() {
                        @Override
                        public void onLocationChanged(@NonNull Location location) {
                            updateMapLocation(location);
                        }

                        @Override
                        public void onProviderDisabled(@NonNull String provider) {
                            showGpsDisabledDialog();
                        }
                    }
            );
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void updateMapLocation(Location location) {
        LatLng currentPos = new LatLng(location.getLatitude(), location.getLongitude());

        if (userMarker == null) {
            userMarker = googleMap.addMarker(new MarkerOptions()
                    .position(currentPos)
                    .title("Ma Position"));
        } else {
            userMarker.setPosition(currentPos);
        }

        // Animation fluide vers la nouvelle position
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 16f));
        
        Toast.makeText(this, "Position: " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();
    }

    private void showGpsDisabledDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Localisation désactivée")
                .setMessage("Le GPS semble être désactivé. Voulez-vous l'activer dans les paramètres ?")
                .setCancelable(false)
                .setPositiveButton("Oui", (dialog, id) -> {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                })
                .setNegativeButton("Non", (dialog, id) -> dialog.cancel())
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQ_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Accès autorisé", Toast.LENGTH_SHORT).show();
                startTrackingLocation();
            } else {
                Toast.makeText(this, "Accès refusé", Toast.LENGTH_LONG).show();
            }
        }
    }
}
