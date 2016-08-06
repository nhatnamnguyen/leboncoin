package com.nhatnam.android.leboncoin.providers;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * This class defines columns and URIs used in the leboncoin offres
 */
public class OffresContract
{
  public static final String        OFFRES      		= "offres";
  public static final String 		OFFRES_VIEW 		= "offres_view";
  
  /**
   * The root authority for the OffreProvider
   */
  public static final String   		LEBONCOIN_AUTHORITY    =  "com.nhatnam.android.leboncoin.provider";
  
  /**
   * Tweets table
   */
  public static final class Offres implements BaseColumns
  {
    /**
     * The root content uri
     */
    public static final Uri    CONTENT_URI              = Uri.parse("content://"
                                                                    + LEBONCOIN_AUTHORITY);

    public static final String THUMB                      		= "thumb";
    public static final String IMAGES                      		= "images";
    public static final String LINK                      		= "link";
    public static final String TITLE                      		= "title";
    public static final String PRICE                      		= "price";
    public static final String CATEGORY                      	= "category";
    public static final String LOCALISATION                     = "localisation";
    public static final String DATE                      		= "date";
    public static final String TIMESTAMP                      	= "timestamp";
    public static final String TIMEDOWNLOAD                     = "timedownload";
    public static final String DESCRIPTION                      = "description";
    public static final String AUTHOR_NAME                      = "author_name";
    public static final String AUTHOR_EMAIL                     = "author_email";
    public static final String SEND_EMAIL_LINK                  = "send_email_link";
    public static final String AUTHOR_TEL                      	= "author_tel";
    public static final String STATUS                      		= "status";

    public static final int OFFRE_STATUS_UNKNOWN                = 0x01;
    public static final int OFFRE_STATUS_DOWNLOADED_HEADER		= 0x02;
    public static final int OFFRE_STATUS_DOWNLOADED_DETAIL		= 0x04;
    public static final int OFFRE_STATUS_FAVORITED				= 0x08;
    
    
    /**
     * The default sort order for this table - most recently modified first
     */
    public static final String DEFAULT_SORT_ORDER       		= TIMEDOWNLOAD + " ASC";
  }


  /**
   * Convenience definition for the projection we will use to retrieve columns from the message
   */
  public static final String[] IT5_TWEET_PROJECTION      = { 	Offres._ID, Offres.THUMB,  Offres.IMAGES, Offres.LINK, Offres.TITLE,
	  															Offres.PRICE, Offres.CATEGORY, Offres.LOCALISATION,
	  															Offres.DESCRIPTION, Offres.AUTHOR_NAME, Offres.SEND_EMAIL_LINK,
	  															Offres.AUTHOR_EMAIL, Offres.AUTHOR_TEL,
	  															Offres.DATE, Offres.TIMESTAMP, Offres.TIMEDOWNLOAD, Offres.STATUS};

}
