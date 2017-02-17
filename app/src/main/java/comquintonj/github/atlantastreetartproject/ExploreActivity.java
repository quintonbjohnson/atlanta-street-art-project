package comquintonj.github.atlantastreetartproject;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.util.Pair;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class ExploreActivity extends BaseDrawerActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private FirebaseAuth mAuth;
    private StorageReference storageRef;
    private DatabaseReference mRef;
    private HashMap<String, Pair<String, String>> pathAndDataMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Create layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);
        setTitle("Explore");

        // Get Instance of Firebase
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        mRef = FirebaseDatabase.getInstance().getReference();

        // Set up Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set profile name
        View header = navigationView.getHeaderView(0);
        FirebaseUser user = mAuth.getCurrentUser();
        TextView headerName = (TextView) header.findViewById(R.id.profileNameText);
        assert user != null;
        headerName.setText(user.getEmail());

        // Initiate Recycler View
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        // Use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                pathAndDataMap =
                        new HashMap<String, Pair<String, String>>();

                // Result will be holded Here
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    String path = "image/" + String.valueOf(dsp.getKey());
                    String artist = String.valueOf(dsp.child("Artist").getValue());
                    String location = String.valueOf(dsp.child("Location").getValue());
                    pathAndDataMap.put(path, new Pair(artist, location));
                }
                populateAdapter(pathAndDataMap);

            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }

    public void populateAdapter(HashMap<String, Pair<String, String>> pathAndDataMap) {
        // Populate Adapter
        MyAdapter adapter = new MyAdapter(this.getApplicationContext(), pathAndDataMap);
        mRecyclerView.setAdapter(adapter);
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

        }

        return super.onOptionsItemSelected(item);
    }
}
