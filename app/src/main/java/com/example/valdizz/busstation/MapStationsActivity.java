package com.example.valdizz.busstation;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.valdizz.busstation.Database.DatabaseAccess;
import com.example.valdizz.busstation.Dialogs.StationListDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

import static com.example.valdizz.busstation.MainActivity.TAG_LOG;

public class MapStationsActivity extends AppCompatActivity implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener, LocationListener, GoogleMap.CancelableCallback, GoogleMap.OnMarkerClickListener {

    protected static final LatLng LIDA = new LatLng(53.887350, 25.302713);
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    protected static final int DURATION_TIME_IN_MILLISECONDS = 2500;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    protected final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    protected final static String KEY_LOCATION = "location";

    protected GoogleMap mMap;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected LocationSettingsRequest mLocationSettingsRequest;
    protected Location mCurrentLocation;
    protected Boolean mRequestingLocationUpdates;
    protected Marker myLocationMarker;
    protected LatLng myPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_stations);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mRequestingLocationUpdates = false;

        updateValuesFromBundle(savedInstanceState);
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.d(TAG_LOG, "updateValuesFromBundle");
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(KEY_REQUESTING_LOCATION_UPDATES);
            }
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }
            updateUI();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        mLocationSettingsRequest = builder.build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG_LOG, "onActivityResult "+requestCode);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        break;
                    case Activity.RESULT_CANCELED:
                        mRequestingLocationUpdates = false;
                        updateUI();
                        break;
                }
                break;
        }
    }

    protected void startLocationUpdates() {
        if (!mRequestingLocationUpdates)
            mRequestingLocationUpdates = true;
        LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, mLocationSettingsRequest)
                .setResultCallback(new ResultCallback<LocationSettingsResult>() {
                    @Override
                    public void onResult(LocationSettingsResult result) {
                        final Status status = result.getStatus();
                        switch (status.getStatusCode()) {
                            case LocationSettingsStatusCodes.SUCCESS:
                                Log.d(TAG_LOG, "LocationSettingsStatusCodes.SUCCESS");
                                try {
                                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, MapStationsActivity.this);
                                } catch (SecurityException e) {
                                    Log.d(TAG_LOG, "ERROR " +e.toString());
                                }
                                break;
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.d(TAG_LOG, "LocationSettingsStatusCodes.RESOLUTION_REQUIRED");
                                try {
                                    status.startResolutionForResult(MapStationsActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException e) {
                                    Log.d(TAG_LOG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Toast.makeText(MapStationsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                        }
                        updateUI();
                    }
                });
    }

    private void updateUI() {
        Log.d(TAG_LOG, "updateUI " +mCurrentLocation);
        if (mCurrentLocation != null) {
            myPosition = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            if (myLocationMarker == null) {
                CameraPosition myCameraPosition = CameraPosition.builder()
                        .target(myPosition)
                        .zoom(15)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(myCameraPosition), DURATION_TIME_IN_MILLISECONDS, MapStationsActivity.this);
                addMyLocationMarker(myPosition);
            }
            else {
                myLocationMarker.setPosition(myPosition);
            }
        }
    }

    @Override
    public void onFinish() {

    }

    @Override
    public void onCancel() {

    }

    protected void stopLocationUpdates() {
        Log.d(TAG_LOG, "stopLocationUpdates");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                mRequestingLocationUpdates = false;
            }
        });
    }

    @Override
    protected void onStart() {
        Log.d(TAG_LOG, "onStart");
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        Log.d(TAG_LOG, "onResume");
        super.onResume();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        updateUI();
    }

    @Override
    protected void onPause() {
        Log.d(TAG_LOG, "onPause");
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG_LOG, "onStop");
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (mCurrentLocation == null) {
            try {
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            } catch (SecurityException e) {
                Log.d(TAG_LOG, "ERROR " +e.toString());
            }
            updateUI();
        }
        if (mRequestingLocationUpdates) {
            Log.d(TAG_LOG, "in onConnected(), starting location updates");
            startLocationUpdates();
        }
        Log.d(TAG_LOG, "onConnected " +mCurrentLocation + " / " + mRequestingLocationUpdates);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG_LOG, "onLocationChanged");
        mCurrentLocation = location;
        updateUI();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG_LOG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG_LOG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(MapStationsActivity.this);
        addLidaMarker();
        startLocationUpdates();
        addStationMarkers();
    }

    private void addLidaMarker() {
        mMap.addMarker(new MarkerOptions()
                .position(LIDA)
                .visible(false));
        CameraPosition lida = CameraPosition.builder()
                .target(LIDA)
                .zoom(13)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(lida));
    }

    private void addMyLocationMarker(LatLng position) {
        myLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(getString(R.string.map_mylocation))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    }

    private void addStationMarkers() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Map<LatLng, MarkerOptions> stationsMap = new HashMap<LatLng, MarkerOptions>();

                DatabaseAccess databaseAccess = DatabaseAccess.getInstance(MapStationsActivity.this);
                databaseAccess.open();
                Cursor stations = databaseAccess.getAllStations();
                while (!stations.isAfterLast()){
                    String position = stations.getString(stations.getColumnIndex("gps"));
                    if (position!=null && !position.isEmpty()) {
                        String[] positions = position.split(",");
                        if (positions.length == 2) {
                            double lat = 0;
                            double lng = 0;
                            try {
                                lat = Double.parseDouble(positions[0].trim());
                                lng = Double.parseDouble(positions[1].trim());
                            } catch (NumberFormatException e) {
                                Log.d(TAG_LOG, "Error while parsing coordinates: " + position);
                                continue;
                            }

                            LatLng markerLatLng = new LatLng(lat, lng);
                            stationsMap.put(markerLatLng, new MarkerOptions()
                                    .position(markerLatLng)
                                    .title(stations.getString(stations.getColumnIndex("station_name")))
                                    .snippet(stationsMap.containsKey(markerLatLng)
                                            ? stationsMap.get(markerLatLng).getSnippet()+", "+stations.getString(stations.getColumnIndex("route_number"))
                                            : getString(R.string.map_routes) + stations.getString(stations.getColumnIndex("route_number")))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.busstation_icon)));
                        }
                    }
                    stations.moveToNext();
                }
                databaseAccess.close();
                for (Map.Entry<LatLng, MarkerOptions> entry : stationsMap.entrySet()){
                    mMap.addMarker(entry.getValue());
                }
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (!marker.getSnippet().isEmpty()){
            new StationListDialog().show(getSupportFragmentManager(), marker.getTitle());
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back_menu: {
                finish();
                return true;
            }
            case R.id.about_menu: {
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
