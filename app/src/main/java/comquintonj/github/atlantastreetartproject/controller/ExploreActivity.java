package comquintonj.github.atlantastreetartproject.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
import comquintonj.github.atlantastreetartproject.model.ExploreAdapter;
import comquintonj.github.atlantastreetartproject.model.ItemClickSupport;

/**
 * Home page of the application that users will see when they log in.
 * Shows a list of the art that has been uploaded.
 */
public class ExploreActivity extends BaseDrawerActivity {

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
    private LinkedHashMap<String, ArrayList<String>> pathAndDataMap;

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

        context = this;

        // Create the navigation drawer
        createNavigationDrawer();

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

                    // Create an array list to hold the data for an individual piece of art
                    ArrayList<String> imageData = new ArrayList<>();
                    imageData.add(pieceOfArt.getArtist());
                    imageData.add(pieceOfArt.getDisplayName());
                    imageData.add(pieceOfArt.getLocation());
                    imageData.add(pieceOfArt.getPhotoPath());
                    imageData.add(pieceOfArt.getRatingDownvotes());
                    imageData.add(pieceOfArt.getRatingUpvotes());
                    imageData.add(pieceOfArt.getTitle());
                    String path = "image/" + String.valueOf(dsp.getKey());
                    pathAndDataMap.put(path, imageData);
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
                        String item = pathAndDataMap.get(index).get(3);
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
            populateAdapter(pathAndDataMap);
            adapter.notifyDataSetChanged();
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
    private void populateAdapter(HashMap<String, ArrayList<String>> pathAndDataMap) {
        adapter = new ExploreAdapter(this.getApplicationContext(), pathAndDataMap);
        mRecyclerView.setAdapter(adapter);
    }

    /**
     * Sort the art found in the RecyclerView by popularity
     * @return the newly sorted art hash map
     */
    private void sortByPopularity() {
        // Sort by popularity

        // Get the array lists of data from the hash map
        ArrayList<ArrayList<String>> dataList =
                new ArrayList<>(pathAndDataMap.values());

        // Create a list that will hold ArtInformation objects
        ArrayList<ArtInformation> art = new ArrayList<>();

        // Iterate through the data for each key from the hash map,
        // and create ArtInformation objects from that
        for (ArrayList<String> data : dataList) {
            art.add(new ArtInformation(data.get(0), data.get(1),
                    data.get(2), data.get(3), data.get(4), data.get(5), data.get(6)));
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
        LinkedHashMap<String, ArrayList<String>> resultMap = new LinkedHashMap<>();
        for (ArtInformation product : art) {
            ArrayList<String> resultData = new ArrayList<>();
            resultData.add(product.getArtist());
            resultData.add(product.getDisplayName());
            resultData.add(product.getLocation());
            resultData.add(product.getPhotoPath());
            resultData.add(product.getRatingDownvotes());
            resultData.add(product.getRatingUpvotes());
            resultData.add(product.getTitle());
            resultMap.put("image/" + product.getPhotoPath(), resultData);
        }
        pathAndDataMap = resultMap;
    }
}
