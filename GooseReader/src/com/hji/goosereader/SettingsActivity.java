package com.hji.goosereader;

import java.io.File;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

import com.hardyjones.goosereader.R;

public class SettingsActivity extends PreferenceActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    // Grab the layout.
	    addPreferencesFromResource(R.xml.settings);
	    
	    // Set up a listener for the delete button.
	    Preference prefDelete = (Preference) findPreference("prefDeleteComics");
	    prefDelete.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			public boolean onPreferenceClick(Preference preference) {
				
			    // XXX DB crap.
					String message;
					
					
					if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
						boolean allDeleted = true;
						boolean fileDeleted;
						String topLevel = Environment.getExternalStorageDirectory().getPath();
						topLevel += "/Android/data/com.hardyjones.goosereader/images";
						SQLiteDatabase comicsDb = new ComicOpenHelper(getBaseContext()).getWritableDatabase();
						Cursor mCursor = comicsDb.rawQuery("select _id, _image from comics", null);
						if (0 == mCursor.getCount()) {
							message = "Nothing to delete.";
						} else {
							for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
								fileDeleted = new File(topLevel, mCursor.getString(1)).delete();
								Log.d("file to delete", topLevel + mCursor.getString(1));
								if (fileDeleted) {
									comicsDb.delete("comics", "_id = " + mCursor.getInt(0), null);
								}
								allDeleted &= fileDeleted;
							}
							message = (allDeleted) ? "All comics deleted." : "Error deleting all comics.";
						}
						// Close the database.
						comicsDb.close();
						mCursor.close();
					} else {
						message = "Could not find comics to delete.";
					}

					// Show a message displaying what just happened.
					Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT);
			    // XXX DB crap.

				return false;
			}
		});
	}

}
