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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import comquintonj.github.atlantastreetartproject.R;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final String TAG = "MyActivity";
    private Intent exploreIntent;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private EditText emailText;
    private EditText passwordText;
    private Intent introIntent;
    private Toolbar appbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Create layout
        setContentView(R.layout.activity_login);
        super.onCreate(savedInstanceState);
        setTitle("Login");
        appbar = (Toolbar) findViewById(R.id.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        exploreIntent = new Intent(this, ExploreActivity.class);
        introIntent = new Intent(this, IntroActivity.class);

        // Instantiate resources
        emailText = (EditText) findViewById(R.id.loginUsername);
        passwordText = (EditText) findViewById(R.id.loginPassword);

        // Listener to check when the user signs in. If the user signs in, the user object will
        // not be null, and they can be taken to the explore page.
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    startActivity(exploreIntent);
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        // Login Button
        Button loginBut = (Button) findViewById(R.id.loginButton);
        loginBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(emailText.getText().toString(), passwordText.getText().toString());
            }
        });

        // Allow text view for guest to send user to explore screen
        TextView guestView = (TextView) findViewById(R.id.guestTextView);
        final Intent guestIntent = new Intent(this, ExploreActivity.class);
        guestView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInAnonymously();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    startActivity(exploreIntent);
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

    // Sign in with a regular email and password
    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Continue as a guest and use an anonymous account
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
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Taken from Firebase Support
    private boolean validateForm() {
        boolean valid = true;

        String email = emailText.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailText.setError("Required.");
            valid = false;
        } else {
            emailText.setError(null);
        }

        String password = passwordText.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordText.setError("Required.");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
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



