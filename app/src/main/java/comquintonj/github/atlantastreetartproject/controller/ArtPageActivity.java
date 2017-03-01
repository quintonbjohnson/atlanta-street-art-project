package comquintonj.github.atlantastreetartproject.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
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

import comquintonj.github.atlantastreetartproject.R;
import comquintonj.github.atlantastreetartproject.model.ArtInformation;

/**
 * The screen for an individual piece of art that the user is taken to after selecting an image
 * on the explore screen.
 */
public class ArtPageActivity extends AppCompatActivity {

    /**
     * An ArtInformation object to store data about the individual piece of art
     */
    private ArtInformation pieceOfArt;

    /**
     * Keeps track of whether or not the art has been upvoted
     */
    private boolean upvoteSet = false;

    /**
     * Keeps track of whether or not the art has been downvoted
     */
    private boolean downvoteSet = false;

    /**
     * The context of the current state of the application
     */
    private Context context;

    /**
     * A reference to the Firebase database to store information about the art
     */
    private DatabaseReference mDatabase;

    /**
     * Current user of the application
     */
    private FirebaseUser user;

    /**
     * Image of the art
     */
    private ImageView imageOfArt;

    /**
     * Upvote button to rate art
     */
    private ImageButton upvoteButton;


    /**
     * Downvote button to rate art
     */
    private ImageButton downvoteButton;

    /**
     * An intent to go back to the explore screen
     */
    private Intent exploreIntent;

    /**
     * A reference to the Firebase storage kept in order to upload images
     */
    private StorageReference mStorage;

    /**
     * The TextView to show the artist of the art
     */
    private TextView artistText;

    /**
     * The TextView to show the submitter of the art
     */
    private TextView submitterText;

    /**
     * The TextView to show the number of downvotes the art has
     */
    private TextView downvoteText;

    /**
     * The TextView to show the number of upvotes the art has
     */
    private TextView upvoteText;

