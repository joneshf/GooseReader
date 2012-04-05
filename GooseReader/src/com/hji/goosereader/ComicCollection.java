/* ComicCollection.java
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
import java.io.ObjectInputStream.GetField;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import android.content.Context;
import android.os.Environment;

/**
 * A collection to handle comics for offline usage.
 * 
 * @author Hardy Jones III
 *
 */
public class ComicCollection {
	private boolean mExternalStorageAvailable;
	private boolean mExternalStorageWritable;
	private int mSize;
	private int mCurrentNumber;
	private String mCurrentImage;
	private String mDirLocation;
	private String mState;
	private Random mRandomNumber;
	private List<Integer> mComicNumbers;
	private TreeMap<Integer, String> mComicMap;
	
	public ComicCollection() {
		mRandomNumber = new Random();
		mComicMap = new TreeMap<Integer, String>();
		mComicNumbers = new ArrayList<Integer>();
		
		// Check the status of the media.
		checkMediaStatus();
	}
	
	public void addComic(int number, String image) {
		// Ensure the number is non-negative.
		if (number < 0) {
			// Do nothing if it is negative.
			return ;
		}
		
		// Check if we already have an element.
		if (mComicMap.containsKey(number)) {
			// Do nothing here also.
			return ;
		}
		
		// We made it past the checks, so let's add the comic number to the list,
		// and the number:image to the map.
		mComicNumbers.add(number);
		mComicMap.put(number, image);
		// Set the currentNumber and image accordingly.
		mCurrentNumber = number;
		mCurrentImage = image;
		// increase the size of the collection.
		mSize++;
	}
	
	public void removeComic(int number) {
		// Ensure the number is non-negative.
		if (number < 0) {
			// Do nothing if it is negative.
			return ;
		}
		// Make sure we actually have this element.
		if (!mComicMap.containsKey(number)) {
			// Do nothing here also.
			return ;
		}
		
		// Get rid of the number from the list and map.
		mComicNumbers.remove(mComicNumbers.indexOf(number));
		mComicMap.remove(number);
	}
	
	public int getComicNumber() {
		return mCurrentNumber;
	}
	
	public String getComicImage() {
		return mCurrentImage;
	}
	
	// Navigation controls.
	
	public void first() {
		// Set the current number to the first number in the list.
		mCurrentNumber = mComicNumbers.get(0);
		// Set the current image based on the current number.
		setImage();
	}
	
	public void previous() {
		// Create an int to mark the position in the number list.
		int position = mComicNumbers.indexOf(mCurrentNumber);
		// Check that we're still in bounds.
		if (position > 0) {
			// Since we can go back one, at least, go back one.
			mCurrentNumber = mComicNumbers.get(--position);
		}
		// Either we're at the beginning, or we just moved back one.
		// In either case, we're still in bounds so we can...		
		// Set the currentImage based on the current number.
		setImage();
	}
	
	public void random() {
		// Get a new random number and immediately access that number in the list.
		mCurrentNumber = mComicNumbers.get(mRandomNumber.nextInt(mSize));
		// Set the currentImage based on the current number.
		setImage();
	}
	
	public void next() {
		// Create an int to mark the position in the number list.
		int position = mComicNumbers.indexOf(mCurrentNumber);
		// Check that we're still in bounds.
		// Increment here, so it makes comparison easier.
		if (++position < mSize) {
			// Since we can go forward one, at least, go forward one.
			mCurrentNumber = mComicNumbers.get(position);
		}
		// Either we're at the end, or we just moved forward one.
		// In either case, we're still in bounds so we can...		
		// Set the currentImage based on the current number.
		setImage();
	}
	
	public void last() {
		// Set the current number to the last number in the list.
		mCurrentNumber = mComicNumbers.get(mSize - 1);
		// Set the currentImage based on the current number.
		setImage();
	}
	
	/**
	 * Sets the comic image based on the current number.
	 */
	private void setImage() {
		mCurrentImage = mComicMap.get(mCurrentNumber);
	}
	
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
	
	private void saveImage(String imageUrl) {
		// Make sure we can write.
		checkMediaStatus();
		if (mExternalStorageAvailable && mExternalStorageWritable) {
			try {
				// Get the image.
				URL url = new URL(imageUrl);
				InputStream is = (InputStream) url.getContent();
				// Create a file
				File file = new File(Environment.getExternalStorageDirectory(), "/images/" + mCurrentImage);
				// Create the output stream.
				OutputStream os = new FileOutputStream(file);
				// Create a byte buffer to read into and write from.
				byte buffer[] = new byte[4096];
				// Read the bytes, and check for nothing else having been read.
				while (-1 != is.read(buffer)) {
					// Write the bytes to the file.
					os.write(buffer);
				}
				// Close the streams.
				is.close();
				os.close();
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
}
