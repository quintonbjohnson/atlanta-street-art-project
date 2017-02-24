package comquintonj.github.atlantastreetartproject.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
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
     * Authentication instance of the FireabseAuth
     */
    private FirebaseAuth mAuth;

    /**
     * Status bar at the top of the Activity
     */
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_drawer);
        mAuth = FirebaseAuth.getInstance();

        // Set up toolbar at top of activity
        createToolbar();

        // Create the navigation drawer
        createDrawer();
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
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Creates the status bar at the top of the screen
     */
    public void createToolbar() {
        // Set up Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    /**
     * Creates and populates the navigation drawer
     */
    public void createDrawer() {
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
}