    private HashMap<String, String> artRated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_art_page);

        // No title in action bar
        setTitle("");

        user = FirebaseAuth.getInstance().getCurrentUser();

        // Set up back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Initialize instance variables
        context = this;
        exploreIntent = new Intent(context, ExploreActivity.class);

        // Initialize view
        imageOfArt = (ImageView) findViewById(R.id.art_image_view);
        artistText = (TextView) findViewById(R.id.art_artist_text);
        submitterText = (TextView) findViewById(R.id.art_submitter_text);
        downvoteText = (TextView) findViewById(R.id.downvote_text);
        upvoteText = (TextView) findViewById(R.id.upvote_text);
        downvoteButton = (ImageButton) findViewById(R.id.downvote_button);
        upvoteButton = (ImageButton) findViewById(R.id.upvote_button);

        // Initialize Firebase references
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        // Grab the path of the art that was selected
        final String bundleExtra = getIntent().getExtras().getString("ArtPath");

        // Retrieve the ArtInformation object from Firebase
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (bundleExtra != null) {
                    // Using the path, find the piece of art in the database
                    pieceOfArt = dataSnapshot.child("Art")
                            .child(bundleExtra).getValue(ArtInformation.class);
                    updateArtView();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Retrieve the User object from Firebase
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (bundleExtra != null) {
                    // Using the path, find the piece of art in the database
                    if (user != null) {
                        artRated = (HashMap<String, String>) dataSnapshot.child("Users")
                                .child(user.getDisplayName()).child("rated").getValue();
                        updateRatingView();
                        addClickListeners();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                exploreIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(exploreIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Glide.get(context).clearMemory();
    }

    /**
     * Initialize the information about the art
     */
    public void updateArtView() {
        String artist = "Artist: " + pieceOfArt.getArtist();
        String submitter = "Submitter: " + pieceOfArt.getDisplayName();
        // Set the artist TextView
        artistText.setText(artist);
        // Set the submitter TextView
        submitterText.setText(submitter);

        // Set up proper image path and populate the image view
        StorageReference pathReference = mStorage.child("image/"
                + pieceOfArt.getPhotoPath());
        Glide.with(getApplicationContext())
                .using(new FirebaseImageLoader())
                .load(pathReference)
                .into(imageOfArt);
    }

    /**
     * Update the ratings of the art
     */
    private void updateRatingView() {
        upvoteText.setText(pieceOfArt.getRatingUpvotes());
        downvoteText.setText(pieceOfArt.getRatingDownvotes());
        if (artRated != null) {
            // Check to see if user has already rated this piece of art
            if (artRated.containsKey(pieceOfArt.getPhotoPath())) {
                String rated = (String) artRated.get(pieceOfArt.getPhotoPath());
                if (rated.equals("Upvoted")) {
                    // If the user has already upvoted the art, highlight the upvote button
                    turnOffDownvote();
                    setUpvoteButton();
                    upvoteSet = true;
                } else if (rated.equals("Downvoted")){
                    // If the user has already downvoted the art, highlight the downvote button
                    turnOffUpvote();
                    setDownvoteButton();
                    downvoteSet = true;
                }
            }
        }
    }

    private void addClickListeners() {
        // User has decided to give the art a downvote
        downvoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (downvoteSet) {
                    // Do nothing
                } else if (upvoteSet) {
                    // The user has already upvoted this art
                    upvoteSet = false;
                    turnOffUpvote();
                    setDownvoteButton();
                    pieceOfArt.decUpvote();
                    pieceOfArt.incDownvote();
                    downvoteSet = true;
                } else {
                    // The user hasn't voted on this art
                    downvoteSet = true;
                    setDownvoteButton();
                    pieceOfArt.incDownvote();
                }

                // Store changes in Firebase database
                mDatabase.child("Users").child(user.getDisplayName())
                        .child("rated").child(pieceOfArt.getPhotoPath()).setValue("Downvoted");
                mDatabase.child("Art").child(pieceOfArt.getPhotoPath()).setValue(pieceOfArt);

                // Update TextView to reflect increased downvote
                downvoteText.setText(pieceOfArt.getRatingDownvotes());
            }
        });

        // User has decided to give the art an upvote
        upvoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (upvoteSet) {
                    // Do nothing
                } else if (downvoteSet) {
                    // The user has already downvoted this art
                    downvoteSet = false;
                    turnOffDownvote();
                    setUpvoteButton();
                    pieceOfArt.decDownvote();
                    pieceOfArt.incUpvote();
                    upvoteSet = true;
                } else {
                    // The user hasn't voted on this art
                    upvoteSet = true;
                    setUpvoteButton();
                    pieceOfArt.incUpvote();
                }

                // Store changes in Firebase database
                mDatabase.child("Users").child(user.getDisplayName())
                        .child("rated").child(pieceOfArt.getPhotoPath()).setValue("Upvoted");
                mDatabase.child("Art").child(pieceOfArt.getPhotoPath()).setValue(pieceOfArt);

                // Update TextView to reflect increased upvote
                upvoteText.setText(pieceOfArt.getRatingUpvotes());
            }
        });
    }

    /**
     * Denote an upvote by coloring the button the accent color
     */
    public void setUpvoteButton() {
        DrawableCompat.setTint(upvoteButton.getDrawable(),
                ContextCompat.getColor(context, R.color.colorAccent));
    }

    /**
     * Denote an downvote by coloring the button the accent color
     */
    public void setDownvoteButton() {
        DrawableCompat.setTint(downvoteButton.getDrawable(),
                ContextCompat.getColor(context, R.color.colorAccent));
    }

    /**
     * Change the color of the upvote button back to black
     */
    public void turnOffUpvote() {
        DrawableCompat.setTint(upvoteButton.getDrawable(),
                ContextCompat.getColor(context, R.color.tw__composer_black));
    }

    /**
     * Change the color of the downvote button back to black
     */
    public void turnOffDownvote() {
        DrawableCompat.setTint(downvoteButton.getDrawable(),
                ContextCompat.getColor(context, R.color.tw__composer_black));
    }
}

