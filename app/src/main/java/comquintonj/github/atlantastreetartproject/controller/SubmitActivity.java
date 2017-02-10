package comquintonj.github.atlantastreetartproject.controller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import comquintonj.github.atlantastreetartproject.R;

public class SubmitActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private EditText titleText, addressText, descriptionText, tagText, artistText;
    private Button submitButton;
    private FirebaseAuth firebaseAuth;



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

        submitButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if(v == submitButton){
                                                    submitArtInformation();
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
}
