package comquintonj.github.atlantastreetartproject.controller;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import comquintonj.github.atlantastreetartproject.R;

/**
 * Base navigation drawer activity that other activities extend in order to provide navigation
 * drawer functionality across multiple activities
 */
public class BaseDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * Authentication instance of the FirebaseAuth
     */
    private FirebaseAuth mAuth;

    /**
     * Used to access permission requests
     */
    public  static final int PERMISSIONS_MULTIPLE_REQUEST = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_drawer);
        mAuth = FirebaseAuth.getInstance();
        checkPermission();

        // Create the navigation drawer
        createNavigationDrawer();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (mAuth.getCurrentUser() != null) {
            // Handle navigation view item clicks here.
            int id = item.getItemId();

            // If Explore screen is chosen
            if (id == R.id.nav_explore) {
                // Check to make sure the user is not currently in the Explore Activity
                if (this.getClass().equals(ExploreActivity.class)) {
                    onBackPressed();
                } else {
                    Intent exploreIntent = new Intent(this, ExploreActivity.class);
                    startActivity(exploreIntent);
                }
            } else if (id == R.id.nav_map) {

            } else if (id == R.id.nav_tour) {

            } else if (id == R.id.nav_submit) {
                // Check to make sure the user is not currently in the Submit Activity
                if (this.getClass().equals(SubmitActivity.class)) {
                    onBackPressed();
                } else if (mAuth.getCurrentUser().getDisplayName() == null) {
                    Toast.makeText(this, "Please create an account to use this feature.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent submitIntent = new Intent(this, SubmitActivity.class);
                    startActivity(submitIntent);
                }
            } else if (id == R.id.nav_sign_out) {
                // Sign out the User
                mAuth.signOut();
                Intent introIntent = new Intent(this, IntroActivity.class);
                startActivity(introIntent);
                finish();
            }

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        return false;
    }

    /**
     * Creates and populates the navigation drawer
     */
    public void createNavigationDrawer() {
        // Set up Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Set navigation drawer
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);


        // Set profile name in navigation drawer
        TextView headerName = (TextView)header.findViewById(R.id.profileNameText);
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        headerName.setText(user.getDisplayName());
    }

    /**
     * Check to see if the user has allowed permissions for reading storage and accessing location.
     */
    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat
                .checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale
                            (this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                Snackbar.make(this.findViewById(android.R.id.content),
                        "Granting permissions will allow you to see the location of art around you",
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
            } else {
                requestPermissions(
                        new String[]{Manifest.permission
                                .READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_MULTIPLE_REQUEST);
            }
        }
    }

    /**
     * Check to see if the user has given permission to read external storage
     * @return whether or not the user has given permission
     */
    public boolean checkReadPermission() {
        int result = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check to see if the user has given permission to read external storage
     * @return whether or not the user has given permission
     */
    public boolean checkLocationPermission() {
        int result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
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
}
