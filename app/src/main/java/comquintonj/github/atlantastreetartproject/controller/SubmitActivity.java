package comquintonj.github.atlantastreetartproject.controller;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.content.Intent;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.IOException;
import comquintonj.github.atlantastreetartproject.R;

public class SubmitActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private EditText titleText, addressText, tagText, artistText;
    private Button submitButton;
    private ImageButton buttonChoose;
    //private FirebaseAuth firebaseAuth; might use this later

    //a constant to track the file chooser intent
    private static final int PICK_IMAGE_REQUEST = 234;

    StorageReference storageReference;

    //a Uri object to store file path
    private Uri filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        titleText = (EditText) findViewById(R.id.titleText);
        addressText = (EditText) findViewById(R.id.locationText);
        tagText = (EditText) findViewById(R.id.tagText);
        artistText = (EditText) findViewById(R.id.artistTag);
        submitButton = (Button) findViewById(R.id.submitButton);
        buttonChoose = (ImageButton) findViewById(R.id.chooseButton);
        storageReference = FirebaseStorage.getInstance().getReference();

        submitButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if(v == submitButton){
                                                    submitArtInformation();
                                                }
                                            }
                                        }
        );

        buttonChoose.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if(v == buttonChoose){
                                                    showFileChooser();
                                                }
                                            }
                                        }
        );

    }

    private void submitArtInformation() {
        String name = titleText.getText().toString().trim();
        String add = addressText.getText().toString().trim();
        String artist = artistText.getText().toString().trim();
        String tag = tagText.getText().toString().trim();

        final ArtInformation userInformation = new ArtInformation(name, add, artist, tag );

        //Current logged in user
        //FirebaseUser user = firebaseAuth.getCurrentUser();

        //saving data to firebase database
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild(userInformation.title)) {
                    // run some code
                    Toast.makeText(getApplicationContext(), "Title already exists. Please edit title.", Toast.LENGTH_LONG).show();
                }else{
                    databaseReference.child(userInformation.title).setValue(userInformation);
                    Toast.makeText(getApplicationContext(), "Art information saved.", Toast.LENGTH_LONG).show();
                    uploadFile();
                    Intent myIntent = new Intent(SubmitActivity.this,
                            DiscoverActivity.class);
                    startActivity(myIntent);
                }
            }
            @Override
            public void onCancelled(DatabaseError err){
                Toast.makeText(getApplicationContext(), err.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    public void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    //handling the image chooser activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                buttonChoose.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void uploadFile() {
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            StorageReference riversRef = storageReference.child("image/" + titleText.getText().toString().trim());
            riversRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //Upload successful. Hide dialog
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Art information submitted. Thank you. ", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //Progress calculation and display
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });
        }
        else {
            Toast.makeText(getApplicationContext(), "File not selected.", Toast.LENGTH_LONG).show();
        }
    }
}
