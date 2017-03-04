package comquintonj.github.atlantastreetartproject.controller;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import comquintonj.github.atlantastreetartproject.R;
import comquintonj.github.atlantastreetartproject.model.ArtInformation;
import comquintonj.github.atlantastreetartproject.model.ItemClickSupport;

/**
 * Home page of the application that users will see when they log in.
 * Shows a list of the art that has been uploaded.
 */
public class ExploreActivity extends BaseDrawerActivity {

    /**
     * Check to see if user has permissions enabled
     */
    private boolean allowed;

    /**
     * The context of the current state of the application
     */
    private Context context;

    /**
     * Adapter for the recycler view that allows it to show the art
     */
    private ExploreAdapter adapter;

    /**
     * Hash map used to store where the art is located and information about the art
     */
    private LinkedHashMap<String, ArtInformation> pathAndDataMap;

    /**
     * Recycler view that shows the list of cards which contain information about art
     */
    private RecyclerView mRecyclerView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Create layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);
        setTitle("Explore");
        context = this;

        allowed = checkLocationPermission();
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (allowed) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                    0, mLocationListener);
            userLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }


        // Create the navigation drawer
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        createNavigationDrawer();
        navigationView.getMenu().getItem(0).setChecked(true);

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();

        // Initiate Recycler View
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        // Use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                pathAndDataMap =
                        new LinkedHashMap<>();

                // Get the data of the art from each individual piece of art
                for (DataSnapshot dsp : dataSnapshot.child("Art").getChildren()) {
                    ArtInformation pieceOfArt = dsp.getValue(ArtInformation.class);
                    String path = "image/" + String.valueOf(dsp.getKey());
                    if (allowed) {
                        Location artLocation = new Location("");
                        artLocation.setLatitude(pieceOfArt.getLatitude());
                        artLocation.setLongitude(pieceOfArt.getLongitude());
                        double distanceInMeters = userLocation.distanceTo(artLocation);
                        double distanceInMiles = distanceInMeters / 1609.344;
                        pieceOfArt.setDistance(distanceInMiles);
                    }
                    pathAndDataMap.put(path, pieceOfArt);
                }
                populateAdapter(pathAndDataMap);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });

        ItemClickSupport.addTo(mRecyclerView)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        List<String> indexes = new ArrayList<String>(pathAndDataMap.keySet());
                        String index = indexes.get(position);
                        String item = pathAndDataMap.get(index).getPhotoPath();
                        Intent artIntent = new Intent(context, ArtPageActivity.class);
                        artIntent.putExtra("ArtPath", item);
                        startActivity(artIntent);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.distance_setting) {
            // Sort by distance
            if (allowed) {
                sortByDistance();
                populateAdapter(pathAndDataMap);
                adapter.notifyDataSetChanged();
            } else {
                checkPermission();
            }
        } else if (id == R.id.popularity_setting) {
            // Sort by popularity
            sortByPopularity();
            populateAdapter(pathAndDataMap);
            adapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Populates the adapter with art found in the pathAndDataMap.
     * @param pathAndDataMap the HashMap that contains the image path and data for all art
     */
    private void populateAdapter(HashMap<String, ArtInformation> pathAndDataMap) {
        adapter = new ExploreAdapter(this.getApplicationContext(), pathAndDataMap);
        mRecyclerView.setAdapter(adapter);
    }

    /**
     * Sort the art found in the RecyclerView by popularity
     */
    private void sortByPopularity() {
        // Get the array lists of data from the hash map
        ArrayList<ArtInformation> dataList =
                new ArrayList<>(pathAndDataMap.values());

        // Create a list that will hold ArtInformation objects
        ArrayList<ArtInformation> art = new ArrayList<>();

        // Iterate through the data for each key from the hash map,
        // and create ArtInformation objects from that
        for (ArtInformation data : dataList) {
            art.add(data);
        }

        // Pass in custom comparator to sort based on rating
        Collections.sort(art, new Comparator<ArtInformation>() {
            @Override
            public int compare(ArtInformation o1, ArtInformation o2) {
                if (o1.getRating() > o2.getRating()) {
                    return 1;
                } else if (o1.getRating() < o2.getRating()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        // Reverse array list to put data into the new LinkedHashMap in correct order
        Collections.reverse(art);

        // Prepare to put sorted art back into a hash map
        LinkedHashMap<String, ArtInformation> resultMap = new LinkedHashMap<>();
        for (ArtInformation product : art) {
            resultMap.put("image/" + product.getPhotoPath(), product);
        }
        pathAndDataMap = resultMap;
    }

    /**
     * Sort art based on distance
     */
    private void sortByDistance() {
        // Get the array lists of data from the hash map
        ArrayList<ArtInformation> dataList =
                new ArrayList<>(pathAndDataMap.values());

        // Create a list that will hold ArtInformation objects
        ArrayList<ArtInformation> art = new ArrayList<>();

        for (ArtInformation data : dataList) {
            Location artLocation = new Location("");
            artLocation.setLatitude(data.getLatitude());
            artLocation.setLongitude(data.getLongitude());
            double distanceInMeters = userLocation.distanceTo(artLocation);
            double distanceInMiles = distanceInMeters / 1609.344;
            data.setDistance(distanceInMiles);
            art.add(data);
        }

        // Pass in custom comparator to sort based on distance
        Collections.sort(art, new Comparator<ArtInformation>() {
            @Override
            public int compare(ArtInformation o1, ArtInformation o2) {
                if (o1.getDistance() > o2.getDistance()) {
                    return 1;
                } else if (o1.getDistance() < o2.getDistance()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        // Prepare to put sorted art back into a hash map
        LinkedHashMap<String, ArtInformation> resultMap = new LinkedHashMap<>();
        for (ArtInformation product : art) {
            resultMap.put("image/" + product.getPhotoPath(), product);
        }
        pathAndDataMap = resultMap;
    }
}
