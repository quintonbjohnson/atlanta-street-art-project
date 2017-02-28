package comquintonj.github.atlantastreetartproject.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
     * The context of the current state of the application
     */
    private Context context;

    /**
     * A reference to the Firebase database to store information about the art
     */
    private DatabaseReference mDatabase;

    /**
     * Image of the art
     */
    private ImageView imageOfArt;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_art_page);

        // No title in action bar
        setTitle("");

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
        ImageButton downvoteButton = (ImageButton) findViewById(R.id.downvote_button);
        ImageButton upvoteButton = (ImageButton) findViewById(R.id.upvote_button);

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
                    updateView();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // User has decided to give the art a downvote
        downvoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pieceOfArt.addDownvote();
                // Saving data to Firebase database
                mDatabase.child("Art").child(pieceOfArt.getPhotoPath()).setValue(pieceOfArt);
                // Update TextView to reflect increased downvote
                downvoteText.setText(pieceOfArt.getRatingDownvotes());
            }
        });

        // User has decided to give the art an upvote
        upvoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pieceOfArt.addUpvote();
                // Saving data to Firebase database
                mDatabase.child("Art").child(pieceOfArt.getPhotoPath()).setValue(pieceOfArt);
                // Update TextView to reflect increased upvote
                upvoteText.setText(pieceOfArt.getRatingUpvotes());
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
    public void updateView() {
        String artist = "Artist: " + pieceOfArt.getArtist();
        String submitter = "Submitter: " + pieceOfArt.getDisplayName();
        // Set the artist TextView
        artistText.setText(artist);
        // Set the submitter TextView
        submitterText.setText(submitter);

        upvoteText.setText(pieceOfArt.getRatingUpvotes());
        downvoteText.setText(pieceOfArt.getRatingDownvotes());

        // Set up proper image path and populate the image view
        StorageReference pathReference = mStorage.child("image/"
                + pieceOfArt.getPhotoPath());
        Glide.with(getApplicationContext())
                .using(new FirebaseImageLoader())
                .load(pathReference)
                .into(imageOfArt);
    }
}
