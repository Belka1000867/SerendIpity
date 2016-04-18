package com.example.bel.softwarefactory.ui.fragments;

import android.Manifest;
import android.app.ActionBar;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.bel.softwarefactory.R;
import com.example.bel.softwarefactory.preferences.SharedPreferencesManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;

@EFragment(R.layout.fragment_map)
public class MapFragment extends BaseFragment implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private final String TAG = this.getClass().getSimpleName();

    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;

    private LocationRequest locationRequest;

    @Bean
    protected SharedPreferencesManager sharedPreferencesManager;

    @AfterViews
    public void afterViews() {
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Map");
        }

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(MapFragment.this)
                .addOnConnectionFailedListener(MapFragment.this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected()");
        startLocationUpdates();

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (lastLocation != null) {
                LatLng lastPosition = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                sharedPreferencesManager.setLastPosition(lastPosition);

                // Add a marker to current position
                Log.d(TAG, "Setting user position");
                Log.d(TAG, "Location Latitude " + lastLocation.getLatitude());
                Log.d(TAG, "Location Longitude " + lastLocation.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPosition, 15));
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() :" + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() connectionResult:" + connectionResult.getErrorMessage());
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "onConnected()");
        if (googleApiClient != null && googleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume()");
        if (googleApiClient != null && googleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    protected void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationRequest == null) {
                locationRequest = new LocationRequest();
            }
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, MapFragment.this);
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, MapFragment.this);
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d(TAG, "onStop()");
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady()");

        this.googleMap = googleMap;

        UiSettings mUiSettings = this.googleMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);

        mUiSettings.setTiltGesturesEnabled(false);
        mUiSettings.setRotateGesturesEnabled(false);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "PERMISSION_GRANTED");

            this.googleMap.setMyLocationEnabled(true);
            this.googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(-65, 25))
                    .title("Lovely place on the lake"));
        } else {
            Log.d(TAG, "PERMISSION_NOT_GRANTED");
            Toast.makeText(getActivity(), "Permissions to get the location are not granted. Please setup the permissions.", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged()");

        lastLocation = location;
    }

}
