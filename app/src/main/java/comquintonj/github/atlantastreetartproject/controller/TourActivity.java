package comquintonj.github.atlantastreetartproject.controller;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import comquintonj.github.atlantastreetartproject.R;
import comquintonj.github.atlantastreetartproject.model.ArtInformation;
import comquintonj.github.atlantastreetartproject.model.ItemClickSupport;
import comquintonj.github.atlantastreetartproject.model.User;

public class TourActivity extends BaseDrawerActivity {

    /**
     * Utilizes a list of ArtInformation objects to access by index
     */
    private ArrayList<ArtInformation> listOfTourArt;

    /**
     * Keeps track of if user has allowed location permission
     */
    private boolean allowed;

    /**
     * The context of the current state of the application
     */
    private Context context;

    /**
     * Action button to begin tour
     */
    private FloatingActionButton fab;

    /**
     * Hash map used to store where the art is located and information about the art
     */
    private LinkedHashMap<String, ArtInformation> tourMap;

    /**
     * The current location of the user
     */
    private Location userLocation;

    /**
     * A LocationListener to keep track of the user's location
     */
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {


        }

        @Override
        public void onProviderDisabled(String s) {
            Toast.makeText(context, "Please enable GPS", Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * Recycler view that shows the list of cards which contain information about art
     */
    private RecyclerView mRecyclerView;

    /**
     * Text that displays if the tour is empty
     */
    private TextView tourText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour);
        setTitle("Tour");
        context = this;

        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        allowed = checkLocationPermission();
        if (allowed) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                    0, mLocationListener);
            userLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        // Initialize View
        fab = (FloatingActionButton) findViewById(R.id.fabButton);
        tourText = (TextView) findViewById(R.id.empty_tour_text);
        DrawableCompat.setTint(fab.getDrawable(),
                ContextCompat.getColor(context, R.color.tw__composer_white));

        // Create the navigation drawer
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        createNavigationDrawer();
        navigationView.getMenu().getItem(2).setChecked(true);

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();

        // Initiate Recycler View
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        // Use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        populateAdapter();

        // Add item click responder
        ItemClickSupport.addTo(mRecyclerView)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        List<String> indexes = new ArrayList<String>(tourMap.keySet());
                        String index = indexes.get(position);
                        String item = tourMap.get(index).getPhotoPath();
                        Intent artIntent = new Intent(context, ArtPageActivity.class);
                        artIntent.putExtra("ArtPath", item);
                        startActivity(artIntent);
                    }
                });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // User wants to navigate to the art in their tour
                listOfTourArt = new ArrayList<>(tourMap.values());
                if (userLocation != null) {
                    // Begin to build url for navigating through art
                    String url = "https://maps.google.com/maps?saddr=My+Location&daddr=";
                    // Start with the first piece of art
                    ArtInformation firstArt = listOfTourArt.get(0);
                    url = url + firstArt.getLatitude() + "," + firstArt.getLongitude();
                    if (listOfTourArt.size() > 1) {
                        // For each piece of art in the tour, add it to the navigation
                        for (int i = 1; i < listOfTourArt.size(); i++) {
                            url = url + "+to:";
                            ArtInformation art = listOfTourArt.get(i);
                            url = url + art.getLatitude() + "," + art.getLongitude() + "/";
                        }
                    }
                    url = url + "&mode=walking";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Location could not be found, please try again later",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Populates the adapter with art found in the pathAndDataMap.
     */
    private void populateAdapter() {
        // If the tour is Empty, show User a hint
        if (User.tourArt.isEmpty()) {
            String readText = "Add art to your tour to \n navigate through multiple pieces of art.";
            tourText.setText(readText);
            fab.hide();
        } else {
            fab.show();
            tourMap = new LinkedHashMap<>();
            for (ArtInformation art : User.tourArt) {
                tourMap.put("image/" + art.getPhotoPath(), art);
                RecyclerAdapter adapter =
                        new RecyclerAdapter(this.getApplicationContext(), tourMap);
                mRecyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onRestart() {
        // If User has come back from viewing an ArtPageActivity
        super.onRestart();
        Intent resume = getIntent();
        finish();
        startActivity(resume);
    }
}
