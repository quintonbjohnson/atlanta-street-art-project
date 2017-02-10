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
import android.widget.ImageView;
import android.widget.Toast;
import android.content.Intent;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

import comquintonj.github.atlantastreetartproject.R;

public class SubmitActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private EditText titleText, addressText, descriptionText, tagText, artistText;
    private Button submitButton, buttonChoose;
    private FirebaseAuth firebaseAuth;

    //a constant to track the file chooser intent
    private static final int PICK_IMAGE_REQUEST = 234;

    StorageReference storageReference;

    //ImageView
    private ImageView imageView;

    //a Uri object to store file path
    private Uri filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        titleText = (EditText) findViewById(R.id.titleText);
        addressText = (EditText) findViewById(R.id.addressText);
        descriptionText = (EditText) findViewById(R.id.descriptionText);
        tagText = (EditText) findViewById(R.id.tagText);
        artistText = (EditText) findViewById(R.id.artistTag);
        submitButton = (Button) findViewById(R.id.submitButton);
        buttonChoose = (Button) findViewById(R.id.chooseButton);
        storageReference = FirebaseStorage.getInstance().getReference();
        imageView = (ImageView) findViewById(R.id.imageView);

        submitButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if(v == submitButton){
                                                    submitArtInformation();
                                                    uploadFile();
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
        //Getting values from database
        String name = titleText.getText().toString().trim();
        String add = addressText.getText().toString().trim();
        String artist = artistText.getText().toString().trim();
        String tag = tagText.getText().toString().trim();
        String description = descriptionText.getText().toString().trim();

        //creating a userinformation object
        ArtInformation userInformation = new ArtInformation(name, add, artist, description, tag );

        //getting the current logged in user
        //FirebaseUser user = firebaseAuth.getCurrentUser();

        //saving data to firebase database
        databaseReference.child(userInformation.title).setValue(userInformation);

        Toast.makeText(this, "Information Saved...", Toast.LENGTH_LONG).show();
    }
    //method to show file chooser
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
                imageView.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //this method will upload the file ma
    public void uploadFile() {
        //if there is a file to upload
        if (filePath != null) {
            //displaying a progress dialog while upload is going on
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            StorageReference riversRef = storageReference.child("images/pic.jpg");//will add "+ titleText.toString());" once I can test
            riversRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload is successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying a success toast
                            Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                        }
                    })
                    /**.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                    //if the upload is not successfull
                    //hiding the progress dialog
                    progressDialog.dismiss();

                    //and displaying error message
                    Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    })*/
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //calculating progress percentage
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            //displaying percentage in progress dialog
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });
        }
        //if there is not any file
        else {
            //you can display an error toast
        }
    }
}
