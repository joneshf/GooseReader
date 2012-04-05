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

import java.io.IOException;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
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
	private static String sImageUrl;
	private static String sPresentUrl;
	private static Dialog sInfoDialog;
	private static LinearLayout sNavigationLayout;
	private static TextView sAltTextView;
    private static WebView sComicView;
	private static Document sRawHtml;
	
	// XXX Testing offline.
	private static boolean sOffline;
	private static String sImageName;
	private static ComicCollection sOfflineCollection;
	// XXX Testing offline.
	
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
    	// Scrapy scrapy.
    	scrapeSite();
    	
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
//    	sComicView.clearCache(true);
    }

    /**
     * Handles each button in the navigation layout.
     * @param button The button that performed the event.
     */
	public void navigationButtonHandler(View button) {
		button.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		
		// XXX Testing offline.
		if (sOffline) {

		}
		// XXX Testing offline.
		else {

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
			loadComic();
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

    	// XXX Testing offline.
    	sOfflineCollection = new ComicCollection();
    	sOffline = false;
    	// XXX Testing offline.
    	
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
    	case R.id.toggleNavigation:
    		// Flip the boolean.
    		sNavigation = !sNavigation;
    		// Display/hide the nav.
    		toggleNavigation();
    		break;
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
		// XXX Testing offline.
    	case R.id.offline:
    		// Turn on offline mode.
    		sOffline = true;
    		break;
		// XXX Testing offline.
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
    		sInfoDialog.setTitle(sComicTitle);
			sAltTextView.setText(sAltText);
		default:
			break;
    	}
    }
    
    // XXX Testing offline.
    private void parseImageName(String rawString) {
    	// Rip out everything from the last forward slash.
    	sImageName = rawString.substring(rawString.lastIndexOf("/"));
    }
    // XXX Testing offline.
    
	
    /**
     * Ensures the string for the image source is a valid url.
     * 
     * E.g.: parseImageSource("/comic37.PNG") -> "http://base_url/comic37.PNG"
     * 
     * @param rawString  The datum to parse into a proper url. 
     */
    private void parseImageSource(String rawString) {
        // XXX Testing offline.
    	parseImageName(rawString);
        // XXX Testing offline.

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
					// XXX Testing offline.
			    	sOfflineCollection.addComic(sPresentComicNumber, sImageName);
			    	// XXX Testing offline.
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
    	String comicLink = getString(R.string.share_message) + sPresentUrl;
    	// Pass it to the intent.
    	shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, comicLink);
    	// Call the activity.
    	startActivity(Intent.createChooser(shareIntent, getString(R.string.share_title)));
    }
    
    /** 
     * Toggles the comic navigation on the screen.
     */
    private void toggleNavigation() {
    	if (sNavigation == true) {
    		sNavigationLayout.setVisibility(View.VISIBLE);
    	} else {
    		sNavigationLayout.setVisibility(View.GONE);
    	}
    }
}