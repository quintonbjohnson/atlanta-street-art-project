package comquintonj.github.atlantastreetartproject;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class SubmitActivity extends BaseDrawerActivity {

    private DatabaseReference databaseReference;
    private EditText titleText, addressText, descriptionText, tagText, artistText;
    private Button submitButton, buttonChoose;
    private FirebaseAuth mAuth;

    // A constant to track the file chooser intent
    private static final int PICK_IMAGE_REQUEST = 234;

    StorageReference storageReference;

    // ImageView
    private ImageView imageView;

    // A Uri object to store file path
    private Uri filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);
        setTitle("Explore");
        mAuth = FirebaseAuth.getInstance();

        // Set up Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        FirebaseUser user = mAuth.getCurrentUser();
        TextView headerName = (TextView)header.findViewById(R.id.profileNameText);
        assert user != null;
        headerName.setText(user.getEmail());

        // Instantiate resources
        titleText = (EditText) findViewById(R.id.titleText);
        addressText = (EditText) findViewById(R.id.addressText);
        descriptionText = (EditText) findViewById(R.id.descriptionText);
        tagText = (EditText) findViewById(R.id.tagText);
        artistText = (EditText) findViewById(R.id.artistTag);
        submitButton = (Button) findViewById(R.id.submitButton);
        buttonChoose = (Button) findViewById(R.id.chooseButton);
        storageReference = FirebaseStorage.getInstance().getReference();

        // Set on click listener for the submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v == submitButton){
                    submitArtInformation();
                    uploadFile();
                }
            }
        });

        // Set on click listener for choosing the image button
        buttonChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v == buttonChoose){
                    showFileChooser();
                }
            }
        });
    }

    private void submitArtInformation() {
        // Getting values from database
        String name = titleText.getText().toString().trim();
        String add = addressText.getText().toString().trim();
        String artist = artistText.getText().toString().trim();
        String tag = tagText.getText().toString().trim();
        String description = descriptionText.getText().toString().trim();

        // Get current user
        FirebaseUser user = mAuth.getCurrentUser();

        // Check if address field is empty
        if (add.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter a valid address",
                    Toast.LENGTH_LONG).show();
        } else {
            // Creating an ArtInformation object
            ArtInformation artSubmission = new ArtInformation(name, add, artist,
                    description, tag, user);

            // Saving data to Firebase database
            databaseReference.child(artSubmission.title).setValue(artSubmission);

            // Success
            Toast.makeText(this, "Information Saved...", Toast.LENGTH_LONG).show();
        }
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
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                // TODO Change UI to where it shows the image after being uploaded
//                imageView.setImageBitmap(bitmap);
                Toast.makeText(this, "Set", Toast.LENGTH_SHORT).show();

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

            StorageReference riversRef = storageReference.child("images/pic.jpg");
            riversRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload is successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying a success toast
                            Toast.makeText(getApplicationContext(), "File Uploaded ",
                                    Toast.LENGTH_LONG).show();
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
}
