/* GooseActivity.java
 * 
 * Version 1.0
 * 
 * Copyright 2011 Hardy Jones III
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


package com.hardyjones.goosereader;

import java.io.IOException;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
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
	private static int sCurrentComicNumber; // This is the latest comic.
	private static int sPresentComicNumber; // This is the comic that is presently in view.
	private static String sImageUrl;
	private static String sPresentUrl;
	private static Dialog sInfoDialog;
	private static LinearLayout sNavigationLayout;
	private static TextView sAltTextView;
    private static WebView sComicView;
	private static Document sRawHtml;

	/** Called when the activity is first created. */
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
    }
    
    /** Refreshes to the newest comic. */
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	sPresentComicNumber = 0;
    	findCurrentComic();
    }
	
    /**
     * Handles each button in the navigation layout.
     * @param button The button that performed the event.
     */
	public void navigationButtonHandler(View button) {
		button.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		
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
	
	/** Parses the home page for the value of the current comic. */
    private void findCurrentComic() {
    	retrieveImage();

    	Element currentComicTag = sRawHtml.select(getString(R.string.find_comic_tag)).first();
    	String currentComicString = currentComicTag.attr(getString(R.string.hyperlink));
    	int lastForwardSlash = currentComicString.lastIndexOf(getString(R.string.forward_slash)) + 1;
    	
    	/* This is actually the second most recent comic, so increment. */
    	sCurrentComicNumber = Integer.parseInt(currentComicString.substring(lastForwardSlash));
    	sPresentComicNumber = ++sCurrentComicNumber;

    	loadComic();
    }

    /** Parses the present comic URL for the image URL. */
    private void retrieveImage() {
    	try {
			sPresentUrl = getString(R.string.base_url) + String.valueOf(sPresentComicNumber);
    		sRawHtml = Jsoup.connect(sPresentUrl).get();
    		Element imageTag = sRawHtml.select(getString(R.string.find_image_tag)).first();
    		sImageUrl = imageTag.attr(getString(R.string.image_source));
    	} catch (IOException e){
    		e.printStackTrace();
    	}
    }

    /** Loads the present comic URL into the webview. */
    private void loadComic() {
    	final ProgressDialog loadingComic = ProgressDialog.show(GooseActivity.this, "", getString(R.string.loading_comic));
    	loadingComic.setCancelable(true);
    	retrieveImage();
    	sComicView.loadUrl(sImageUrl);
    	sComicView.setWebChromeClient(new WebChromeClient() {
    		@Override
    		public void onProgressChanged(WebView view, int progress) {
    			if (progress == 100) {
    				loadingComic.dismiss();
    			}
    		}
    	});
    	/* XXX There has to be a better way to keep the cache from filling. */
    	sComicView.clearCache(true);
    }

    /** Creates a menu when the menu button is pressed on the device. */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater mainMenu = getMenuInflater();
    	mainMenu.inflate(R.menu.menu, menu);

    	return true;
    }

    /** Chooses which menu selection was pressed. */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.toggleNavigation:
    		sNavigation = !sNavigation;
    		toggleNavigation();
    		break;
    	case R.id.info:
    		showDialog(SHOW_INFO_DIALOG);
    		break;
    	default:
    		break;
    	}
    	
    	return true;
    }
    
    /** Toggles the comic navigation on the screen. */
    private void toggleNavigation() {
    	if (sNavigation == true) {
    		sNavigationLayout.setVisibility(View.VISIBLE);
    	} else {
    		sNavigationLayout.setVisibility(View.GONE);
    	}
    }

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
     * Shows additional information including: comic title and alt text.
     * @param id ID of the dialog to prepare.
     * @param dialog The dialog to prepare.
     */
    @Override
    protected void onPrepareDialog (int id, Dialog dialog) {
    	switch (id) {
    	case SHOW_INFO_DIALOG:
    		
    		/* Parse the comic title and alt text if available. */
    		Element titleTag = sRawHtml.select(getString(R.string.find_title_tag)).first();
    		String title = titleTag.text();
    		Element imageTag = sRawHtml.select(getString(R.string.find_image_tag)).first();
    		String altText = imageTag.attr(getString(R.string.image_title));

    		sInfoDialog.setTitle(title);
			sAltTextView.setText(altText);
		default:
			break;
    	}
    }
    
    private void parseImageSource(String rawString) {
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
}