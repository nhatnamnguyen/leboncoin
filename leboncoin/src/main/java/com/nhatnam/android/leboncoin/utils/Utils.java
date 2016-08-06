package com.nhatnam.android.leboncoin.utils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.nhatnam.android.leboncoin.LBC_Application;
import com.nhatnam.android.leboncoin.parsers.Criteria;
import com.nhatnam.android.leboncoin.parsers.ParsingOffresList;
import com.nhatnam.android.leboncoin.providers.OffresContract.Offres;

public class Utils {
	
	
	/**
	 * Purge all offre 
	 * @param alsoFavorite: purge also favorite offres
	 */
	public static void purgeOffres(final boolean alsoFavorite) {
		//Purge all offres exception Favorite offre
    	LBC_Application.getResolver().delete(Offres.CONTENT_URI.buildUpon().appendPath("offres").build(), 
    			alsoFavorite?null:"(" + Offres.STATUS + "&" + Offres.OFFRE_STATUS_FAVORITED + ") = 0", null);
	}
	
	public static String  decryptTel(String cryptTel, String key) {
	    String  tel = "tel:";
	    String[] elem = cryptTel.split("0x");
	    for(int i=1, j=0; i<elem.length; i++,j++){
	        int dec_string= Integer.parseInt(elem[i],16);
	        if(j>key.length()){
	            j=0;
	        }
	        tel += Integer.parseInt(String.valueOf(dec_string^key.charAt(j))) - 48;
	    }
	    return tel;
	}
	
	
	
	/**
	 * Purge all current data
	 * 
	 */
	public static void purgeCurrentSearchData() {
		
		//Reset current search plus
		LBC_Application.getInstance().setCurrentSearchKey("");
		LBC_Application.getInstance().setCurrentSearchInTitle(false);
		LBC_Application.getInstance().setCurrentSearchUrgent(false);
		LBC_Application.getInstance().setCurrentParticulierOrProfessionel("a");
		LBC_Application.getInstance().setCurrentSearchZipCode("");

		//TODO
	}
	
	
	
	/**
	 * Reload offres list
	 */
	public static void reloadOffresList(final Criteria criteria) {
		//Purge all offres exception favorite offres
		Utils.purgeOffres(false);
		
		// Refresh
		ParsingOffresList parsingOffreSax = new ParsingOffresList();
		
		String urlRequest = LBC_Application.getInstance().getCurrentRequestOffresUri();
		if (criteria!=null) {
			urlRequest += "&ps=";
			if (criteria.priceMin > 0) {
				 urlRequest += String.valueOf(criteria.priceMin);
			}
			urlRequest += "&pe=";
			if (criteria.priceMax > 0) {
				urlRequest += String.valueOf(criteria.priceMax);
			}
			urlRequest += "&rs=" + String.valueOf(criteria.yearMin);
			urlRequest += "&re=" + String.valueOf(criteria.yearMax);
			
			urlRequest += "&fu=";
			if (criteria.energie>0) {
				urlRequest += String.valueOf(criteria.energie);
			}
			urlRequest += "&gb=";
			if (criteria.boiteVitesse>0) {
				urlRequest += String.valueOf(criteria.boiteVitesse);
			}
		}
		parsingOffreSax.execute(urlRequest);
	
		//Reset current page
		LBC_Application.getInstance().resetCurrentPageNumber();
	}
	
	
	/**
	 * Convert timeString to time stamp
	 *
	 * Le 14/09/2012 à 18h18
	 */
	public static long convertTimeToTimestamp(String timeString) {

        Calendar c = Calendar.getInstance();
        
        String date[] = TextUtils.split(timeString.substring(timeString.indexOf("Le") + 3, timeString.indexOf("à")).trim(), "/");
        if (date!=null && date.length==3) {
        	c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[0])-1);
        	c.set(Calendar.MONTH, Integer.parseInt(date[1])-1);
        	c.set(Calendar.YEAR, Integer.parseInt(date[2]));
        } else {
        	Log.e("lbc", "Error when convert timeString to Timestamp for YEAR, MONTH, DAY_OF_MONTH");
        }

        String time[] = TextUtils.split(timeString.substring(timeString.indexOf("à") + 1).trim(), "h");
        if (time!=null && time.length==2) {
        	c.set(Calendar.HOUR, Integer.parseInt(time[0]));
        	c.set(Calendar.MINUTE, Integer.parseInt(time[1]));
        } else  {
        	Log.e("lbc", "Error when convert timeString to Timestamp for HOUR, MINUTE");
        }
        
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        		
        return c.getTimeInMillis();
    }
	
	
	/**
	 * Reduce the bitmap and then setImage to imageView
	 * @param imageView
	 * @param uri
	 */
	public static void applyBitmapForImageViewFromUri(Context context, ImageView imageView, final Uri uri) {
		
	    InputStream inputStream = null;
		try {
			inputStream = context.getContentResolver().openInputStream(uri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.e("lbc", "Error when applyBitmapForImageViewFromUri :" + uri);
			return;
		}
		
		final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    // Calculate inSampleSize
	    options.inSampleSize = Utils.calculateInSampleSize(options, 200, 200);
	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    
	    Bitmap bitmapSample = BitmapFactory.decodeStream(inputStream, null, options);
		
		imageView.setImageBitmap(bitmapSample);
	}
	
	
	
	private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {
        if (width > height) {
            inSampleSize = Math.round((float)height / (float)reqHeight);
        } else {
            inSampleSize = Math.round((float)width / (float)reqWidth);
        }
    }
    return inSampleSize;
}
}
