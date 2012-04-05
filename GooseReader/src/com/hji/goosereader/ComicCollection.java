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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

/**
 * A collection to handle comics.
 * 
 * @author Hardy Jones III
 *
 */
public class ComicCollection {
	private int size;
	private int currentNumber;
	private String currentImage;
	private Random randomNumber;
	private List<Integer> comicNumbers;
	private TreeMap<Integer, String> comicMap;
	
	public ComicCollection() {
		randomNumber = new Random();
		comicMap = new TreeMap<Integer, String>();
		comicNumbers = new ArrayList<Integer>();
	}
	
	public void addComic(int number, String image) {
		// Ensure the number is non-negative.
		if (number < 0) {
			// Do nothing if it is negative.
			return ;
		}
		
		// Check if we already have an element.
		if (comicMap.containsKey(number)) {
			// Do nothing here also.
			return ;
		}
		
		// We made it past the checks, so let's add the comic number to the list,
		// and the number:image to the map.
		comicNumbers.add(number);
		comicMap.put(number, image);
		// Set the currentNumber and image accordingly.
		currentNumber = number;
		currentImage = image;
		// increase the size of the collection.
		size++;
	}
	
	public void removeComic(int number) {
		// Ensure the number is non-negative.
		if (number < 0) {
			// Do nothing if it is negative.
			return ;
		}
		// Make sure we actually have this element.
		if (!comicMap.containsKey(number)) {
			// Do nothing here also.
			return ;
		}
		
		// Get rid of the number from the list and map.
		comicNumbers.remove(comicNumbers.indexOf(number));
		comicMap.remove(number);
	}
	
	public int getComicNumber() {
		return currentNumber;
	}
	
	public String getComicImage() {
		return currentImage;
	}
	
	// Navigation controls.
	
	public void first() {
		// Set the current number to the first number in the list.
		currentNumber = comicNumbers.get(0);
		// Set the current image based on the current number.
		setImage();
	}
	
	public void previous() {
		
		// Set the currentImage based on the current number.
		setImage();
	}
	
	public void random() {
		
		// Set the currentImage based on the current number.
		setImage();
	}
	
	public void next() {
		
		// Set the currentImage based on the current number.
		setImage();
	}
	
	public void last() {
		// Set the current number to the last number in the list.
		currentNumber = comicNumbers.get(size - 1);
		// Set the currentImage based on the current number.
		setImage();
	}
	
	/**
	 * Sets the comic image based on the current number.
	 */
	private void setImage() {
		currentImage = comicMap.get(currentNumber);
	}
}
