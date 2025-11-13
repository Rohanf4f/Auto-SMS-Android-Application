package com.whozzjp.smsapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import io.grpc.internal.LogExceptionRunnable;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText email, pass;
    MaterialButton signBtn;
    private FirebaseAuth auth;
    MaterialTextView forget_password,nothaveaccount;
    CheckInternet check = new CheckInternet();
    private String mydeviceId;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        email = findViewById(R.id.user_email);
        pass = findViewById(R.id.user_pass);
        signBtn = findViewById(R.id.signin);
        nothaveaccount=findViewById(R.id.signup_activity);


        mydeviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initialize FirebaseApp
        FirebaseApp.initializeApp(this);

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance();

        //Check Permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {

            } else {
                requestPermissions(new String[]{android.Manifest.permission.SEND_SMS, android.Manifest.permission.READ_CALL_LOG, android.Manifest.permission.READ_PHONE_STATE, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS}, 1);
            }
        }
        //Forget password
        forget_password = findViewById(R.id.frg_password);
        forget_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class));
            }
        });
        nothaveaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));

            }
        });

        signBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailET = email.getText().toString().trim();
                String passET = pass.getText().toString().trim();

                if (email.getText().toString().isEmpty()) {
                    email.setError("Enter email");
                } else if (pass.getText().toString().isEmpty()) {
                    pass.setError("Enter password");
                } else if (!email.getText().toString().isEmpty() && !pass.getText().toString().isEmpty()) {
                    String txt_email = email.getText().toString();
                    String txt_pass = pass.getText().toString();
                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference = firebaseDatabase.getReference().child("admin");

                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            String emaildb = snapshot.child("email").getValue(String.class);

                            String passworddb = snapshot.child("password").getValue(String.class);

                            if (emailET.equals(emaildb) && passworddb.equals(passET)) {

                                // Set the login flag
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putBoolean("loggedIn", true);
                                editor.apply();

                                startActivity(new Intent(LoginActivity.this, AdminActivity.class));

                                finish();
                                Toast.makeText(LoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    // Check the login flag when the app is reopened
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                    boolean loggedIn = preferences.getBoolean("loggedIn", false);
                    if (loggedIn) {

                        startActivity(new Intent(getApplicationContext(), AdminActivity.class));
                        finish();
                    }

                    //firebase login method
                    loginuser(txt_email, txt_pass);
                } else {
                    Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    //Firebase Login
    private void loginuser(String email, String password) {

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();

                    String DeviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                    try{
                    if (user != null) {
                        String id=task.getResult().getUser().getUid();
                        DatabaseReference root = FirebaseDatabase.getInstance().getReference().child("Users").child(id);

                        root.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    String databaseDeviceID=snapshot.child("deviceID").getValue(String.class);
                                    if(DeviceID.equals(databaseDeviceID)){
                                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                                        // Set the login flag
                                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                                        SharedPreferences.Editor editor = preferences.edit();
                                        editor.putBoolean("loggedInDevice", true);
                                        editor.apply();

                                        // Proceed to MainActivity
                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    }
                                    else{
                                        Toast.makeText(LoginActivity.this, "Access Denied!!!!!!!!!", Toast.LENGTH_SHORT).show();
                                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                        builder.setTitle("Access Denied!!!.");
                                        builder.setMessage("Please don't try to login.\n Your account is already logged in.");
                                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // Do something when the "OK" button is clicked
                                                //startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
                                                dialog.dismiss();
                                                finish();
                                            }
                                        });
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }}catch (Exception e){
                        Log.d("Firebase",e.getMessage());
                    }

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle("Account doesn't Exist.");
                    builder.setMessage("Please signup yourself!!!");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do something when the "OK" button is clicked
                            startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }

        });

    }

    @Override
    protected void onStart() {

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(check, filter);
        super.onStart();
        // Check the login flag when the app is reopened
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        boolean loggedInDevice = preferences.getBoolean("loggedInDevice", false);
        boolean isLoggedOut = preferences.getBoolean("loggedOUTDevice", false);
        if (loggedInDevice) {

            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
        if (isLoggedOut) {
            // The user has logged out on another device, show a message
            Toast.makeText(LoginActivity.this, "You have been logged out on another device", Toast.LENGTH_SHORT).show();

        }
    }


    @Override
    public void onBackPressed() {
        // Create an alert dialog to ask the user if they want to exit
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm");
        builder.setMessage("Are you sure you want to exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("No", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onStop() {

        unregisterReceiver(check);
        super.onStop();
        //  FirebaseAuth.getInstance().signOut();
    }
}