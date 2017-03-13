package com.vizdashcam.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.vizdashcam.R;
import com.vizdashcam.SharedPreferencesHelper;

public class SplashscreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent intent;
                boolean appFirstOpen = SharedPreferencesHelper.getAppFirstOpen(getApplicationContext());

                intent = new Intent(SplashscreenActivity.this, MainActivity.class);

                startActivity(intent);

                SplashscreenActivity.this.finish();
            }
        }, 10000);
    }
}
