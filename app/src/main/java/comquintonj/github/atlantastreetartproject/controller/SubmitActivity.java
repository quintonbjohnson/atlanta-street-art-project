package comquintonj.github.atlantastreetartproject.controller;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import comquintonj.github.atlantastreetartproject.R;
import comquintonj.github.atlantastreetartproject.model.ArtInformation;
import comquintonj.github.atlantastreetartproject.model.GPSUtility;

/**
 * TODO: Store time of submission in database
 * TODO: Add dialog for camera or image picker
 * Submission page used to submit pieces of art
 */
public class SubmitActivity extends BaseDrawerActivity {

    /**
     *
     */
    private ArtInformation pieceOfArt;

    /**
     * The submit Button
     */
    private Button submitButton;

    /**
     * The context of the current screen
     */
    private Context context;

    /**
     * A reference to the Firebase database to store information about the art
     */
    private DatabaseReference mDatabase;

    /**
     * EditText for the title field
     */
    private EditText titleText;

    /**
     * EditText for the artist field
     */
    private EditText artistText;

    /**
     * EditText for the location field
     */
    private EditText locationText;

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
     * A constant to track the autocomplete intent
     */
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    /**
     * A constant to track the file chooser intent
     */
    private static final int PICK_IMAGE_REQUEST = 234;

    /**
     * The latitude of the art
     */
    private double latitude;

    /**
     * The longitude of the art
     */
    private double longitude;

    /**
     * A reference to the Firebase storage kept in order to upload images
     */
    private StorageReference storageReference;

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
        context = this;

        // Get Instance of Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Create the navigation drawer
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        createNavigationDrawer();
        navigationView.getMenu().getItem(3).setChecked(true);

        // Instantiate resources
        titleText = (EditText) findViewById(R.id.titleText);
        artistText = (EditText) findViewById(R.id.artistTag);
        locationText = (EditText) findViewById(R.id.locationText);
        submitButton = (Button) findViewById(R.id.submitButton);
        imageSelectButton = (ImageButton) findViewById(R.id.art_image_view);
        storageReference = FirebaseStorage.getInstance().getReference();

        // Prevent view from being editable until the user has selected an image
        titleText.setFocusable(false);
        artistText.setFocusable(false);
        locationText.setFocusable(false);

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
                boolean allowed = checkReadPermission();
                if (allowed) {
                    if (v == imageSelectButton) {
                        showFileChooser();
                    }
                }
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
        if (user != null) {
            photoPath = titleText.getText().toString().trim() + user.getDisplayName();
            displayName = user.getDisplayName();
        }
        int upvotes = 0;
        int downvotes = 0;

        pieceOfArt = new ArtInformation(artist, displayName,
                latitude, longitude, photoPath, upvotes, downvotes, title);

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
        startActivityForResult(Intent.createChooser(intent,
                "Complete action using"), PICK_IMAGE_REQUEST);
    }

    /**
     * On the result of the activity for the image picker
     * and for the image selection screen.
     *
     * @param requestCode The request code used to decide which screen the user has completed.
     * @param resultCode  The result code to decide if there has been an error.
     * @param data        Data to include for the result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If user has to select a place from Google Places
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Get the place the user has selected
                Place place = PlaceAutocomplete.getPlace(this, data);
                locationText.setText(place.getName());

                // Get the coordinates from the place
                LatLng latLng = place.getLatLng();
                latitude = latLng.latitude;
                longitude = latLng.longitude;

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }

        // If user is trying to select an image
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK && data != null && data.getData() != null) {

            // Get the Uri for the image
            filePath = data.getData();

            // Instantiate variables to be used
            InputStream inputStreamBitmap = null;
            Bitmap imageBitmap = null;
            File finalFile = null;

            // Retrieve the file from the actual file path of the image
            try {
                finalFile = new File(getRealPathFromURI(filePath));
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            // Read data from file
            boolean success = false;
            if (finalFile != null) {
                // Use the file to read location data
                success = readExif(finalFile.toString());
            }

            // Show bitmap in view
            if (!success) {
                locationText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        findPlace(view);
                    }
                });
            }
            try {
                inputStreamBitmap = getContentResolver().openInputStream(filePath);
                imageBitmap = BitmapFactory.decodeStream(inputStreamBitmap);
                // Resize the image to fit to the view
                Bitmap resizedImage = resizeImage(imageBitmap);
                if (resizedImage != null) {
                    // Set the ImageButton to be the photo the user has selected
                    imageSelectButton.setImageBitmap(resizedImage);

                    // Allow the view to be editable
                    titleText.setFocusableInTouchMode(true);
                    artistText.setFocusableInTouchMode(true);
                } else {
                    Toast.makeText(this, "Please try again with a different image",
                            Toast.LENGTH_SHORT).show();
                }
            } catch (FileNotFoundException e) {
                Toast.makeText(context, "File could not be found", Toast.LENGTH_SHORT).show();
            } finally {
                try {
                    if (inputStreamBitmap != null) {
                        inputStreamBitmap.close();
                    }
                } catch (IOException ignored) {
                }

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
                            // If the upload is successful, hide the progress dialog
                            progressDialog.dismiss();

                            // Take the user to the art page for the piece of art that was uploaded
                            Intent artIntent = new Intent(context, ArtPageActivity.class);
                            artIntent.putExtra("ArtPath", pieceOfArt.getPhotoPath());
                            startActivity(artIntent);
                        }
                    })

                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            // Calculate the upload progress
                            @SuppressWarnings("VisibleForTests")
                            double progress = 100.0 * taskSnapshot.getBytesTransferred() /
                                    taskSnapshot.getTotalByteCount();

                            // Display the progress
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
     *
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
        return valid;
    }

    /**
     * Scrape coordinates from the image that the user chooses
     * @param file the file to get the coordinates from
     * @return whether or not the method was able to succeed
     */
    private boolean readExif(String file) {
        try {
            // Retrieve Exif information from the Bitmap
            ExifInterface exifInterface = new ExifInterface(file);

            // Create a new GPSUtility
            GPSUtility gpsUtil = new GPSUtility(exifInterface);
            if (gpsUtil.toString().equals("null, null")) {
                return false;
            } else {
                // Transcribe the longitude and latitude to a String for submission
                latitude = gpsUtil.getLatitudeE6();
                longitude = gpsUtil.getLongitudeE6();
                locationText.setOnClickListener(null);
                String setString = "Location saved from photo";
                locationText.setText(setString);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(SubmitActivity.this,
                    e.toString(),
                    Toast.LENGTH_LONG).show();
        }
        return false;
    }

    /**
     * Retrieve actual file path from a Uri object.
     * @param uri the Uri input
     * @return the actual file path
     */
    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        } else {
            return null;
        }
    }

    /**
     * Google Places API autocomplete activity that allows users to select a location
     * @param view the view that the activity takes place in
     */
    public void findPlace(View view) {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException |
                GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}

