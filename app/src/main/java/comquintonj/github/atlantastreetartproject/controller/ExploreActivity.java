package comquintonj.github.atlantastreetartproject.controller;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

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

import comquintonj.github.atlantastreetartproject.R;
import comquintonj.github.atlantastreetartproject.model.ArtInformation;
import comquintonj.github.atlantastreetartproject.model.ExploreAdapter;

/**
 * Home page of the application that users will see when they log in.
 * Shows a list of the art that has been uploaded.
 */
public class ExploreActivity extends BaseDrawerActivity {

    /**
     * Adapter for the recycler view that allows it to show the art
     */
    private ExploreAdapter adapter;

    /**
     * Hash map used to store where the art is located and information about the art
     */
    private HashMap<String, ArrayList<String>> pathAndDataMap;

    /**
     * Recycler view that shows the list of cards which contain information about art
     */
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Create layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);
        setTitle("Explore");

        // Set up toolbar at top of activity
        createToolbar();

        // Create the navigation drawer
        createDrawer();

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
                        new HashMap<String, ArrayList<String>>();

                // Get the data of the art from each individual piece of art
                for (DataSnapshot dsp : dataSnapshot.child("Art").getChildren()) {
                    // Get the data of an individual piece of art
                    String title = String.valueOf(dsp.child("Title").getValue());
                    String path = "image/" + String.valueOf(dsp.getKey());
                    String artist = String.valueOf(dsp.child("Artist").getValue());
                    String location = String.valueOf(dsp.child("Location").getValue());
                    String displayName = String.valueOf(dsp.child("Display Name").getValue());
                    String rating = String.valueOf(dsp.child("Rating").getValue());

                    // Create an array list to hold the data for an individual piece of art
                    ArrayList<String> imageData = new ArrayList<String>();
                    imageData.add(artist);
                    imageData.add(displayName);
                    imageData.add(location);
                    imageData.add(path);
                    imageData.add(rating);
                    imageData.add(title);
                    pathAndDataMap.put(path, imageData);
                }
                populateAdapter(pathAndDataMap);

            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

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
            populateAdapter(pathAndDataMap);
        } else if (id == R.id.popularity_setting) {
            // Sort by popularity

            // Get the array lists of data from the hash map
            ArrayList<ArrayList<String>> dataList =
                    new ArrayList<ArrayList<String>>(pathAndDataMap.values());

            // Create a list that will hold ArtInformation objects
            ArrayList<ArtInformation> art = new ArrayList<ArtInformation>();

            // Iterate through the data for each key from the hash map,
            // and create ArtInformation objects from that
            for (ArrayList<String> data : dataList) {
                art.add(new ArtInformation(data.get(0), data.get(1),
                        data.get(2), data.get(3), data.get(4), data.get(5)));
            }

            // Pass in custom comparator to sort based on rating
            Collections.sort(art, new Comparator<ArtInformation>() {
                @Override
                public int compare(ArtInformation o1, ArtInformation o2) {
                    if (Integer.parseInt(o1.getRating()) > Integer.parseInt(o2.getRating())) {
                        return 1;
                    } else if (Integer.parseInt(o1.getRating()) < Integer.parseInt(o2.getRating())) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });

            // Reverse array list to put data into the new LinkedHashMap in correct order
            Collections.reverse(art);

            // Prepare to put sorted art back into a hash map
            HashMap<String, ArrayList<String>> resultMap = new LinkedHashMap<>();
            for (ArtInformation product : art) {
                ArrayList<String> resultData = new ArrayList<String>();
                resultData.add(product.getArtist());
                resultData.add(product.getDisplayName());
                resultData.add(product.getLocation());
                resultData.add(product.getPhotoPath());
                resultData.add(product.getRating());
                resultData.add(product.getTitle());
                resultMap.put(product.getPhotoPath(), resultData);
            }

            // Populate adapter with sorted map
            populateAdapter(resultMap);
            adapter.notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Populates the adapter with art found in the pathAndDataMap.
     * @param pathAndDataMap the HashMap that contains the image path and data for all art
     */
    public void populateAdapter(HashMap<String, ArrayList<String>> pathAndDataMap) {
        adapter = new ExploreAdapter(this.getApplicationContext(), pathAndDataMap);
        mRecyclerView.setAdapter(adapter);
    }
}