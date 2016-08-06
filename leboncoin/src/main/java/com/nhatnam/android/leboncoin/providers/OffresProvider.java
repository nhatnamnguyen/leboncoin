package com.nhatnam.android.leboncoin.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.nhatnam.android.leboncoin.providers.OffresContract.Offres;

public class OffresProvider extends ContentProvider {

	private static final String TAG = "OffreProvider";
	private DatabaseHelper dbHelper;
	
	/**
	 * Database file name
	 */
	private static final String DATABASE_NAME = "leboncoin.db";
	private static final int DATABASE_VERSION = 1;
	
	/**
	 * Matcher Uris
	 */
	private static final UriMatcher URI_MATCHER;
	
	private static final int OFFRES 		= 1;
	private static final int OFFRE 			= 2;
	private static final int RECHERCHES 	= 5;
	private static final int RECHERCHE 		= 6;
	private static final int DEPOSES 		= 7;
	private static final int DEPOSE 		= 8;
	
	/**
	 * This inner DatabaseHelper class defines methods to create and upgrade the
	 * database from previous versions.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(final Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(final SQLiteDatabase db) {
			// Create TWEETS table
			db.execSQL("CREATE TABLE " + OffresContract.OFFRES + " ("
					+ Offres._ID 				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ Offres.THUMB 				+ " TEXT,"
					+ Offres.IMAGES 			+ " TEXT,"
					+ Offres.LINK  				+ " TEXT,"
					+ Offres.TITLE  			+ " TEXT,"
					+ Offres.PRICE  			+ " TEXT,"
					+ Offres.CATEGORY  			+ " TEXT,"
					+ Offres.LOCALISATION  		+ " TEXT,"
					+ Offres.DESCRIPTION 		+ " TEXT,"
					+ Offres.AUTHOR_NAME  		+ " TEXT,"
					+ Offres.AUTHOR_EMAIL  		+ " TEXT,"
					+ Offres.AUTHOR_TEL  		+ " TEXT,"
					+ Offres.SEND_EMAIL_LINK    + " TEXT,"
					+ Offres.DATE 				+ " TEXT,"
					+ Offres.TIMESTAMP 			+ " LONG,"
					+ Offres.TIMEDOWNLOAD 		+ " LONG,"
					+ Offres.STATUS  			+ " INTEGER)");
			
			try {
				db.rawQuery("PRAGMA journal_mode=OFF;PRAGMA temp_store=MEMORY;PRAGMA locking_mode=NORMAL;PRAGMA synchronous=NORMAL;",
						null).close();
			} catch (final Throwable ex) {
				Log.w(OffresProvider.TAG, ex);
			}
		}

		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
				final int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			
			//Drop all tables
			db.execSQL("DROP TABLE IF EXISTS " + OffresContract.OFFRES);
			
			
			//Drop all triggers
			
			onCreate(db);
		}
	}

	@Override
	public boolean onCreate() {
		// On the creation of the content provider, open the database,
		// the Database helper will create a new version of the database
		// if needed through the functionality in SQLiteOpenHelper
		this.dbHelper = new DatabaseHelper(getContext());

		return true;
	}

	@Override
	public String getType(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
			case OFFRES :
				return "vnd.android.cursor.dir/vnd.nhatnam.leboncoin.offres";
			case OFFRE :
				return "vnd.android.cursor.item/vnd.nhatnam.leboncoin.offre";
			default :
				// any other kind of URL is illegal
				throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		final long rowID;
	    ContentValues values;
	    if (initialValues != null) {
	    	values = new ContentValues(initialValues);
	    }
	    else {
	    	values = new ContentValues();
	    }

	    final int match = URI_MATCHER.match(uri);
	    final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
	    
		switch (match) {
			case OFFRES :
				rowID = db.insert(OffresContract.OFFRES, Offres._ID, values);
				if (rowID > 0) {
					final Uri newUri = ContentUris.withAppendedId(Offres.CONTENT_URI.buildUpon()
							.appendPath("offres").build(), rowID);
					getContext().getContentResolver().notifyChange(uri, null);
					return newUri;
				}
				break;
			default :
				throw new IllegalArgumentException("Unknown URL " + uri);
		}
	    throw new SQLException("Failed to insert row into " + uri);
	}
	
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
	    int count;
	    final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		
	    switch (URI_MATCHER.match(uri)) {
	    	case OFFRES :
			count = db.delete(OffresContract.OFFRES, where, whereArgs);
			
			getContext().getContentResolver().notifyChange(uri, null);
			break;
			case OFFRE :
				count = db.delete(OffresContract.OFFRES, Offres._ID + "= '" + uri.getLastPathSegment(), null);
				
				getContext().getContentResolver().notifyChange(uri, null);
				break;
			default :
				throw new IllegalArgumentException("Unknown URL " + uri);
		}
		return count;
	}
	
	
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] whereArgs) {
		int count;
		final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		
		switch (URI_MATCHER.match(uri)) {
			case OFFRES :
				//TODO Test
				count = db.update(OffresContract.OFFRES, values, selection, whereArgs);
				getContext().getContentResolver().notifyChange(uri, null);
				break;
			case OFFRE :
				count = db.update(OffresContract.OFFRES, values, Offres._ID + "= '" + uri.getLastPathSegment() + "'"
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), whereArgs);
				getContext().getContentResolver().notifyChange(uri, null);
				break;
			default :
				throw new IllegalArgumentException("Unknown URL " + uri);
		}
		return count;
	}
	

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sort) {
		// Query the database using the arguments provided
	    final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

	    final SQLiteDatabase mDataBase = this.dbHelper.getReadableDatabase();
	    Cursor cursor = null;

	    final int match = URI_MATCHER.match(uri);
		switch (match) {
			case OFFRES :
				queryBuilder.setTables(OffresContract.OFFRES);
				cursor = queryBuilder.query(mDataBase, projection, selection, selectionArgs,
								null, null, sort != null ? sort : Offres.DEFAULT_SORT_ORDER);

				cursor.setNotificationUri(getContext().getContentResolver(), uri);
				break;
			case OFFRE :
				queryBuilder.setTables(OffresContract.OFFRES);
				cursor = queryBuilder.query(mDataBase, projection, Offres._ID + "= '" + uri.getLastPathSegment() + "'"
								+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs,
								null, null, sort != null ? sort : Offres.DEFAULT_SORT_ORDER);
						
				cursor.setNotificationUri(getContext().getContentResolver(), uri);
				break;
			default :
				// anything else is considered and illegal request
				throw new IllegalArgumentException("Unknown URL " + uri);
		}
	    return cursor;
	}
	
	
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

		/*********** Please don't move the order, it's important ************/
		
		/**
		 * Match all offres
		 */
		URI_MATCHER.addURI(OffresContract.LEBONCOIN_AUTHORITY, "offres", OFFRES);
		
		/**
		 * Match an offre
		 */
		URI_MATCHER.addURI(OffresContract.LEBONCOIN_AUTHORITY, "offres/*", OFFRE);
		
	}
}
