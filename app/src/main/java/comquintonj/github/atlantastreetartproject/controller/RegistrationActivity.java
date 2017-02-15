package comquintonj.github.atlantastreetartproject.controller;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import comquintonj.github.atlantastreetartproject.R;

public class RegistrationActivity extends AppCompatActivity {

    // Instance variables
    private static final String TAG = "MyActivity";
    private EditText emailInfo;
    private EditText passwordInfo;
    private EditText confirmInfo;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Intent loginIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialization
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        mAuth = FirebaseAuth.getInstance();

        // Find views by ID for the text fields
        emailInfo = (EditText) findViewById(R.id.emailInput);
        passwordInfo = (EditText ) findViewById(R.id.passwordInput);
        confirmInfo = (EditText ) findViewById(R.id.confirmInput);

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
        loginIntent = new Intent(this,LoginActivity.class);

        // Set up registration button to add User with the edited text fields
        Button registerButton = (Button) findViewById(R.id.registrationButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailText = emailInfo.getText().toString();
                String passwordText = confirmInfo.getText().toString();
                String confirmText = passwordInfo.getText().toString();
                if (!passwordText.equals(confirmText)) {
                    Toast.makeText(RegistrationActivity.this, "Passwords do not match.",
                            Toast.LENGTH_SHORT).show();
                }
                createAccount(emailText, confirmText);
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

    // Called when a user presses the "register" button. Creates an account
    // using Firebase authorization.
    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(RegistrationActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        startActivity(loginIntent);
                    }
                });

    }

    // Taken from Firebase support
    private boolean validateForm() {
        boolean valid = true;

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
}
