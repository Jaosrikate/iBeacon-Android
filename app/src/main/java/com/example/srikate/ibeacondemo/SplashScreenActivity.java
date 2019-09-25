package com.example.srikate.ibeacondemo;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by srikate on 10/6/2017 AD.
 */

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Start home activity
        startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
        // close splash activity
        finish();
    }
}
