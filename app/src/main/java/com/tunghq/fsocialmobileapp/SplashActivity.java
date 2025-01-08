package com.tunghq.fsocialmobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tunghq.fsocialmobileapp.Util.KeystoreUtils;


public class SplashActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //init firebase
        auth=FirebaseAuth.getInstance ();
        user=auth.getCurrentUser ();

        try {
            KeystoreUtils.generateKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        new Handler(  ).postDelayed (new Runnable () {
            @Override
            public void run() {
                if (user !=null)
                {
                    startActivity ( new Intent( SplashActivity.this,HomeActivity.class ) );
                }
                else
                {
                    startActivity ( new Intent ( SplashActivity.this, SignInActivity.class ) );
                }
                finish ();
            }
        },1500 );
    }
}