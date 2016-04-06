package com.example.bel.softwarefactory;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Bel on 24.02.2016.
 */
public class Map extends Fragment implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "Debug_GoogleMap";

    private GoogleMap mGoogleMap;
    private UiSettings mUiSettings;

    private GoogleApiClient googleApiClient;
    private Location mLastLocation;
    private Double lastLatitude;
    private Double lastLongitude;
    private LatLng lastPosition;
    private LocationRequest locationRequest;
    protected Boolean mRequestingLocationUpdates;

    private UserLocalStore userLocalStore;

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected()");

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (mLastLocation != null) {
                lastLatitude = mLastLocation.getLatitude();
                lastLongitude = mLastLocation.getLongitude();

                lastPosition = new LatLng(lastLatitude, lastLongitude);

                userLocalStore.setLastLatitude(lastLatitude);
                userLocalStore.setLastLongitude(lastLongitude);

                Log.d(TAG, "Location Latitude " + mLastLocation.getLatitude());
                Log.d(TAG, "Location Longitude " + mLastLocation.getLongitude());

                // Add a marker to current position

                Log.d(TAG, "Setting user position");
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPosition, 15));

            }
        }

        if(mRequestingLocationUpdates){
            startLocationUpdates();
        }

    }

    protected void startLocationUpdates(){
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, Map.this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended()");
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() connectionResult:" + connectionResult.getErrorMessage());
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        mRequestingLocationUpdates = false;

        userLocalStore = new UserLocalStore(getActivity());
        /*
        * Creating an instance of GoogleAPIClient with manual (GoogleApiClient.ConnectionCallbacks) of context in fragment
        * connecting to location services api
        * */
        buildGoogleApiClient();









        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                final LocationSettingsStates locationSettingsStates = locationSettingsResult.getLocationSettingsStates();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.d(TAG, "Success");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

/*                        try {
                            status.startResolutionForResult(getActivity(), );
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        */

                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                        break;
                }

            }
        });


      //  PendingResult<LocationSettingsResult> mRequestingLocationUpdates = LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, Map.this, Map.this, T);
    }



    @Override
    public void onStart() {
        Log.d(TAG, "onStart()");
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onConnected()");
        super.onPause();
        if(googleApiClient.isConnected()){
            stopLocationUpdates();
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
        try {
            getActivity().getActionBar().setTitle("Map");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(googleApiClient.isConnected() && !mRequestingLocationUpdates){
            startLocationUpdates();
        }
    }

    protected void stopLocationUpdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, Map.this);
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop()");
        googleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView()");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach()");
        super.onDetach();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "PERMISSION_GRANTED");

        View root = null;
        try {
            root = inflater.inflate(R.layout.fragment_map, container, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return root;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady()");

        mGoogleMap = googleMap;
        /*
        * Adding map control buttons
        * */
        mUiSettings = mGoogleMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        /*
        * cancel changing the angel
        * */
        mUiSettings.setTiltGesturesEnabled(false);
        mUiSettings.setRotateGesturesEnabled(false);
        //mUiSettings.setScrollGesturesEnabled(false);



        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "PERMISSION_GRANTED");

            /*
            * Allow application to get user location
            * */
            mGoogleMap.setMyLocationEnabled(true);

            // Show rationale and request permission.


            mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(-65, 25))
                    .title("Lovely place on the lake"));


        } else {
            Log.d(TAG, "PERMISSION_NOT_GRANTED");
            Toast.makeText(getActivity(), "Permissions to get the location are not granted. Please setup the permissions.", Toast.LENGTH_LONG).show();
        }

    }

    /*
    * Method onLocationChange from Interface gms.LocationListener
    * */

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged()");

        mLastLocation = location;

    }

    protected synchronized void buildGoogleApiClient(){
        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(Map.this)
                .addOnConnectionFailedListener(Map.this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }


    /*
    * The priority of PRIORITY_HIGH_ACCURACY, combined with the ACCESS_FINE_LOCATION permission setting
    * that you've defined in the app manifest, and a fast update interval of 5000 milliseconds (5 seconds),
    * causes the fused location provider to return location updates that are accurate to within a few feet.
    * */
    protected void createLocationRequest(){
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

}
