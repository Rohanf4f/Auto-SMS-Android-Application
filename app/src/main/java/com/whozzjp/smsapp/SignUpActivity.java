package com.whozzjp.smsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    MaterialTextView signin_activity;
    TextInputLayout verify_phoneno;
    CheckInternet check=new CheckInternet();

    TextInputEditText name, address, mail, mobile, pass, cnfPass;
    MaterialButton signupBtn;
    private FirebaseAuth auth;



    //Firebase database
    FirebaseAuth fAuth;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reference = database.getReference("Users");


    TextView viewTerms;
    CheckBox termsCB;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        /*//Load image to ImageView
        ImageView logo =findViewById(R.id.header_image);
        Glide.with(SignupActivity.this)
                .load(R.drawable.signup_header)
                .override(1080, 810)
                .into(logo);*/

        //Authentication
        fAuth = FirebaseAuth.getInstance();

        verify_phoneno = findViewById(R.id.phone_no);
        signin_activity = findViewById(R.id.signin_activity);
        signupBtn = findViewById(R.id.signup_btn);

        name = findViewById(R.id.user_name);
        address = findViewById(R.id.user_address);
        mail = findViewById(R.id.user_email);
        mobile = findViewById(R.id.user_mobile);
        pass = findViewById(R.id.user_pass);
        cnfPass = findViewById(R.id.user_cnfpass);




        //view terms and conditions
        signupBtn.setVisibility(View.GONE);
        signupBtn.setEnabled(false);


        termsCB = findViewById(R.id.cb_terms);
        termsCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    signupBtn.setVisibility(View.VISIBLE);
                    signupBtn.setEnabled(true);

                    //Firebase

                    String userName = name.getText().toString();
                    String userAddress = address.getText().toString();
                    String userEmail = mail.getText().toString();
                    String userMobileNo = mobile.getText().toString();
                    String userPass = pass.getText().toString();
                    String userConPass = cnfPass.getText().toString();
                   // String userReferalId = referalId.getText().toString();
                   // String link="https://b2bdigitalworld.com/c/";



                    Map<String, Object> user = new HashMap<>();
                    user.put("Username", userName);
                    user.put("Address", userAddress);
                    user.put("Email", userEmail);
                    user.put("Mobile No", userMobileNo);
                    user.put("Password", userPass);
                    user.put("Confirm Password", userConPass);
                    //user.put("Referal Id", userReferalId);
                   // user.put("Link",link);


                    signupBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                startActivity(new Intent(SignupActivity.this, HomeActivity.class));

                            if(TextUtils.isEmpty(name.getText().toString()) && TextUtils.isEmpty(address.getText().toString())
                                    && TextUtils.isEmpty(mail.getText().toString()) && TextUtils.isEmpty(mobile.getText().toString())
                                    && TextUtils.isEmpty(pass.getText().toString()) && TextUtils.isEmpty(cnfPass.getText().toString())
                                   ){  //TO CHECK IF BOTH EDITTEXT ARE EMPTY

                                name.setError("Please enter fullname");
                                address.setError("Please enter address");
                                mail.setError("Please enter your E-mail");
                                mobile.setError("Please enter mobile number");
                                pass.setError("Please enter password");
                                cnfPass.setError("Please enter your password");
                                //referalId.setError("Please enter referral id");

                                termsCB.setVisibility(View.GONE);

                            } else if (mobile.getText().toString().length()!=10) {
                                mobile.setError("Mobile Number is not valid");

                            } else if(pass.getText().toString().length() < 6 && !isValidPassword(pass.getText().toString())){
                                pass.setError("Password not valid");
                            } else if (!(mail.getText().toString().contains("@")&& mail.getText().toString().contains("."))) {
                                mail.setError("Please enter valid mail");
                            } else if(pass.getText().toString().equals(cnfPass.getText().toString())){



                                //Firebase database

                                fAuth.signInWithEmailAndPassword(mail.getText().toString(), pass.getText().toString())
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()){



                                                    // User already exists, show error message or take desired action
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                                    builder.setMessage("Your account is already Signed In\n Thank you!")
                                                            .setCancelable(false)
                                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int id) {
                                                                    dialog.dismiss();
                                                                    startActivity(new Intent(SignUpActivity.this,  LoginActivity.class));
                                                                }
                                                            });
                                                    AlertDialog alert = builder.create();
                                                    alert.show();
                                                }else{
                                                    // User doesn't exist, create a new user
                                                    fAuth.createUserWithEmailAndPassword(mail.getText().toString(), pass.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                                            if (task.isSuccessful()) {
                                                                User ud=new User(mail.getText().toString(),pass.getText().toString());
                                                                String id=task.getResult().getUser().getUid();
                                                                String currentDeviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

                                                                // reference.child(id).setValue(ud);

                                                                DatabaseReference root = FirebaseDatabase.getInstance().getReference().child("Users");
                                                                // DatabaseReference newMember = root.push();
                                                                root.child(id).child("Username").setValue(name.getText().toString());
                                                                root.child(id).child("Address").setValue(address.getText().toString());
                                                                root.child(id).child("Email").setValue(mail.getText().toString());
                                                                root.child(id).child("Mobile No").setValue(mobile.getText().toString());
                                                                root.child(id).child("Password").setValue(pass.getText().toString());
                                                                root.child(id).child("deviceID").setValue(currentDeviceID);
                                                               // root.child(id).child("Referal Id").setValue(referalId.getText().toString());
                                                               // root.child(id).child("Link").setValue("https://b2bdigitalworld.com/c/");


//                                        Toast.makeText(SignupActivity4.this, "signup successful", Toast.LENGTH_SHORT).show();

                                                                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                                                finish();
                                                            } else {
                                                                // Toast.makeText(SignupActivity4.this, "Please check E-mail & Password enter valid details", Toast.LENGTH_SHORT).show();
                                                                AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                                                builder.setMessage("Please check your entered E-mail is valid or Password has minimum length of 6 digit\n Thank you!")
                                                                        .setCancelable(false)
                                                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                            public void onClick(DialogInterface dialog, int id) {
                                                                                dialog.dismiss();
                                                                            }
                                                                        });
                                                                AlertDialog alert = builder.create();
                                                                alert.show();

                                                            }

                                                        }
                                                    });
                                                }
                                            }
                                        });


                            }
                            else{
                                //Toast.makeText(SignupActivity4.this, "Please fill with proper deatials", Toast.LENGTH_SHORT).show();

                                AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                builder.setMessage("Both passwords doesn't match \n Please enter correct password...!")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.dismiss();
                                            }
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        }
                    });
                }else {
                    signupBtn.setVisibility(View.GONE);
                    signupBtn.setEnabled(false);
                }
            }
        });

        viewTerms = findViewById(R.id.view_terms);
        viewTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://technolitesolutions.com/terms-and-conditions-connectit/"));
                startActivity(intent);
            }
        });


        verify_phoneno.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(SignupActivity.this, OTPVerificationActivity.class);
//                startActivity(intent);
            }
        });

        signin_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    public static boolean isValidPassword(final String password) {  //DEFINATION OF METHOD FOR  VALIDATION OF THE PASSWORD
        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";  //VALIDATION STRING
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);
        return matcher.matches();
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
    protected void onStart() {

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION ) ;
        registerReceiver(check, filter)  ;
        super.onStart();

       /* try {
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                finish();
            }
        }catch (Exception e){
            Log.d("Firebase",e.getMessage());
        }*/

    }

    @Override
    protected void onStop() {

        unregisterReceiver(check);
        super.onStop();
        //  FirebaseAuth.getInstance().signOut();
    }


}