package com.nhatnam.android.leboncoin.parsers;

import java.net.URL;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import com.nhatnam.android.leboncoin.LBC_Application;
import com.nhatnam.android.leboncoin.providers.OffresContract.Offres;
import com.nhatnam.android.leboncoin.utils.LeboncoinConstant;

public class ParsingOffresList extends AsyncTask<Object, Object, Object> {

	ArrayList<Offre> offresList = new ArrayList<Offre>(20);
	Offre offre = null;
	CategoryDepartement categoryLocalisation = null;
	private String stringPageTotal;
	
	int characters = - 1;
	public static final int CHARACTERS_LINK 					= 0;
	public static final int CHARACTERS_TITLE 					= 1;
	public static final int CHARACTERS_PRICE 					= 2;
	public static final int CHARACTERS_CATEGORY 				= 3;
	public static final int CHARACTERS_LOCALISATION 			= 4;
	public static final int CHARACTERS_PRE_DATE 				= 5;
	public static final int CHARACTERS_DATE 					= 6;
	public static final int CHARACTERS_PRE_CATEGORY_LIST		= 7;
	public static final int CHARACTERS_CATEGORY_LIST			= 8;
	public static final int CHARACTERS_PRE_LOCALISATION_LIST	= 9;
	public static final int CHARACTERS_LOCALISATION_LIST		= 10;
	public static final int CHARACTERS_PAGE_TOTAL				= 11;
	
	
	@Override
	protected Object doInBackground(Object... params) {

		try {
			URL sourceUrl = new URL((String)params[0]);
			
			Log.i("lbc", "Fetch database from ............: " + sourceUrl);
			
			XMLReader xmlReader = XMLReaderFactory
					.createXMLReader("org.ccil.cowan.tagsoup.Parser");
			ContentHandler handler = new DefaultHandler() {

				@Override
				public void startDocument() throws SAXException {
					// TODO Auto-generated method stub
					super.startDocument();
					offresList = new ArrayList<Offre>(20);
					stringPageTotal = "";
				}
				
				
				
				@Override
				public void endDocument() throws SAXException {
				
					//Insert offreList to ContentProvider
					Log.i("NNA", "offres list" + offresList);
					ContentValues cv;
					Offre offre;
					for (int i = 0; i < offresList.size(); i++) {
						offre = offresList.get(i);
						
						cv = new ContentValues();
						cv.put(Offres._ID, offre.getOffreId());
						cv.put(Offres.THUMB, offre.getOffreThumb());
						cv.put(Offres.LINK, offre.getOffreLink());
						cv.put(Offres.TITLE, offre.getOffreTitle());
						cv.put(Offres.PRICE, offre.getOffrePrice());
						cv.put(Offres.CATEGORY, offre.getOffreCategory());
						cv.put(Offres.LOCALISATION, offre.getOffreLocalisation());
						cv.put(Offres.DATE, offre.getOffreDate());
						cv.put(Offres.TIMEDOWNLOAD, System.currentTimeMillis());
						try {
							cv.put(Offres.STATUS, Offres.OFFRE_STATUS_DOWNLOADED_HEADER);
							LBC_Application.getResolver().insert(Offres.CONTENT_URI.buildUpon().appendEncodedPath("offres").build(), cv);
						} catch (Exception e) {
							Log.e("lbc", "This offre is ready loaded ......" + cv.getAsString(Offres._ID) + " --> Just update it");
							
							LBC_Application.getResolver().update(Offres.CONTENT_URI.buildUpon().
									appendEncodedPath("offres").appendEncodedPath(offre.getOffreId()).build(), 
									cv, null, null);
						}
					}
					
					// Send the broadcast in order to update the UI
					final Intent intent = new Intent(LeboncoinConstant.INTENT_BROADCAST_DOWNLOADED);
                    intent.setPackage(LBC_Application.getInstance().getPackageName());
                    intent.putExtra(LeboncoinConstant.EXTRA_RESULT, 1);
                    LBC_Application.getInstance().sendBroadcast(intent);
				}
				
				
				@Override
				public void startElement(String uri, String localName,
						String qName, Attributes attributes)
						throws SAXException 
				{
					
					if (localName.equalsIgnoreCase("select")
							&& attributes.getIndex("id")>=0 && attributes.getValue("id").equalsIgnoreCase("change_category")
							&& LBC_Application.getInstance().getCategoryList()==null)
					{
						//Begin category list
						characters = CHARACTERS_PRE_CATEGORY_LIST;
						
						//Init arrayList category
						LBC_Application.getInstance().setCategoryList(new ArrayList<CategoryDepartement>());
					} else if (localName.equalsIgnoreCase("select")
							&& attributes.getIndex("id")>=0 && attributes.getValue("id").equalsIgnoreCase("change_area")
							&& LBC_Application.getInstance().getDepartementList()==null)
					{
						//Begin localisation list
						characters = CHARACTERS_PRE_LOCALISATION_LIST;
						
						//Init arrayList localisation
						LBC_Application.getInstance().setDepartementList(new ArrayList<CategoryDepartement>());
					} else if (localName.equalsIgnoreCase("option")
							&& (characters == CHARACTERS_PRE_CATEGORY_LIST || characters == CHARACTERS_CATEGORY_LIST
								|| characters == CHARACTERS_PRE_LOCALISATION_LIST || characters == CHARACTERS_LOCALISATION_LIST))
					{
						if (characters == CHARACTERS_PRE_CATEGORY_LIST || characters == CHARACTERS_CATEGORY_LIST) {
							characters = CHARACTERS_CATEGORY_LIST;
						} else {
							characters = CHARACTERS_LOCALISATION_LIST;
						}
						categoryLocalisation = new CategoryDepartement();
						categoryLocalisation.setValue(Integer.parseInt(attributes.getValue("value")));
					} else if (localName.equalsIgnoreCase("tr")
							&& attributes.getIndex("id")>=0 && attributes.getValue("id").startsWith("view_")) 
					{
						//Begin offre
						offre = new Offre();
					} else if (localName.equalsIgnoreCase("a")
							&& attributes.getIndex("id")>=0 && attributes.getValue("id").startsWith("view_link_")
							&& attributes.getIndex("class")>=0 && attributes.getValue("class").equalsIgnoreCase("nohistory"))
					{
						//offre link
						offre.setOffreLink(Html.fromHtml(attributes.getValue("href")).toString());
					} else if (localName.equalsIgnoreCase("img")
						&& attributes.getIndex("class")>=0 && attributes.getValue("class").equalsIgnoreCase("image_thumb"))
					{
						//image thumb
						offre.setOffreThumb(attributes.getValue("src").trim());
					} else if (localName.equalsIgnoreCase("div")
							&& attributes.getIndex("id")>=0 && attributes.getValue("id").startsWith("item_")) {
						//offre id
						offre.setOffreId(attributes.getValue("id").substring(5).trim());
					} else if (localName.equalsIgnoreCase("h2")
							&& attributes.getIndex("class")>=0 && attributes.getValue("class").equalsIgnoreCase("item_title")) {
						//titre
						characters = CHARACTERS_TITLE;
					} else if (localName.equalsIgnoreCase("div")
							&& attributes.getIndex("class")>=0 && attributes.getValue("class").equalsIgnoreCase("item_price")) {
						//price
						characters = CHARACTERS_PRICE;
					} else if (localName.equalsIgnoreCase("div")
							&& attributes.getIndex("class")>=0 && attributes.getValue("class").equalsIgnoreCase("item_category")) {
						//category
						characters = CHARACTERS_CATEGORY;
					} else if (localName.equalsIgnoreCase("div")
							&& attributes.getIndex("class")>=0 && attributes.getValue("class").equalsIgnoreCase("item_localization")) {
						//localization
						characters = CHARACTERS_LOCALISATION;
					} else if (localName.equalsIgnoreCase("div")
							&& attributes.getIndex("class")>=0 && attributes.getValue("class").equalsIgnoreCase("item_date")) {
						//Begin of Offre Date
						characters = CHARACTERS_PRE_DATE;
					} else if (localName.equalsIgnoreCase("span")
							&& characters == CHARACTERS_PRE_DATE) {
						//Offre Date
						characters = CHARACTERS_DATE;
					} else if (localName.equalsIgnoreCase("div")
							&& attributes.getIndex("class")>=0 && attributes.getValue("class").equalsIgnoreCase("page_nav_number")
							&& TextUtils.isEmpty(stringPageTotal)) {
						//Page total
						characters = CHARACTERS_PAGE_TOTAL;
					}
				}
				
				
				@Override
				public void characters(char[] ch, int start, int length)
						throws SAXException {
				
					if (characters>=0) {
						switch (characters) {
						case CHARACTERS_CATEGORY_LIST:
						case CHARACTERS_LOCALISATION_LIST:
							categoryLocalisation.setText(categoryLocalisation.getText() +  new String(ch, start, length));
							break;
						case CHARACTERS_TITLE:
							//Title
							offre.setOffreTitle(offre.getOffreTitle() + new String(ch, start, length));
							break;
						case CHARACTERS_PRICE:
							//Price
							offre.setOffrePrice(offre.getOffrePrice() + new String(ch, start, length));
							break;
						case CHARACTERS_CATEGORY:
							//Category
							offre.setOffreCategory(offre.getOffreCategory() + new String(ch, start, length));
							break;
						case CHARACTERS_LOCALISATION:
							//Localization
							offre.setOffreLocalisation(offre.getOffreLocalisation() + new String(ch, start, length).trim());
							break;
						case CHARACTERS_DATE:
							//Date
							offre.setOffreDate(offre.getOffreDate() + new String(ch, start, length));
							break;
						case CHARACTERS_PAGE_TOTAL:
							//Page total
							stringPageTotal = stringPageTotal + new String(ch, start, length).trim();
							break;
						default:
							break;
						}
					}
				}
				
				
				@Override
				public void endElement(String uri, String localName,
						String qName) throws SAXException {
					
					if (characters == CHARACTERS_DATE) {
						//Fin offre
						offresList.add(offre);
						
						if (offresList.size() == 20) {
							//TODO Stop parsing
						}
					} else if (characters == CHARACTERS_CATEGORY_LIST
							&& categoryLocalisation!=null) {
						LBC_Application.getInstance().getCategoryList().add(categoryLocalisation);
						categoryLocalisation = null;
					} else if (characters == CHARACTERS_LOCALISATION_LIST
							&& categoryLocalisation!=null) {
						LBC_Application.getInstance().getDepartementList().add(categoryLocalisation);
						categoryLocalisation = null;
					}
					
					if (characters == CHARACTERS_PAGE_TOTAL) {
						LBC_Application.getInstance().setCurrentPageTotal(
								Integer.parseInt(stringPageTotal.substring(stringPageTotal.indexOf('/')+1).trim().replace("\u00A0", "").trim()));
						
					}
					
					//Reset characters
					if (characters!=CHARACTERS_PRE_DATE
							&& ((characters!=CHARACTERS_PRE_CATEGORY_LIST && characters!=CHARACTERS_CATEGORY_LIST
									&& characters!=CHARACTERS_PRE_LOCALISATION_LIST && characters!=CHARACTERS_LOCALISATION_LIST) 
								|| localName.equalsIgnoreCase("select"))) {
						characters = -1;
					}
				}
			};

			xmlReader.setContentHandler(handler);
			InputSource is = new InputSource(sourceUrl.openStream());
			is.setEncoding("iso-8859-1");
			xmlReader.parse(is);

		} catch (Exception e) {
			Log.e("lbc", "XML Pasing Excpetion" + e.getMessage());
		}

		return null;
	}
	
	
	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);
	}

	@Override
	protected void onProgressUpdate(Object... values) {
		super.onProgressUpdate(values);
	}
	
}