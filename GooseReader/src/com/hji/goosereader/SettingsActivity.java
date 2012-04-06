package com.hji.goosereader;

import com.hardyjones.goosereader.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    addPreferencesFromResource(R.xml.settings);
	}
}
