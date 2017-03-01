package comquintonj.github.atlantastreetartproject.controller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
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

import comquintonj.github.atlantastreetartproject.R;
import comquintonj.github.atlantastreetartproject.model.ArtInformation;

/**
 * Submission page used to submit pieces of art
 */
public class SubmitActivity extends BaseDrawerActivity {

    /**
     * The submit Button
     */
    private Button submitButton;

    /**
     * A reference to the Firebase database to store information about the art
     */
    private DatabaseReference mDatabase;

    /**
     * EditText for the title field
     */
    private EditText titleText;

    /**
     * EditText for the location field
     */
    private EditText locationText;

    /**
     * EditText for the artist field
     */
    private EditText artistText;

    /**
     * Authentication instance of the FirebaseAuth
     */
    private FirebaseAuth mAuth;

    /**
     * The current user
     */
    private FirebaseUser user;

    /**
     * The ImageButton that takes the user to the image picker
     */
    private ImageButton imageSelectButton;

    /**
     * A constant to track the file chooser intent
     */
    private static final int PICK_IMAGE_REQUEST = 234;

    /**
     * Request code to decide where to take the user upon an action
     */
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;

    /**
     * A reference to the Firebase storage kept in order to upload images
     */
    private StorageReference storageReference;

    /**
     * The ID of the place the user selects as the location
     */
    private String placeId;

    /**
     * TAG used for error messages
     */
    private static final String TAG = "MyActivity";

    /**
     * A Uri object to store the file path of the image
     */
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

        // Create the navigation drawer
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        createNavigationDrawer();
        navigationView.getMenu().getItem(3).setChecked(true);

        // Instantiate resources
        titleText = (EditText) findViewById(R.id.titleText);
        locationText = (EditText) findViewById(R.id.locationTextView);
        artistText = (EditText) findViewById(R.id.artistTag);
        submitButton = (Button) findViewById(R.id.submitButton);
        imageSelectButton = (ImageButton) findViewById(R.id.art_image_view);
        storageReference = FirebaseStorage.getInstance().getReference();

        // Set on click listener for the submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == submitButton) {
                    if (!validateForm()) {
                        return;
                    }
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

        // When a user enters location take them to them to the Autocomplete Activity
        locationText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(MotionEvent.ACTION_UP == event.getAction()) {
                    findPlace();
                }

                return true; // return is important...
            }
        });
    }

    /**
     * Called when a user decides to submit the art.
     */
    private void submitArtInformation() {
        // Get current user
        user = mAuth.getCurrentUser();

        // Getting values from database
        String photoPath = "";
        String displayName = "";
        final String title = titleText.getText().toString().trim();
        String artist = artistText.getText().toString().trim();
        String location = placeId;
        if (user != null) {
            photoPath = titleText.getText().toString().trim() + user.getDisplayName();
            displayName = user.getDisplayName();
        }
        String upvotes = String.valueOf(0);
        String downvotes = String.valueOf(0);

        ArtInformation pieceOfArt = new ArtInformation(artist, displayName,
                location, photoPath, upvotes, downvotes, title);

        // Saving data to Firebase database
        mDatabase.child("Art").child(photoPath).setValue(pieceOfArt);
    }

    /**
     * Shows the file chooser to select an image from the user's device.
     */
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    /**
     * On the result of the activity for the Google Places API location chooser
     * and for the image selection screen.
     * @param requestCode The request code used to decide which screen the user has completed.
     * @param resultCode The result code to decide if there has been an error.
     * @param data Data to include for the result.
     */
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
                Log.i(TAG, "User left intent");
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
                } else {
                    Toast.makeText(this, "Please try again with a different image.",
                            Toast.LENGTH_SHORT).show();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Uploads the file to Firebase storage
     */
    private void uploadFile() {
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
                            Toast.makeText(getApplicationContext(), "Art Uploaded",
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

    /**
     * Resize the bitmap in order to constrain the image to the image view.
     * Credits: http://stackoverflow.com/
     * questions/15124179/resizing-a-bitmap-to-a-fixed-value-but-without-changing-the-aspect-ratio
     * @param bm the bitmap to resize
     * @return the resized bitmap image
     */
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

    /**
     * The Autocomplete intent from the Google Places API to choose the location of the art
     */
    private void findPlace() {
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

    /**
     * Validates the form to ensure that no fields are left empty
     * @return true if the form is ready to be submitted, false otherwise
     */
    private boolean validateForm() {
        boolean valid = true;

        String title = titleText.getText().toString();
        if (TextUtils.isEmpty(title)) {
            titleText.setError("Required.");
            valid = false;
        } else {
            titleText.setError(null);
        }

        String artist = artistText.getText().toString();
        if (TextUtils.isEmpty(artist)) {
            artistText.setError("Required.");
            valid = false;
        } else {
            artistText.setError(null);
        }

        String location = locationText.getText().toString();
        if (TextUtils.isEmpty(location)) {
            locationText.setError("Required.");
            valid = false;
        } else {
            locationText.setError(null);
        }

        return valid;
    }
}

