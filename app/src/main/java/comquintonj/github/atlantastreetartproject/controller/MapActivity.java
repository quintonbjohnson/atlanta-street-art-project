package comquintonj.github.atlantastreetartproject.controller;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedHashMap;

import comquintonj.github.atlantastreetartproject.R;
import comquintonj.github.atlantastreetartproject.model.ArtInformation;

/**
 * Map screen that shows a visual location of the art
 */
public class MapActivity extends BaseDrawerActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMarkerClickListener {

    /**
     * A reference to the Firebase database to store information about the art
     */
    DatabaseReference mRef;

    /**
     * LocationRequest for the user's current location
     */
    LocationRequest mLocationRequest;

    /**
     * API client for the Map
     */
    GoogleApiClient mGoogleApiClient;

    /**
     * User's location coordinates
     */
    LatLng userLocation;

    /**
     * Check to see if user has permissions enabled
     */
    private boolean allowed;

    /**
     * The context of the current state of the application
     */
    private Context context;

    /**
     * Hash map used to store where the art is located and information about the art
     */
    private LinkedHashMap<String, ArtInformation> pathAndDataMap;

    /**
     * The Map shown on screen
     */
    GoogleMap mGoogleMap;

    /**
     * Marker used to keep track of the user's current location
     */
    Marker currLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setTitle("Map");

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_map);

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Create the navigation drawer
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        createNavigationDrawer();
        navigationView.getMenu().getItem(1).setChecked(true);

        // Connect to Firebase
        mRef = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Initialize the map, add listener to markers, and show the user's location and art
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mGoogleMap = map;
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.hideInfoWindow();
                String markerTitle = marker.getTitle();
                ArtInformation art = pathAndDataMap.get(markerTitle);
                Intent artIntent = new Intent(context, ArtPageActivity.class);
                artIntent.putExtra("ArtPath", art.getPhotoPath());
                startActivity(artIntent);
                return true;
            }
        });

        // Check permissions
        allowed = checkLocationPermission();
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // If permissions are allowed, get the user's current location
        if (allowed) {
            mGoogleMap.setMyLocationEnabled(true);
        }
        // Build Map Client
        buildGoogleApiClient();
        mGoogleApiClient.connect();

        // Add art to map
        addArt();
    }

    /**
     * Build the map client
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Retrieves art from the database and shows it in the map
     */
    public void addArt() {
        // Pull in data from Firebase for art
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                pathAndDataMap =
                        new LinkedHashMap<>();

                // Get the data of the art from each individual piece of art
                for (DataSnapshot dsp : dataSnapshot.child("Art").getChildren()) {
                    ArtInformation pieceOfArt = dsp.getValue(ArtInformation.class);
                    String path = "image/" + String.valueOf(dsp.getKey());
                    pathAndDataMap.put(path, pieceOfArt);
                }

                // For each piece of art in the HashMap, add a marker for it
                for (ArtInformation pieceOfArt : pathAndDataMap.values()) {
                    LatLng artCoordinates = new LatLng(pieceOfArt.getLatitude(), pieceOfArt.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(artCoordinates);
                    markerOptions.title("image/" + pieceOfArt.getPhotoPath());
                    mGoogleMap.addMarker(markerOptions);
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        checkPermission();
        if (allowed) {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                userLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            }
        }
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); //5 seconds
        mLocationRequest.setFastestInterval(3000); //3 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this,"Location services suspended",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this,"Can't connect to location services",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Place marker at current position
        if (currLocationMarker != null) {
            currLocationMarker.remove();
        }
        userLocation = new LatLng(location.getLatitude(), location.getLongitude());

        // Zoom to current position:
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        marker.hideInfoWindow();
        String markerTitle = marker.getTitle();
        ArtInformation art = pathAndDataMap.get(markerTitle);
        Intent artIntent = new Intent(context, ArtPageActivity.class);
        artIntent.putExtra("ArtPath", art.getPhotoPath());
        startActivity(artIntent);
        return true;
    }
}
