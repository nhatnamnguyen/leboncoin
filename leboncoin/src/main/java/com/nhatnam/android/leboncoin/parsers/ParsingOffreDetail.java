package com.nhatnam.android.leboncoin.parsers;

import java.net.URL;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.Log;

import com.nhatnam.android.leboncoin.LBC_Application;
import com.nhatnam.android.leboncoin.providers.OffresContract.Offres;
import com.nhatnam.android.leboncoin.utils.Utils;

public class ParsingOffreDetail extends AsyncTask<Object, Object, Object> {

	Offre offre = null;
	
	int characters = - 1;
	public static final int CHARACTERS_AUTHOR_NAME 			= 0;
	public static final int CHARACTERS_TIMESTAMP 			= 1;
	public static final int CHARACTERS_THUMB 				= 2;
	public static final int CHARACTERS_PRE_LOCALISATION		= 3;
	public static final int CHARACTERS_LOCALISATION			= 4;
	public static final int CHARACTERS_DESCRIPTION			= 5;
	public static final int CHARACTERS_AUTHOR_TEL			= 6;
	public static final int CHARACTERS_IMAGES				= 7;
	
	
	@Override
	protected Object doInBackground(Object... params) {

		try {
			offre = new Offre();
			offre.setOffreId((String)params[0]);

			URL sourceUrl = new URL((String)params[1]);
			Log.i("lbc", "Fetch database detail offre from ............: " + sourceUrl);
			
			XMLReader xmlReader = XMLReaderFactory
					.createXMLReader("org.ccil.cowan.tagsoup.Parser");
			ContentHandler handler = new DefaultHandler() {

				@Override
				public void startDocument() throws SAXException {
					// TODO Auto-generated method stub
					super.startDocument();
				}
				
				
				@Override
				public void endDocument() throws SAXException {
					//Update offre to ContentProvider
					ContentValues cv = new ContentValues();
					cv.put(Offres.AUTHOR_NAME, offre.getOffreAuthorName());
					cv.put(Offres.SEND_EMAIL_LINK, offre.getOffreSendMailLink());
					cv.put(Offres.TIMESTAMP, Utils.convertTimeToTimestamp(offre.getOffreTimestamp()));
					cv.put(Offres.AUTHOR_TEL, offre.getOffreAuthorTel());
					cv.put(Offres.DESCRIPTION, offre.getOffreDescription());
					cv.put(Offres.STATUS, Offres.OFFRE_STATUS_DOWNLOADED_DETAIL);
					
					if (offre.getOffreImages() != null && offre.getOffreImages().size()>0) {
						StringBuffer offreImages = new StringBuffer("");
						for (int i = 0; i < offre.getOffreImages().size(); i++) {
							offreImages.append(offre.getOffreImages().get(i)).append("&");
						}
						
						cv.put(Offres.IMAGES, offreImages.toString());
					}
					
					LBC_Application.getResolver().update(Offres.CONTENT_URI.buildUpon().appendEncodedPath("offres").build(), 
							cv, Offres._ID + "=?", new String[]{offre.getOffreId()});
				}
				
				
				@Override
				public void startElement(String uri, String localName,
						String qName, Attributes attributes)
						throws SAXException 
				{
					
					if (localName.equalsIgnoreCase("a")
							&& attributes.getIndex("class")>=0 && attributes.getValue("class").equalsIgnoreCase("nohistory")
							&& attributes.getIndex("href")>=0 && attributes.getValue("href").contains("sendmail")) 
					{
						//Offre author name and link to send mail
						offre.setOffreSendMailLink(attributes.getValue("href"));
						characters = CHARACTERS_AUTHOR_NAME;
					} else if (localName.equalsIgnoreCase("div")
							&& attributes.getIndex("id")>=0 && attributes.getValue("id").equalsIgnoreCase("view_date"))
					{
						//offre timestamp detail
						characters = CHARACTERS_TIMESTAMP;
					} else if (localName.equalsIgnoreCase("img")
							&& attributes.getIndex("class")>=0 && attributes.getValue("class").equalsIgnoreCase("thumbs"))
					{
						//more 1 offre images
						offre.getOffreImages().add(attributes.getValue("src").replace("thumbs", "images"));
					} 
					 else if (localName.equalsIgnoreCase("img")
							&& attributes.getIndex("id")>=0 && attributes.getValue("id").equalsIgnoreCase("main_image_1"))
					{
						//Only 1 offre images
						offre.getOffreImages().add(attributes.getValue("src"));
					}
					else if (localName.equalsIgnoreCase("span")
							&& attributes.getIndex("class")>=0 && attributes.getValue("class").equalsIgnoreCase("ad_details"))
					{
						//Pre offre localisation
						characters = CHARACTERS_PRE_LOCALISATION;
					} else if (characters == CHARACTERS_PRE_LOCALISATION
							&& localName.equalsIgnoreCase("label")
							&& attributes.getIndex("class")>=0 && attributes.getValue("class").equalsIgnoreCase("ad_details"))
					{
						
					} else if (localName.equalsIgnoreCase("p")
							&& attributes.getIndex("class")>=0 && attributes.getValue("class").equalsIgnoreCase("item_body view_details"))
					{
						//Offre description
						characters = CHARACTERS_DESCRIPTION;
					}
					else if (localName.equalsIgnoreCase("a")
							&& attributes.getIndex("class")>=0 && attributes.getValue("class").equalsIgnoreCase("nohistory adview_links")
							&& attributes.getValue("href").equalsIgnoreCase("#"))
					{
						//Offre author tel
						characters = CHARACTERS_AUTHOR_TEL;
					}
				}
				
				
				@Override
				public void characters(char[] ch, int start, int length)
						throws SAXException {
				
					if (characters>=0) {
						switch (characters) {
						case CHARACTERS_AUTHOR_NAME:
							//Author name
							offre.setOffreAuthorName(offre.getOffreTitle() + new String(ch, start, length));
							break;
						case CHARACTERS_TIMESTAMP:
							offre.setOffreTimestamp(offre.getOffreTimestamp() + new String(ch, start, length));
							break;
						case CHARACTERS_DESCRIPTION:
							offre.setOffreDescription(offre.getOffreDescription() + new String(ch, start, length));
							break;
						case CHARACTERS_AUTHOR_TEL:
							String chaine = new String(ch);
							chaine.substring(0, chaine.lastIndexOf("'"));
							
							String[] splitChaine = chaine.split("'");
							
							offre.setOffreAuthorTel(Utils.decryptTel("0" + splitChaine[0].trim(), splitChaine[2]).trim());
							break;
						default:
							break;
						}
					}
				}
				
				
				@Override
				public void endElement(String uri, String localName,
						String qName) throws SAXException {
					//Reset characters
					
					if (characters == CHARACTERS_DESCRIPTION) {
						if (localName.equalsIgnoreCase("br")) {
							offre.setOffreDescription(offre.getOffreDescription() + "\n");
						} else if (localName.equalsIgnoreCase("p")) {
							characters = -1;
						}
						
					}
					
					if (characters != CHARACTERS_PRE_LOCALISATION
							&& characters != CHARACTERS_DESCRIPTION) {
						characters = -1;
					}
				}
			};

			xmlReader.setContentHandler(handler);
			InputSource is = new InputSource(sourceUrl.openStream());
			is.setEncoding("iso-8859-1");
			xmlReader.parse(is);

		} catch (Exception e) {
			Log.e("lbc", "XML Pasing Excpetion : " + e.getMessage());
		}

		return null;
	}
}