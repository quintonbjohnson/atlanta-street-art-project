package comquintonj.github.atlantastreetartproject.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import comquintonj.github.atlantastreetartproject.R;
import comquintonj.github.atlantastreetartproject.model.User;

public class RegistrationActivity extends AppCompatActivity {

    // Instance variables
    private static final String TAG = "MyActivity";
    private EditText usernameInfo;
    private EditText emailInfo;
    private EditText passwordInfo;
    private EditText confirmInfo;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Intent introIntent;
    private Intent loginIntent;
    private FirebaseUser user;
    private DatabaseReference mDatabase;
    private Toolbar appbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Create layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        setTitle("Registration");
        appbar = (Toolbar) findViewById(R.id.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Instantiate resources
        emailInfo = (EditText) findViewById(R.id.emailInput);
        passwordInfo = (EditText) findViewById(R.id.passwordInput);
        confirmInfo = (EditText) findViewById(R.id.confirmInput);
        usernameInfo = (EditText) findViewById(R.id.usernameInput);

        // Listener to check when the user signs in. If the user signs in, the user object will
        // not be null.
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    // The user has successfully registered and the email verification can be sent
                    user.sendEmailVerification()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Email sent.");
                                    }
                                }
                            });
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        // Set up intent to go to login screen upon successful registration
        introIntent = new Intent(this, IntroActivity.class);
        loginIntent = new Intent(this, LoginActivity.class);


        // Set up registration button to add User with the edited text fields
        Button registerButton = (Button) findViewById(R.id.registrationButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernameText = usernameInfo.getText().toString();
                String emailText = emailInfo.getText().toString();
                String passwordText = confirmInfo.getText().toString();
                String confirmText = passwordInfo.getText().toString();
                if (!passwordText.equals(confirmText)) {
                    Toast.makeText(RegistrationActivity.this, "Passwords do not match",
                            Toast.LENGTH_SHORT).show();
                } else {
                    checkDisplayName(emailText, confirmText, usernameText);
                }
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in

                    // Update display name of the user that just registered to match username text
                    String displayName = usernameInfo.getText().toString();
                    UserProfileChangeRequest profileUpdates =
                            new UserProfileChangeRequest.Builder()
                                    .setDisplayName(displayName)
                                    .build();

                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User profile updated.");
                                        mAuth.signOut();
                                        startActivity(loginIntent);
                                    }
                                }
                            });

                    Toast.makeText(RegistrationActivity.this, "Account created",
                            Toast.LENGTH_SHORT).show();

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };


    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    // Called when a user presses the "register" button. Creates an account
    // using Firebase authorization.
    private void checkDisplayName(final String email,
                                  final String password, final String displayName) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }
        // Check if display name is taken
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.child("Users").hasChild(displayName)) {
                    Toast.makeText(RegistrationActivity.this,
                            "Username is already in use",
                            Toast.LENGTH_SHORT).show();
                } else {
                    createAccount(email, password, displayName);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    // Taken from Firebase support
    private boolean validateForm() {
        boolean valid = true;

        String username = usernameInfo.getText().toString();
        if (TextUtils.isEmpty(username)) {
            usernameInfo.setError("Required.");
            valid = false;
        } else {
            emailInfo.setError(null);
        }

        String email = emailInfo.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailInfo.setError("Required.");
            valid = false;
        } else {
            emailInfo.setError(null);
        }

        String password = passwordInfo.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordInfo.setError("Required.");
            valid = false;
        } else {
            passwordInfo.setError(null);
        }

        return valid;
    }

    // Create an account with the given email and password
    private void createAccount(String email, String password, String displayName) {
        // Create a user object with the given email and password
        final User newUser = new User(displayName, email);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(RegistrationActivity.this, "Please check fields",
                                    Toast.LENGTH_SHORT).show();
                        } else {

                            // Saving data to Firebase database
                            mDatabase.child("Users").child(newUser.getProfileName())
                                    .setValue(newUser.getProfileName());
                            mDatabase.child("Users").child(newUser.getProfileName())
                                    .child("Email").setValue(newUser.getEmail());
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                introIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(introIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
