package com.example.medicompass;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomepageActivity extends AppCompatActivity {

    private GoogleSignInClient googleSignInClient;
    private TextView welcomeText;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage_main); // Ensure the layout file name matches

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize GoogleSignInOptions
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id)) // Add your web client ID here
                .requestEmail()
                .build();

        // Initialize GoogleSignInClient with GoogleSignInOptions
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        // Initialize TextView
        welcomeText = findViewById(R.id.welcomeText);

        // Get Username and Display
        if (firebaseUser != null) {
            String username = firebaseUser.getDisplayName();
            if (username == null || username.isEmpty()) {
                username = "User"; // Default if username is not set
            }
            welcomeText.setText("Welcome, " + username + "!");
        } else {
            welcomeText.setText("Welcome, Guest!");
        }

        // Handle Locate Me Button
        Button locateMeButton = findViewById(R.id.locateMeButton);
        locateMeButton.setOnClickListener(view -> {
            Toast.makeText(HomepageActivity.this, "Locate Me clicked", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(HomepageActivity.this, locateMe.class);
            startActivity(intent);
        });

        // Handle Find Nearby Hospital Button
        Button findHospitalButton = findViewById(R.id.findHospitalButton);
        findHospitalButton.setOnClickListener(view -> {
            Toast.makeText(HomepageActivity.this, "Find Nearby Hospital clicked", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(HomepageActivity.this, MainActivity.class);
            startActivity(intent);

        });

        // Handle User Profile Button
        Button userProfileButton = findViewById(R.id.userProfileButton);
        userProfileButton.setOnClickListener(view -> {
            Toast.makeText(HomepageActivity.this, "User Profile clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomepageActivity.this, userprofile.class);
            startActivity(intent);
            // Add your logic here
        });

        // Handle Sign Out Button (MaterialButton)
        Button signOutButton = findViewById(R.id.signOutButton); // Assuming you have a MaterialButton in your layout
        signOutButton.setOnClickListener(view -> {
            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut();

            // Sign out from Google
            googleSignInClient.signOut().addOnSuccessListener(unused -> {
                Toast.makeText(HomepageActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(HomepageActivity.this, signin.class));
                finish(); // Close HomepageActivity
            });
        });
    }

    // Inflate the menu for the Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Handle menu item clicks
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_about_us) {
            // Navigate to About Us page
            Intent intent = new Intent(this, com.example.medicompass.AboutActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

