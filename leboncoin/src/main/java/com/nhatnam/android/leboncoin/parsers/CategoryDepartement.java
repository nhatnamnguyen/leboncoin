package com.nhatnam.android.leboncoin.parsers;

import android.text.TextUtils;


public class CategoryDepartement {

	private String mText;
	private int mValue;
	

	/**
	 * Empty constructor
	 */
	public CategoryDepartement() {
	
	}
	
	public CategoryDepartement(final String text, final int value) {
		this.mText = text;
		this.mValue = value;
	}
	
	
	
	public String getText() {
		if (TextUtils.isEmpty(mText))
			return "";
		return mText;
	}


	public void setText(String mText) {
		this.mText = mText;
	}


	public int getValue() {
		return mValue;
	}


	public void setValue(int mValue) {
		this.mValue = mValue;
	}
}
