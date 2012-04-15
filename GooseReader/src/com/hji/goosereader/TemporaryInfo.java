/* TemporaryInfo.java
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

/**
 * This is a helper class to store the current info
 * about the comic while it is being updated.
 * 
 * @author Hardy Jones III
 *
 */
public class TemporaryInfo {
	
	private int mPresentComicNumber;
	private int mLatestComicNumber;
	private String mAltText;
	private String mComicTitle;
	private String mImageUrl;
	private String mPresentUrl;
	
	public TemporaryInfo(int mPresentComicNumber, int mLatestComicNumber,
			String mAltText, String mComicTitle, String mImageUrl,
			String mPresentUrl) {
		this.mPresentComicNumber = mPresentComicNumber;
		this.mLatestComicNumber = mLatestComicNumber;
		this.mAltText = mAltText;
		this.mComicTitle = mComicTitle;
		this.mImageUrl = mImageUrl;
		this.mPresentUrl = mPresentUrl;
	}

	/**
	 * @return the mPresentComicNumber
	 */
	public int getmPresentComicNumber() {
		return mPresentComicNumber;
	}

	/**
	 * @param mPresentComicNumber the mPresentComicNumber to set
	 */
	public void setmPresentComicNumber(int mPresentComicNumber) {
		this.mPresentComicNumber = mPresentComicNumber;
	}

	/**
	 * @return the mLatestComicNumber
	 */
	public int getmLatestComicNumber() {
		return mLatestComicNumber;
	}

	/**
	 * @param mLatestComicNumber the mLatestComicNumber to set
	 */
	public void setmLatestComicNumber(int mLatestComicNumber) {
		this.mLatestComicNumber = mLatestComicNumber;
	}

	/**
	 * @return the mAltText
	 */
	public String getmAltText() {
		return mAltText;
	}

	/**
	 * @param mAltText the mAltText to set
	 */
	public void setmAltText(String mAltText) {
		this.mAltText = mAltText;
	}

	/**
	 * @return the mComicTitle
	 */
	public String getmComicTitle() {
		return mComicTitle;
	}

	/**
	 * @param mComicTitle the mComicTitle to set
	 */
	public void setmComicTitle(String mComicTitle) {
		this.mComicTitle = mComicTitle;
	}

	/**
	 * @return the mImageUrl
	 */
	public String getmImageUrl() {
		return mImageUrl;
	}

	/**
	 * @param mImageUrl the mImageUrl to set
	 */
	public void setmImageUrl(String mImageUrl) {
		this.mImageUrl = mImageUrl;
	}

	/**
	 * @return the mPresentUrl
	 */
	public String getmPresentUrl() {
		return mPresentUrl;
	}

	/**
	 * @param mPresentUrl the mPresentUrl to set
	 */
	public void setmPresentUrl(String mPresentUrl) {
		this.mPresentUrl = mPresentUrl;
	}
	
	
}
