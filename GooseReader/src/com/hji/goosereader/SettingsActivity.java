package com.hji.goosereader;

import com.hardyjones.goosereader.R;

import android.preference.PreferenceActivity;
import android.os.Bundle;

public class SettingsActivity extends PreferenceActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    // Grab the layout.
	    addPreferencesFromResource(R.xml.settings);
	}

}
