package com.nhatnam.android.leboncoin.parsers;

import java.util.ArrayList;

import android.text.TextUtils;


/** Contains getter and setter method for varialbles */
public class Offre {

	/** Variables */
	private String offreId;
	private String offreLink;
	private String offreThumb;
	private String offreTitle;
	private String offrePrice;
	private String offreCategory;
	private String offreLocalisation;
	private String offreDate;
	private String offreTimestamp;

	private String offreDescription;
	private String OffreAuthorName;
	private String offreAuthorEmail;
	private String offreAuthorTel;
	private String offreSendMailLink;
	private ArrayList<String> offreImages;
	
	/**
	 * Constructer by default
	 */
	public Offre() {
		this.offreImages = new ArrayList<String>();
	}
	public String getOffreId() {
		return offreId;
	}
	public void setOffreId(String offreId) {
		this.offreId = offreId;
	}
	public String getOffreLink() {
		if (TextUtils.isEmpty(offreLink))
			return "";
		return offreLink;
	}
	public void setOffreLink(String offreLink) {
		this.offreLink = offreLink;
	}
	public String getOffreThumb() {
		return offreThumb;
	}
	public void setOffreThumb(String offreThumb) {
		this.offreThumb = offreThumb;
	}
	public String getOffreTitle() {
		if (TextUtils.isEmpty(offreTitle))
			return "";
		
		return offreTitle;
	}
	public void setOffreTitle(String offreTitle) {
		this.offreTitle = offreTitle;
	}
	public String getOffrePrice() {
		if (TextUtils.isEmpty(offrePrice))
			return "";
		return offrePrice;
	}
	public void setOffrePrice(String offrePrice) {
		this.offrePrice = offrePrice;
	}
	public String getOffreCategory() {
		if (TextUtils.isEmpty(offreCategory))
			return "";
		return offreCategory;
	}
	public void setOffreCategory(String offreCategory) {
		this.offreCategory = offreCategory;
	}
	public String getOffreLocalisation() {
		if (TextUtils.isEmpty(offreLocalisation))
			return "";
		return offreLocalisation;
	}
	public void setOffreLocalisation(String offreLocalisation) {
		this.offreLocalisation = offreLocalisation;
	}
	public String getOffreDate() {
		if (TextUtils.isEmpty(offreDate))
			return "";
		return offreDate;
	}
	public void setOffreDate(String offreDate) {
		this.offreDate = offreDate;
	}
	public String getOffreTimestamp() {
		if (TextUtils.isEmpty(offreTimestamp))
			return "";
		return offreTimestamp;
	}
	public void setOffreTimestamp(String timestamp) {
		this.offreTimestamp = timestamp;
	}
	public String getOffreDescription() {
		if (TextUtils.isEmpty(offreDescription))
			return "";
		return offreDescription;
	}
	public void setOffreDescription(String offreDescription) {
		this.offreDescription = offreDescription;
	}
	public String getOffreAuthorName() {
		if (TextUtils.isEmpty(OffreAuthorName))
			return "";
		return OffreAuthorName;
	}
	public void setOffreAuthorName(String offreAuthorName) {
		OffreAuthorName = offreAuthorName;
	}
	public String getOffreAuthorEmail() {
		if (TextUtils.isEmpty(offreAuthorEmail))
			return "";
		return offreAuthorEmail;
	}
	public void setOffreAuthorEmail(String offreAuthorEmail) {
		this.offreAuthorEmail = offreAuthorEmail;
	}
	public String getOffreAuthorTel() {
		return offreAuthorTel;
	}
	public void setOffreAuthorTel(String offreAuthorTel) {
		this.offreAuthorTel = offreAuthorTel;
	}
	public String getOffreSendMailLink() {
		return offreSendMailLink;
	}
	public void setOffreSendMailLink(String offreSendMailLink) {
		this.offreSendMailLink = offreSendMailLink;
	}
	public void setOffreImages(ArrayList<String> offreImages) {
		this.offreImages = offreImages;
	}
	public ArrayList<String> getOffreImages() {
		if (this.offreImages == null) {
			return new ArrayList<String>();
		}
		return this.offreImages;
	}
}
