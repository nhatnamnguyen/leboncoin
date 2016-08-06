package com.nhatnam.android.leboncoin.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.nhatnam.android.leboncoin.R;
import com.nhatnam.android.leboncoin.activities.MainActivity;

public class WidgetProvider extends AppWidgetProvider {
	/**
	 * Used by the logger.
	 */
	protected static final transient String TAG = "WidgetSmallProvider";
	
	/* (non-Javadoc)
	 * @see android.appwidget.AppWidgetProvider#onUpdate(android.content.Context, android.appwidget.AppWidgetManager, int[])
	 */
	@Override
	public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		
		RemoteViews views = null;
		
		// Perform this loop procedure for each App Widget that belongs to this provider
		for (int idx = 0, N = appWidgetIds.length; --N >= 0; ++idx, views = null) {
			WidgetProvider.initHeader(views = new RemoteViews(context.getPackageName(), R.layout.widget_view), context);
		    
		    // Tell the AppWidgetManager to perform an update on the current app widget
		    appWidgetManager.updateAppWidget(appWidgetIds[idx], views);
		}
	}
	
	/**
	 * Retrieve a PendingIntent that will start a new activity.
	 * @param context The context of your application.
	 * @param intent Intent of the activity to be launched.
	 * @return Returns an existing or new PendingIntent matching the given parameters.
	 */
	protected static final PendingIntent createPendingIntent(final Context context, final Intent intent) {
		return PendingIntent.getActivity(context, 0, intent.setPackage(context.getPackageName()), PendingIntent.FLAG_CANCEL_CURRENT);
	}
	
	/**
	 * Create an intent used to go to the dashboard.
	 * @param context The context of your application.
	 * @return A new Intent.
	 */
	protected static final Intent createMainIntent(final Context context) {
		return new Intent(context, MainActivity.class);
	}
	
	/**
	 * Initialize the header view of the widget.
	 * @param views The RemoteViews object to show.
	 * @param context The context of your application.
	 */
	protected static final void initHeader(final RemoteViews views, final Context context) {
		views.setOnClickPendingIntent(R.id.widget_image,
	    	  						  WidgetProvider.createPendingIntent(context, WidgetProvider.createMainIntent(context)));

	}
}
