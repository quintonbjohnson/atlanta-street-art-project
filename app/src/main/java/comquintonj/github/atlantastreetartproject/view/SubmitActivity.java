package comquintonj.github.atlantastreetartproject.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Random;

import comquintonj.github.atlantastreetartproject.R;
import comquintonj.github.atlantastreetartproject.model.ArtInformation;

public class SubmitActivity extends BaseDrawerActivity {

    private DatabaseReference mDatabase;
    private EditText titleText, locationText, artistText, ratingText;
    private Button submitButton;
    private ImageButton imageSelectButton;
    private FirebaseAuth mAuth;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private static final String TAG = "MyActivity";
    private String placeId;
    private FirebaseUser user;

    // A constant to track the file chooser intent
    private static final int PICK_IMAGE_REQUEST = 234;

    StorageReference storageReference;

    // ImageView
    private ImageView imageView;

    // A Uri object to store file path
    private Uri filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Create layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);
        setTitle("Submit");

        // Get Instance of Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

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
        user = mAuth.getCurrentUser();
        TextView headerName = (TextView) header.findViewById(R.id.profileNameText);
        assert user != null;
        headerName.setText(user.getDisplayName());

        // Instantiate resources
        titleText = (EditText) findViewById(R.id.titleText);
        locationText = (EditText) findViewById(R.id.locationTextView);
        artistText = (EditText) findViewById(R.id.artistTag);
        submitButton = (Button) findViewById(R.id.submitButton);
        imageSelectButton = (ImageButton) findViewById(R.id.imageSelect);
        storageReference = FirebaseStorage.getInstance().getReference();

        // Set on click listener for the submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == submitButton) {
                    submitArtInformation();
                    uploadFile();
                }
            }
        });

        // Set on click listener for choosing the image button
        imageSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == imageSelectButton) {
                    showFileChooser();
                }
            }
        });

        GoogleApiClient mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, new OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(SubmitActivity.this,
                                "Please try again later.", Toast.LENGTH_SHORT).show();
                    }
                })
                .build();

        locationText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(MotionEvent.ACTION_UP == event.getAction()) {
                    findPlace(v);
                }

                return true; // return is important...
            }
        });
    }

    // Submit art information to the database
    private void submitArtInformation() {
        // Get current user
        user = mAuth.getCurrentUser();

        // Getting values from database
        final String title = titleText.getText().toString().trim();
        String artist = artistText.getText().toString().trim();
        String location = placeId;
        assert user != null;
        String photoPath = titleText.getText().toString().trim() + user.getDisplayName();
        String displayName = user.getDisplayName();
        Random rand = new Random();
        String rating = String.valueOf(rand.nextInt(100));

        ArtInformation pieceOfArt = new ArtInformation(artist, displayName,
                location, photoPath, rating, title);

        // Saving data to Firebase database
        mDatabase.child("Art").child(photoPath).child("Artist").setValue(pieceOfArt.getArtist());
        mDatabase.child("Art").child(photoPath).child("Location").setValue(pieceOfArt.getLocation());
        mDatabase.child("Art").child(photoPath).child("Display Name").setValue(pieceOfArt.getDisplayName());
        mDatabase.child("Art").child(photoPath).child("Rating").setValue(pieceOfArt.getRating());
    }

    // Method to show file chooser
    public void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    // Handling the image chooser activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If user is trying to select a place
        if (requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                final Place place = PlaceAutocomplete.getPlace(this, data);
                placeId = place.getId();
                final String placeName = place.getName().toString();
                Log.i(TAG, "Place Selected: " + place.getName());

                Runnable thRead = new Runnable(){
                    public void run() {
                        locationText.setText(placeName);
                    }
                };
                runOnUiThread(thRead);

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.e(TAG, "Error: Status = " + status.toString());
            } else if (resultCode == RESULT_CANCELED) {
                // User left search intent
            }
        }

        // If user is trying to select an image
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                Bitmap resizedImage = resizeImage(bitmap);
                if (resizedImage != null) {
                    imageSelectButton.setImageBitmap(resizedImage);
                    imageSelectButton.setBackgroundColor(255);

                    Toast.makeText(this, "Set", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Please try again with a different image.",
                            Toast.LENGTH_SHORT).show();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Upload the file to Firebase
    public void uploadFile() {
        // If there is a file to upload
        if (filePath != null) {
            // Displaying a progress dialog while upload is going on
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            StorageReference riversRef = storageReference.child("image/"
                    + titleText.getText().toString().trim()
                    + user.getDisplayName());
            riversRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload is successful
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying a success toast
                            Toast.makeText(getApplicationContext(), "File Uploaded ",
                                    Toast.LENGTH_LONG).show();
                            Intent exploreIntent = new Intent(SubmitActivity.this,
                                    ExploreActivity.class);
                            startActivity(exploreIntent);
                        }
                    })

                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //calculating progress percentage
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) /
                                    taskSnapshot.getTotalByteCount();

                            //displaying percentage in progress dialog
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });
        }
        // If there is not a file
        else {
            // Display error toast
            Toast.makeText(getApplicationContext(), "No file selected", Toast.LENGTH_LONG).show();
        }
    }

    // Credits: http://stackoverflow.com/
    // questions/15124179/resizing-a-bitmap-to-a-fixed-value-but-without-changing-the-aspect-ratio
    private Bitmap resizeImage(Bitmap bm) {
        if (bm != null) {
            int width = bm.getWidth();
            int height = bm.getHeight();
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int maxWidth = size.x;
            int maxHeight = 500;
            Log.v("Pictures", "Width and height are " + width + "--" + height);

            if (width > height) {
                // landscape
                float ratio = (float) width / maxWidth;
                width = maxWidth;
                height = (int) (height / ratio);
            } else if (height > width) {
                // portrait
                float ratio = (float) height / maxHeight;
                height = maxHeight;
                width = (int) (width / ratio);
            } else {
                // square
                height = maxHeight;
                width = maxWidth;
            }

            Log.v("Pictures", "after scaling Width and height are " + width + "--" + height);

            bm = Bitmap.createScaledBitmap(bm, width, height, true);
            return bm;
        }
        return null;
    }

    // Google Places API used to find a location and store it in the location text field
    public void findPlace(View view) {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
        } catch (GooglePlayServicesRepairableException
                | GooglePlayServicesNotAvailableException e) {
            Log.d(TAG, "PlacesError");
        }
    }

    // Gets the first part of a user's email; ignores everything starting at '@'
    private String getUserName(String email) {
        String returnValue = "";
        for (char ch : email.toCharArray()) {
            if (ch == '@') {
                return returnValue;
            } else {
                returnValue = returnValue + ch;
            }
        }
        return returnValue;
    }

}

