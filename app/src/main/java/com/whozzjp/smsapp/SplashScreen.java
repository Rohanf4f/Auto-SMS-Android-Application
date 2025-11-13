package com.whozzjp.smsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);




        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;


        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "e", Toast.LENGTH_SHORT).show();
        }


        Thread background = new Thread(){
            public void run(){
                try{
                    sleep(3*1000);
                    startActivity(new Intent(SplashScreen.this, LoginActivity.class));

                    finish();
                }
                catch (Exception e){}
            }
        };
        background.start();






    }

}