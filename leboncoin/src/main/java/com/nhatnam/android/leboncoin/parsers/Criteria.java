package com.nhatnam.android.leboncoin.parsers;

import java.util.ArrayList;



/** Contains getter and setter method for varialbles */
public class Criteria {

	private ArrayList<String> prices;
	public int priceMin, priceMax, energie, boiteVitesse, parOrPro;
	public String yearMin, yearMax, kmMin, kmMax, zipCode;
	
	/**
	 * Constructer by default
	 */
	public Criteria() {
		this.prices = new ArrayList<String>();
		yearMin = "";
		yearMax = "";
		kmMin = "";
		kmMax = "";
	}
	
	public void setPrices(ArrayList<String> prices) {
		this.prices = prices;
	}
	
	public ArrayList<String> getPrices() {
		if (this.prices==null)
			return new ArrayList<String>();
		return this.prices;
	}
}
