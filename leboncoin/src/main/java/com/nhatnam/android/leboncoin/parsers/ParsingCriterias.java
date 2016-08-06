package com.nhatnam.android.leboncoin.parsers;

import java.net.URL;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.nhatnam.android.leboncoin.LBC_Application;
import com.nhatnam.android.leboncoin.utils.LeboncoinConstant;

public class ParsingCriterias extends AsyncTask<Object, Object, Object> {

	Criteria criteria = null;
	
	int characters = - 1;
	public static final int CHARACTERS_PRE_PRICE 			= 0;
	public static final int CHARACTERS_PRICE 				= 1;

	
	@Override
	protected Object doInBackground(Object... params) {

		try {
			criteria = new Criteria();

			URL sourceUrl = new URL((String)params[0]);
			Log.i("lbc", "Fetch database criteria from ............: " + sourceUrl);
			
			XMLReader xmlReader = XMLReaderFactory
					.createXMLReader("org.ccil.cowan.tagsoup.Parser");
			ContentHandler handler = new DefaultHandler() {

				@Override
				public void startDocument() throws SAXException {
					super.startDocument();
				}
				
				
				@Override
				public void endDocument() throws SAXException {
					LBC_Application.getInstance().setCurrentCriteria(criteria);
				}
				
				
				@Override
				public void startElement(String uri, String localName,
						String qName, Attributes attributes)
						throws SAXException 
				{
					
					if (localName.equalsIgnoreCase("select")
							&& attributes.getIndex("class")>=0 && attributes.getValue("class").equalsIgnoreCase("featurebox")
							&& attributes.getIndex("name")>=0 && attributes.getValue("name").equalsIgnoreCase("ps"))
					{
						//Pre price
						characters = CHARACTERS_PRE_PRICE;
					} else if ((characters == CHARACTERS_PRE_PRICE || characters == CHARACTERS_PRICE ) 
							&& localName.equalsIgnoreCase("option")
							&& attributes.getIndex("value")>=0 && !TextUtils.isEmpty(attributes.getValue("value")))
					{
						//Price
						characters = CHARACTERS_PRICE;
					}
				}
				
				
				@Override
				public void characters(char[] ch, int start, int length)
						throws SAXException {
				
					if (characters>=0) {
						switch (characters) {
						case CHARACTERS_PRICE:
							//Price
							criteria.getPrices().add(new String(ch, start, length));
							break;
						default:
							break;
						}
					}
				}
				
				
				@Override
				public void endElement(String uri, String localName,
						String qName) throws SAXException 
				{
					//Reset characters
					if ((characters!=CHARACTERS_PRE_PRICE && characters!=CHARACTERS_PRICE)
								|| localName.equalsIgnoreCase("select")) {
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



	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);
		
		// Send the broadcast in order to update the UI
		final Intent intent = new Intent(LeboncoinConstant.INTENT_BROADCAST_CRITERIA_DOWNLOADED);
        intent.setPackage(LBC_Application.getInstance().getPackageName());
        intent.putExtra(LeboncoinConstant.EXTRA_RESULT, 1);
        LBC_Application.getInstance().sendBroadcast(intent);
		
	}
	
	
}