package comquintonj.github.atlantastreetartproject.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

import java.util.HashMap;

import comquintonj.github.atlantastreetartproject.R;
import comquintonj.github.atlantastreetartproject.model.User;

public class RegistrationActivity extends AppCompatActivity {

    /**
     * A reference to the Firebase database to store information about the user
     */
    private DatabaseReference mDatabase;

    /**
     * EditText for the confirm password field
     */
    private EditText confirmInfo;

    /**
     * EditText for the email field
     */
    private EditText emailInfo;

    /**
     * EditText for the password field
     */
    private EditText passwordInfo;

    /**
     * EditText for the username field
     */
    private EditText usernameInfo;

    /**
     * Authentication instance of the FirebaseAuth
     */
    private FirebaseAuth mAuth;

    /**
     * AuthStateListener for Firebase to determine if a user is already signed in
     */
    private FirebaseAuth.AuthStateListener mAuthListener;

    /**
     * Intent to go to the intro activity
     */
    private Intent introIntent;

    /**
     * Intent to go to the login activity
     */
    private Intent loginIntent;

    /**
     * Intent to go to the explore activity
     */
    private Intent exploreIntent;

    /**
     * TAG used for error messages
     */
    private static final String TAG = "MyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Create layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        setTitle("Registration");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
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
        exploreIntent = new Intent(this, ExploreActivity.class);


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

        // Once a user has registered, input their profile display name into the Firebase database
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in

                    // Update display name of the user that just registered to match username text
                    String displayName = usernameInfo.getText().toString();
                    if (displayName.equals("")) {
                        startActivity(exploreIntent);
                        finish();
                    } else {
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
                                            finish();
                                        }
                                    }
                                });

                        Toast.makeText(RegistrationActivity.this, "Account created",
                                Toast.LENGTH_SHORT).show();
                    }

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        // Allow text view for guest to send user to explore screen
        TextView guestView = (TextView) findViewById(R.id.guest_text_view);
        guestView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInAnonymously();
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    startActivity(exploreIntent);
                    finish();
                }
            }
        });
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Validates the form to ensure that no fields are left empty
     * @return true if the form is ready to be submitted, false otherwise
     */
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

    /**
     * Creates an account with the given information.
     * @param email the email the user entered
     * @param password the password the user entered
     * @param displayName the display name the user entered
     */
    private void createAccount(String email, String password, String displayName) {
        // Create a user object with the given email and password
        final User newUser = new User(displayName, email, new HashMap<String, String>());
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
                                    .child("email").setValue(newUser.getEmail());
                            HashMap<String, String> artRated = new HashMap<String, String>();
                            artRated.put("First", "First");
                            mDatabase.child("Users").child(newUser.getProfileName())
                                    .child("rated").setValue(artRated);
                        }
                    }
                });
    }

    /**
     * If a user decided to login anonymously, sign them in with an anonymous account
     */
    private void signInAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInAnonymously", task.getException());
                            Toast.makeText(RegistrationActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Ensures that the display name is not already in use.
     * @param email the email of the user
     * @param password the password of the user
     * @param displayName the display name of the user
     */
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
}
