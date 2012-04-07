/* GooseActivity.java
 * 
 * Version 1.1.3
 * 
 * Copyright 2011-2012 Hardy Jones III
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.hji.goosereader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hardyjones.goosereader.R;

/**
 * A simple comic reader for the website http://abstrusegoose.com/
 * 
 * @author Hardy Jones III
 */
public class GooseActivity extends Activity {
	
	private static final int SHOW_INFO_DIALOG = 0;
	private static final int FIRST_COMIC_NUMBER = 1;
	private static final Random sRandomNumber = new Random();
	private static boolean sNavigation = true;
	private static boolean sScraped = false;
	// This is the latest comic.
	private static int sCurrentComicNumber;
	// This is the comic that is presently in view.
	// Default to zero to make things easy.
	private static int sPresentComicNumber = 0;
	private static String sAltText;
	private static String sComicTitle;
	private static String sImageName;
	private static String sImageUrl;
	private static String sPresentUrl;
	private static Dialog sInfoDialog;
	private static LinearLayout sNavigationLayout;
	private static TextView sAltTextView;
    private static WebView sComicView;
	private static Document sRawHtml;
	
	private static SharedPreferences sSettings;
    // XXX DB crap.
	private static SQLiteDatabase sComicsDb;
	private static ComicOpenHelper sComicsDbHelper;
	private static String sNumberLookup[] = {/*ComicOpenHelper.COLUMN_IMAGE,
		ComicOpenHelper.TABLE_COMICS, ComicOpenHelper.COLUMN_NUMBER, */""};
	private static ContentValues sValues = new ContentValues();
	private static boolean sOffline;
	private static boolean sSaveLocal;
	private boolean mExternalStorageAvailable;
	private boolean mExternalStorageWritable;
	private String mState;
	private String mDirLocation;
	private Cursor mCursor;
    // XXX DB crap.
	
