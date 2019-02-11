package com.valdizz.busstation;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.valdizz.busstation.database.DatabaseAccess;
import com.valdizz.busstation.dialogs.StationListDialog;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.valdizz.busstation.database.DatabaseAccess.TAG_LOG;

public class MapStationsActivity extends AppCompatActivity implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener, GoogleMap.CancelableCallback, GoogleMap.OnMarkerClickListener {

    private static final LatLng LIDA = new LatLng(53.887350, 25.302713);
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int DURATION_TIME_IN_MILLISECONDS = 2500;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private Location mCurrentLocation;
    private Boolean mRequestingLocationUpdates;
    private Marker myLocationMarker;
    private FusedLocationProviderClient mFusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_stations);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mRequestingLocationUpdates = false;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        updateValuesFromBundle(savedInstanceState);
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
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

    private synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        mLocationSettingsRequest = builder.build();
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Location permission")
                        .setMessage("Check location permission!")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapStationsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        startLocationUpdates();
                    }
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    private void startLocationUpdates() {
        if (!mRequestingLocationUpdates)
            mRequestingLocationUpdates = true;
        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(this).checkLocationSettings(mLocationSettingsRequest);
        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location requests here.
                    if (ContextCompat.checkSelfPermission(MapStationsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback(){
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                for (Location location : locationResult.getLocations()) {
                                    mCurrentLocation = location;
                                    updateUI();
                                }
                            }
                        }, null);
                    }
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                                resolvable.startResolutionForResult(MapStationsActivity.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the settings so we won't show the dialog.
                            break;
                    }
                }
            }
        });
    }

    private void updateUI() {
        if (mCurrentLocation != null) {
            LatLng myPosition = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            if (myLocationMarker == null) {
                CameraPosition myCameraPosition = CameraPosition.builder()
                        .target(myPosition)
                        .zoom(15)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(myCameraPosition), DURATION_TIME_IN_MILLISECONDS, MapStationsActivity.this);
                addMyLocationMarker(myPosition);
            } else {
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

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                mRequestingLocationUpdates = false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkLocationPermission()) {
            if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
                startLocationUpdates();
            }
        }
        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (mCurrentLocation == null) {
            try {
                mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null)
                            mCurrentLocation = location;
                    }
                });
            } catch (SecurityException ignored) {
            }
            updateUI();
        }
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
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
                Map<LatLng, MarkerOptions> stationsMap = new HashMap<>();

                DatabaseAccess databaseAccess = DatabaseAccess.getInstance(MapStationsActivity.this);
                databaseAccess.open();
                Cursor stations = databaseAccess.getAllStations();
                while (!stations.isAfterLast()) {
                    String position = stations.getString(stations.getColumnIndex(DatabaseAccess.BUSSTATION_GPS));
                    if (position != null && !position.isEmpty()) {
                        String[] positions = position.split(",");
                        if (positions.length == 2) {
                            double lat;
                            double lng;
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
                                    .title(stations.getString(stations.getColumnIndex(DatabaseAccess.STATION_NAME)))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.busstation_icon)));
                        }
                    }
                    stations.moveToNext();
                }
                databaseAccess.close();
                for (Map.Entry<LatLng, MarkerOptions> entry : stationsMap.entrySet()) {
                    mMap.addMarker(entry.getValue());
                }
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (!marker.equals(myLocationMarker)) {
            Bundle stationListDialogArgs = new Bundle();
            stationListDialogArgs.putString(DatabaseAccess.STATION_NAME, marker.getTitle());
            StationListDialog stationListDialog = new StationListDialog();
            stationListDialog.setArguments(stationListDialogArgs);
            stationListDialog.show(getSupportFragmentManager(), String.format(Locale.ENGLISH, "%1$.6f, %2$.6f", marker.getPosition().latitude, marker.getPosition().longitude));
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:{
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
