package com.vizdashcam.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.vizdashcam.fragments.FragmentPreferences;

public class ActivitySettings extends PreferenceActivity {

	@SuppressWarnings("unused")
	private static final String TAG = "Settings";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().setTitle("Settings");

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new FragmentPreferences())
				.commit();
	}

}
