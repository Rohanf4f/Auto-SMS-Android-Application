package com.whozzjp.smsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String MESSAGE_KEY = "message";
    private SwitchMaterial switch_BT;
    private EditText messageEditText;
    TextView tv;
    Button subscribe,logout,block,set_BT;
    private FirebaseAuth auth;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv=findViewById(R.id.Text);
        subscribe=findViewById(R.id.subscribe_BT);
        logout=findViewById(R.id.logout_BT);
        block=findViewById(R.id.block_BT);
        set_BT=findViewById(R.id.setbtn_IN);
        switch_BT=findViewById(R.id.my_switchin);
        messageEditText=findViewById(R.id.entermsgET_IN);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedMessage = preferences.getString(MESSAGE_KEY, "");
        messageEditText.setText(savedMessage);

        try {

            auth = FirebaseAuth.getInstance(); // initialize auth variable here
            if (auth.getCurrentUser() == null) {
                // user not logged in, navigate to login screen
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                String id = auth.getCurrentUser().getUid();
                DatabaseReference root = FirebaseDatabase.getInstance().getReference().child("Users").child(id);

                subscribe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        root.child("Subscribed").setValue(true);
                        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor1 = sharedPreferences.edit();
                        editor1.putBoolean("Subscribed", true);
                        editor1.apply();
                        tv.setText("SUBSCRIBED");
                    }
                });

                logout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Sign out the user from Firebase authentication
                        auth.signOut();

                        // Set the login flag
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("loggedOUTDevice", true);
                        editor.putBoolean("loggedInDevice", false);
                        editor.apply();
                        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor1 = sharedPreferences.edit();
                        editor1.clear();
                        editor1.apply();


                        // Stop the foreground service to stop the CallReceiver
                        Intent serviceIntent = new Intent(getApplicationContext(), CallReceiverForegroundService.class);
                        stopService(serviceIntent);

                        // Navigate back to the login screen
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }
            }catch(Exception e){
            Log.d("Exception", e.getMessage());
            }
        block.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),Blocksms.class));
            }
        });

        set_BT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switch_BT.isChecked()) {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
                    } else {
                        // Start the foreground service to keep the CallReceiver running
                        Intent serviceIntent = new Intent(getApplicationContext(), CallReceiverForegroundService.class);
                        ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
                        String message = messageEditText.getText().toString();
                        saveMessage(message);

                    }
                } else {
                    // Stop the foreground service to stop the CallReceiver
                    Intent serviceIntent = new Intent(getApplicationContext(), CallReceiverForegroundService.class);
                    stopService(serviceIntent);
                }

            }

        });

    }
    private void saveMessage(String message) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(MESSAGE_KEY, message);
        editor.apply();
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startService(new Intent(getApplicationContext(), CallReceiverForegroundService.class));
                String message = messageEditText.getText().toString();
                saveMessage(message);
            } else {
                switch_BT.setChecked(false);
                Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
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
                finishAffinity();
            }
        });
        builder.setNegativeButton("No", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