    // XXX DB crap.
	private void checkMediaStatus() {
		mState = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(mState)) {
			// The media is available and writable.
			mExternalStorageAvailable = true;
			mExternalStorageWritable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(mState)) {
			// The media is available, but read only.
			mExternalStorageAvailable = true;
			mExternalStorageWritable = false;
		} else {
			// We cannot do anything with the media.
			mExternalStorageAvailable = false;
			mExternalStorageWritable = false;
		}
	}
    // XXX DB crap.
	
	/**
	 * Parses the home page for the value of the current comic.
	 */
    private void findCurrentComic() {
    	// It's scrapin' time!
    	scrapeSite();
    	// Set scraped to true, so we don't have to do this again so soon.
    	sScraped = true;
    	// Set the current comic number based on what was just parsed.
    	sCurrentComicNumber = sPresentComicNumber;
    	// Display this latest comic.
    	loadComic();
    	// Reset scraped to false, so things will work like they should.
    	sScraped = false;
    }

    /**
     * Loads the present comic URL into the webview.
     */
    private void loadComic() {
    	// Throw up a progress dialog box.
    	final ProgressDialog loadingComic = ProgressDialog.show(GooseActivity.this, "", getString(R.string.loading_comic));
    	// Let the user cancel it if need be.
    	loadingComic.setCancelable(true);
    	
        // XXX DB crap.
    	// Put the present comic number in the lookup string.
    	sNumberLookup[0] = String.valueOf(sPresentComicNumber);
    	// Check if we have the current comic already.
    	if (mExternalStorageAvailable) {
    		mCursor = sComicsDb.rawQuery("select _image from comics where _number = ?", sNumberLookup);
    	   	if (mCursor.getCount() > 0) {
    	   		mCursor.moveToFirst();
    	   		sImageUrl = "file://" + mDirLocation + mCursor.getString(0);
    	   	} else {
    	   		// Scrapy scrapy.
    	   		scrapeSite();
    	   		
    	   		if (sSaveLocal && mExternalStorageWritable) {
    	   			// Insert content into the content value.
    	   			sValues.put("_number", sPresentComicNumber);
    	   			sValues.put("_image", sImageName);
    	   			sValues.put("_title", sComicTitle);
    	   			sValues.put("_text", sAltText);
    	   			// Add the info to the db.
    	   			sComicsDb.insert("comics", null, sValues);
    	   			// Save the file.
    	   			saveImage(sImageUrl);
    	   			// Set the image url.
    	   			sImageUrl = "file://" + mDirLocation + sImageName;
    	   		}
    	   	}
    	} else {
    		scrapeSite();
    	}
	   	// XXX DB crap.
    	
    	sComicView.loadUrl(sImageUrl);
    	sComicView.setWebChromeClient(new WebChromeClient() {
    		@Override
    		public void onProgressChanged(WebView view, int progress) {
    			if (progress == 100) {
    				loadingComic.dismiss();
    			}
    		}
    	});
    	/* 
    	 * XXX There has to be a better way to keep the cache from filling.
    	 * Especially since this doesn't work.
    	 */
    	sComicView.clearCache(true);
    }

    private void loadSettings() {
		// Load the settings.
		sSettings = PreferenceManager.getDefaultSharedPreferences(this);
		sNavigation = sSettings.getBoolean("prefNavigation", true);
		sOffline = sSettings.getBoolean("prefOfflineMode", false);
		sSaveLocal = sSettings.getBoolean("prefSaveLocal", true);
		checkNavigation();
    }
    
    /**
     * Handles each button in the navigation layout.
     * @param button The button that performed the event.
     */
	public void navigationButtonHandler(View button) {
		button.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		
		// Check if we're supposed to be using offline comics.
		if (sOffline) {
			// Call the offline version of this.
			offlineNav(button);
		} else {
			// We're just doing things normally.
			switch (button.getId()) {
			case R.id.firstButton:
				sPresentComicNumber = FIRST_COMIC_NUMBER;
				break;

				/* 
				 * Don't go past the beginning.
				 * Comic 0 is actually the current comic.
				 * Handle this like this case by going to the second newest comic.
				 */
			case R.id.previousButton:
				switch (sPresentComicNumber) {
				case 0:
					sPresentComicNumber = sCurrentComicNumber - 1;
					break;
				case 1:
					sPresentComicNumber = FIRST_COMIC_NUMBER;
					break;
				default:
					sPresentComicNumber--;
				}
				break;
			case R.id.randomButton:
				sPresentComicNumber = sRandomNumber.nextInt(sCurrentComicNumber);
				break;
				/* Don't go past the end or comic 0, which is actually the current comic. */
			case R.id.nextButton:
				if ((sPresentComicNumber == sCurrentComicNumber) || (sPresentComicNumber == 0)) {
					sPresentComicNumber = sCurrentComicNumber;
				} else {
					sPresentComicNumber++;
				}
				break;
			case R.id.currentButton:
				sPresentComicNumber = sCurrentComicNumber;
				break;
			default:
				break;
			}
		}
		loadComic();
	}
	
	private void offlineNav(View button) {
		int cursorSize;
		// First we need the comics we can work with.
		mCursor = sComicsDb.rawQuery("select _number from comics order by _number asc", null);
		cursorSize = mCursor.getCount();
		
		if (cursorSize > 0) {
			switch (button.getId()) {
			case R.id.firstButton:
				// Move the cursor to the first row.
				mCursor.moveToFirst();
				break;
			case R.id.previousButton:
				// Find the first comic number lower than the present value.
				// Move the cursor to this row, or the beginning if none are lower.
				Cursor prevCursor = sComicsDb.rawQuery(
						"select _number from comics where _number < " + sPresentComicNumber +
						" order by _number desc", null);
				if (prevCursor.getCount() > 0){
					// There's at least one comic less than the present one.
					// Copy the prevCursor to the mCursor and work with that.
					mCursor = prevCursor;
				}
				// Move the cursor to the first row.
				mCursor.moveToFirst();
				break;
			case R.id.randomButton:
				// Move the cursor to some random row.
				mCursor.moveToPosition(sRandomNumber.nextInt(cursorSize));
				break;
			case R.id.nextButton:
				// Find the first comic number higher than the present value.
				// Move the cursor to this row, or the beginning if none are higher.
				Cursor nextCursor = sComicsDb.rawQuery(
						"select _number from comics where _number > " + sPresentComicNumber +
						" order by _number asc", null);
				if (nextCursor.getCount() > 0){
					// There's at least one comic less than the present one.
					// Copy the prevCursor to the mCursor and work with that.
					mCursor = nextCursor;
					// Move the cursor to the first row.
					mCursor.moveToFirst();
				} else {
					// Otherwise, we're at the greatest comic number stored.
					mCursor.moveToLast();
				}
				break;
			case R.id.currentButton:
				mCursor.moveToLast();
				break;
			default:
				break;
			}

			// Now that the cursor is in the right position.
			// Get the comic number.
			sPresentComicNumber = mCursor.getInt(0);
		} else {
			Toast.makeText(getApplicationContext(), "There are no comics", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		/* Set up the webview */
		sComicView = (WebView) findViewById(R.id.comicView);
		sComicView.getSettings().setBuiltInZoomControls(true);
		sComicView.getSettings().setLoadWithOverviewMode(true);
		sComicView.getSettings().setUseWideViewPort(true);
		sComicView.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				showDialog(SHOW_INFO_DIALOG);

				return true;
			}
		});

		/* Set up the navigation control layout if it is to be hidden. */
		sNavigationLayout = (LinearLayout) findViewById(R.id.buttonLayout);

		loadSettings();
		// XXX DB crap.
		// Get the database set up.
		checkMediaStatus();
		sComicsDbHelper = new ComicOpenHelper(this);
		sComicsDb = sComicsDbHelper.getWritableDatabase();
		if (mExternalStorageAvailable) {
			String topLevel = Environment.getExternalStorageDirectory().getPath();
			mDirLocation = topLevel + "/Android/data/com.hardyjones.goosereader/images";
			// Try to make the folder if it doesn't exist.
			File imageFolder = new File(mDirLocation);
			File noMedia = new File(mDirLocation + "/.nomedia");
			if (!imageFolder.exists() && mExternalStorageWritable) {
				try {
					imageFolder.mkdirs();
					noMedia.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	    // XXX DB crap.
		// Get the latest comic number.
		findCurrentComic();

	}

    /**
     * Creates a dialog for the alt text.
     */
    @Override
    protected Dialog onCreateDialog(int id) {
    	switch (id) {
    	case SHOW_INFO_DIALOG:
    		sInfoDialog = new Dialog(this);
    		sInfoDialog.setContentView(R.layout.alt_text);
    		sAltTextView = (TextView) sInfoDialog.findViewById(R.id.altTextView);
    		
    		return sInfoDialog;
    	default:
    		break;
    	}
    	return super.onCreateDialog(id);
    }
    
    /**
     * Creates a menu when the menu button is pressed on the device.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater mainMenu = getMenuInflater();
    	mainMenu.inflate(R.menu.menu, menu);

    	return true;
    }

    /** 
     * Chooses which options menu selection was pressed.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.info:
    		// Bring up the alt-text.
    		showDialog(SHOW_INFO_DIALOG);
    		break;
    	case R.id.refresh:
    		// Load the same comic again.
    		loadComic();
    		break;
    	case R.id.share:
    		// Start the share intent.
    		shareComic();
    		break;
		case R.id.settings:
			// Create an intent and start the settings activity.
			Intent settings = new Intent(this, SettingsActivity.class);
			startActivity(settings);
			break;
    	default:
    		break;
    	}
    	
    	return true;
    }
    
    /** 
     * Shows additional information including: comic title and alt text.
     * @param id ID of the dialog to prepare.
     * @param dialog The dialog to prepare.
     */
    @Override
    protected void onPrepareDialog (int id, Dialog dialog) {
    	switch (id) {
    	case SHOW_INFO_DIALOG:
    		// Query the database.
    		mCursor = sComicsDb.rawQuery("select * from comics where _number = " + sPresentComicNumber, null);
    		if (mCursor.getCount() > 0) {
    			// Get the correct title and alt text.
    			mCursor.moveToFirst();
    			sComicTitle = mCursor.getString(3);
    			sAltText = mCursor.getString(4);
    		}
    		// Set the dialog title and text.
    		sInfoDialog.setTitle(sComicTitle);
    		sAltTextView.setText(sAltText);
    	default:
			break;
    	}
    }
	
	@Override
	protected void onResume() {
		super.onResume();

		loadSettings();
	}

	private void parseImageName(String rawString) {
		// Rip out everything from the last forward slash.
		sImageName = rawString.substring(rawString.lastIndexOf("/"));
	}

	/**
     * Ensures the string for the image source is a valid url.
     * 
     * E.g.: parseImageSource("/comic37.PNG") -> "http://base_url/comic37.PNG"
     * 
     * @param rawString  The datum to parse into a proper url. 
     */
    private void parseImageSource(String rawString) {
    	// Set just the image name.
    	parseImageName(rawString);
    	// Check if the image source has the base url on it.
    	if (!rawString.startsWith(getString(R.string.base_url))) {
    		// If it doesn't, prepend the string with the base_url,
    		// Remembering to remove the slash from the raw string.
    		sImageUrl = getString(R.string.base_url) + rawString.substring(1);
    	} else {
    		// The string should already be parsed.
    		sImageUrl = rawString;
    	}
    }
    
	public void saveImage(String imageUrl) {
		// Make sure we can write.
		checkMediaStatus();
		if (mExternalStorageAvailable && mExternalStorageWritable) {
			try {
				// Get the image.
				URL url = new URL(imageUrl);
				InputStream is = (InputStream) url.getContent();
				// Create a file
				File file = new File(mDirLocation + sImageName);
				// Create the output stream.
				OutputStream os = new FileOutputStream(file);
				// Create a byte buffer to read into and write from.
				byte buffer[] = new byte[4096];
				int bytesRead;
				// Read the bytes, and check for nothing else having been read.
				while (-1 != (bytesRead = is.read(buffer))) {
					// Write the bytes to the file.
					os.write(buffer, 0, bytesRead);
				}
				// Close the streams.
				is.close();
				os.close();
				// Set the current image location.
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// We cannot write, so don't do anything.
		}
	}
    
    /**
     * Does a whole mess of stuff.
     * 
     * Gets the correct comic number, image source, comic title, and alt-text.
     */
    private void scrapeSite() {
    	// If we just recently did this, return.
    	if (sScraped) {
    		return;
    	}
    	// Otherwise, it's time for some fun.
    	try {
    		// Set the present URL, in case it's never been done.
    		sPresentUrl = getString(R.string.base_url) + sPresentComicNumber;
    		// Connect to the site and get the HTML.
			sRawHtml = Jsoup.connect(sPresentUrl).get();
			// Try to get the div with a post class.
			Elements divTags = sRawHtml.select(getString(R.string.find_div_tag));
			
			if (divTags.size() > 0) {
				// Assuming we got any divs, take the first one.
				Element divTag = divTags.first();
				// Get the current comic number.
				String presentNumber = divTag.id().substring(5);
				// Convert it to an integer, so we can play with it.
				sPresentComicNumber = Integer.parseInt(presentNumber);
				// Now that we have the actual comic number,
				// reset the present URL to the actual URL.
				sPresentUrl = getString(R.string.base_url) + sPresentComicNumber; 
				
				// Grab the comic header and the img stuff.
				Elements anchorTags = divTag.select(getString(R.string.find_anchor_tag));
				Elements imageTags = divTag.select(getString(R.string.find_image_tag));

				// See if we got any anchor tags.
				if (anchorTags.size() > 0) {
					// Take the text of the tag as the comic title.
					sComicTitle = anchorTags.first().ownText();
				} else {
					// There was no title for the comic. Use a default value.
					sComicTitle = getString(R.string.app_name);
				}
				
				// See if we got any image tags.
				if (imageTags.size() > 0) {
					// Nab the source of the image and the title.
					String rawSource = imageTags.first().attr(getString(R.string.image_source));
					sAltText = imageTags.first().attr(getString(R.string.image_title));
					// Send the image source to parsing.
					parseImageSource(rawSource);
				} else {
					// There was no image tag.
					Toast.makeText(getApplicationContext(), R.string.loading_error, Toast.LENGTH_SHORT).show();
				}
			} else {
				// Couldn't find a div tag.
				Toast.makeText(getApplicationContext(), R.string.loading_error, Toast.LENGTH_SHORT).show();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * Creates a share intent and calls it for the user to share a link to the comic.
     */
    private void shareComic() {
    	// Create the intent.
    	Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
    	// Let's use just plain text.
    	shareIntent.setType(getString(R.string.plain_text));
    	// Create the content.
    	String comicLink = getString(R.string.share_message) + getString(R.string.base_url) + sPresentComicNumber;
    	// Pass it to the intent.
    	shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, comicLink);
    	// Call the activity.
    	startActivity(Intent.createChooser(shareIntent, getString(R.string.share_title)));
    }
    
    /** 
     * Toggles the comic navigation on the screen.
     */
    private void checkNavigation() {
    	if (sNavigation == true) {
    		sNavigationLayout.setVisibility(View.VISIBLE);
    	} else {
    		sNavigationLayout.setVisibility(View.GONE);
    	}
    }
}