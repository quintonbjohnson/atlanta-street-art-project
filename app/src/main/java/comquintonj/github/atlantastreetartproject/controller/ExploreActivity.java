package comquintonj.github.atlantastreetartproject.controller;

import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import comquintonj.github.atlantastreetartproject.R;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import comquintonj.github.atlantastreetartproject.model.ArtInformation;
import comquintonj.github.atlantastreetartproject.model.ItemClickSupport;


public class ExploreActivity extends BaseDrawerActivity {

    private ViewPager viewPager;
    private DrawerLayout drawer;
    private TabLayout tabLayout;
    private String[] pageTitle = {"NEARBY", "POPULAR", "NEW"};

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
     * A reference to the Firebase database to store information about the art
     */
    private DatabaseReference mRef;

    /*
    * ViewPagerAdapter object that is responsible for creating the fragments for each tab
    */
    private ViewPagerAdapter pagerAdapter;

    /*
     * Keeps track of the current tab selected by the user
     */
    private int selectedTab = 0;

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);
        setTitle("Explore");
        context = this;

        LayoutInflater inflater = (LayoutInflater)context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        View cardsList = inflater.inflate(
                R.layout.cards_list, null, false);
        mRecyclerView = (RecyclerView) cardsList.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //Sets the color of the status bar a few shades darker thant he toolbar
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.StatusBar));

        viewPager = (ViewPager)findViewById(R.id.view_pager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Check permissions
        allowed = checkLocationPermission();
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // If permissions are allowed, get the user's current location
        if (allowed) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                    0, mLocationListener);
            userLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        setSupportActionBar(toolbar);

        //create default navigation drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //setting Tab layout (number of Tabs = number of ViewPager pages)
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        for (int i = 0; i < 3; i++) {
            tabLayout.addTab(tabLayout.newTab().setText(pageTitle[i]));
        }

        //set gravity for tab bar
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Create the navigation drawer
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        createNavigationDrawer();
        navigationView.getMenu().getItem(0).setChecked(true);

        //set viewpager adapter
        this.pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), mRecyclerView);
        viewPager.setAdapter(pagerAdapter);

        //change Tab selection when swipe ViewPager
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        mRef = FirebaseDatabase.getInstance().getReference();

        //change ViewPager page when tab selected
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
                viewPager.setCurrentItem(tab.getPosition());
                updateContent();
                pagerAdapter.updateRecyclerView(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
                viewPager.setCurrentItem(tab.getPosition());
                updateContent();
                pagerAdapter.updateRecyclerView(tab.getPosition());
            }
        });

        //Opens to Tab 0 when the user enters the activity
        new Handler().postDelayed(
                new Runnable(){
                    @Override
                    public void run() {
                        tabLayout.getTabAt(0).select();
                    }
                }, 100);
    }

    public void updateContent () {

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
                        if (userLocation != null) {
                            double distanceInMeters = userLocation.distanceTo(artLocation);
                            double distanceInMiles = distanceInMeters / 1609.344;
                            pieceOfArt.setDistance(distanceInMiles);
                        }
                    }
                    pathAndDataMap.put(path, pieceOfArt);
                }
                // Sorts the recycler view based on the selected tab
                if (selectedTab == 0) {
                    sortByDistance();
                }
                else if (selectedTab == 1) {
                    sortByPopularity();
                }
                else{
                    sortByMostRecent();
                }
                populateAdapter(pathAndDataMap);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });

        // Add item click responder
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

    /**
     * Populates the adapter with art found in the pathAndDataMap.
     * @param pathAndDataMap the HashMap that contains the image path and data for all art
     */
    private void populateAdapter(HashMap<String, ArtInformation> pathAndDataMap) {
        adapter = new ExploreAdapter(this.getApplicationContext(), pathAndDataMap);
        mRecyclerView.setAdapter(adapter);
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
     * Sort art by most recently uploaded
     */
    private void sortByMostRecent() {
        // Get the array lists of data from the hash map
        ArrayList<ArtInformation> dataList =
                new ArrayList<>(pathAndDataMap.values());

        // Create a list that will hold ArtInformation objects
        ArrayList<ArtInformation> art = new ArrayList<>();

        for (ArtInformation data : dataList) {
            art.add(data);
        }

        // Pass in custom comparator to sort based on distance
        Collections.sort(art, new Comparator<ArtInformation>() {
            @Override
            public int compare(ArtInformation o1, ArtInformation o2) {
                if (o1.getCreatedAt() > o2.getCreatedAt()) {
                    return 1;
                } else if (o1.getCreatedAt() < o2.getCreatedAt()) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSIONS_MULTIPLE_REQUEST:
                if (grantResults.length > 0) {
                    boolean locationPermission
                            = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean readExternalFile = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if(locationPermission && readExternalFile)
                    {
                        recreate();
                    }
                }
                else {
                    Snackbar.make(this.findViewById(android.R.id.content),
                            "Please Grant Permissions to upload profile photo",
                            Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    requestPermissions(
                                            new String[]{Manifest.permission
                                                    .READ_EXTERNAL_STORAGE,
                                                    Manifest.permission.ACCESS_FINE_LOCATION},
                                            PERMISSIONS_MULTIPLE_REQUEST);
                                }
                            }).show();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}

