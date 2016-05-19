package com.vizdashcam.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.vizdashcam.R;
import com.vizdashcam.fragments.FragmentPreferences;

import java.util.List;

public class ActivitySettings extends AppCompatPreferenceActivity {

    private static final String TAG = "Settings";

    private Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        findViews();

        // Android API looks through rootView for @android:id/list and replaces that with the
        // FragmentPreferences instance
        getFragmentManager().beginTransaction()
                .replace(R.id.rootView, new FragmentPreferences()).commit();
    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    private void findViews() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
