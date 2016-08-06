package com.nhatnam.android.leboncoin.activities;

import java.io.IOException;
import java.util.List;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.nhatnam.android.leboncoin.LBC_Application;
import com.nhatnam.android.leboncoin.R;
import com.nhatnam.android.leboncoin.utils.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class CarteFranceActivity extends SherlockFragmentActivity implements OnClickListener, OnTouchListener, LocationListener
{
	private ImageView mCarteFrance, mCarteDataFrance, mCarteSelectedRegion;
	private TextView mSelectedRegion;
	private int selectedRegionId;
	private Button mRegionOk;
	
	private LocationManager locationManager;
	private String provider;
	private double latitude, longitude;
	private boolean triggedFromMenu = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (this.getIntent().getExtras()!=null) {
			this.triggedFromMenu = this.getIntent().getExtras().getBoolean("fromMenu", false);
		}
		
		setContentView(R.layout.carte_france);
	
		this.mCarteFrance = (ImageView) findViewById(R.id.map_all_regions);
		this.mCarteDataFrance = (ImageView) findViewById(R.id.map_all_data_regions);
		this.mCarteSelectedRegion = (ImageView) findViewById(R.id.map_selected_region);
		this.mCarteFrance.setOnTouchListener(this);
		this.mSelectedRegion = (TextView) findViewById(R.id.selected_region);
		this.mRegionOk = (Button) findViewById(R.id.region_ok);
		this.mRegionOk.setOnClickListener(this);
		
	}
	

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		
		int x = (int) event.getX();
		int y = (int) event.getY();
		
		Bitmap bitmap = ((BitmapDrawable) (this.mCarteDataFrance).getDrawable()).getBitmap();
		Log.i("lbc", " x:" + x + "  y:" + y);
		if (x>=bitmap.getWidth() || y>=bitmap.getHeight())	return true;
		Display display = getWindowManager().getDefaultDisplay();
		
		Log.i("lbc", "Pixel  x:" +  (x*410)/display.getWidth() + "  y:" + (y*480)/display.getHeight());
        int pixel = bitmap.getPixel((x*410)/display.getWidth(), (y*480)/display.getHeight());
        int redValue = Color.red(pixel);
		
        
        if (redValue != 0) {
        	setSelectedRegion(redValue);
        }
        
		return true;
	}
	
	
	@Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Log.i("lbc", "...................Check my location latitude: " + latitude + " - longitude: " + longitude);
        
        Geocoder geoCoder = new Geocoder(this);
		try {
			List<Address> listAdresses = geoCoder.getFromLocation(latitude, longitude, 1);
			String regionName = listAdresses.get(0).getAdminArea();
			String regionNameGoogle[] = this.getResources().getStringArray(R.array.region_google_array);
			int i = regionNameGoogle.length;
			for (; --i >= 0; ) {
				if (regionNameGoogle[i].equalsIgnoreCase(regionName))
					break;
			}
			
			if (i<0) {
				//Tout la France
				
			} else {
				setSelectedRegion(i);
			}
			
			//Remove the listener update for location
			locationManager.removeUpdates(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

	@Override
	public void onStatusChanged(String provider, int status,
			Bundle extras) {
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		
	}
	
	
	@Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        getSupportMenuInflater().inflate(R.menu.menu_carte_france, menu);
        return true;
    }
	
	
	@Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_my_location:
            	Log.i("lbc", "...................Check my location");
            	locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); 
            	boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            	if (!enabled) {
            	  Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            	  startActivity(intent);
            	}
            	
            	// Define the criteria how to select the location provider -> usedefault
                Criteria criteria = new Criteria();
                provider = locationManager.getBestProvider(criteria, false);
                Location location = locationManager.getLastKnownLocation(provider);
                
        		if (location != null) {
        			Log.i("lbc", "Provider " + provider + " has been selected.");
        			onLocationChanged(location);
        		} else {
        			Log.i("lbc", "Provider locationManager is not available");
        			locationManager.requestLocationUpdates(provider, 400, 1, this);
        		}
            	
            	return true;
            default:
                return false;
        }
    }


	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.region_ok:
			  
			//Set the region after click
			LBC_Application.getInstance().setCurrentRegion(selectedRegionId, true);
			
			if (!triggedFromMenu) {
				//Start mainActivity
				final Intent intent = new Intent(this, MainActivity.class);
				this.startActivity(intent);
			} else {
				//Reset la liste de departements
				LBC_Application.getInstance().resetDepartementList();
				
				//Load offres
				Utils.reloadOffresList(null);
			}
			
			//Finish the carteFranceActivity
			finish();
			break;

		default:
			break;
		}
	}
	
	
	private void setSelectedRegion(final int regionId) {
		int idDrawableSelectedRegion;
    	switch (regionId) {
		case 1:
			idDrawableSelectedRegion = R.drawable.map_1;
			break;
		case 2:
			idDrawableSelectedRegion = R.drawable.map_2;
			break;
		case 3:
			idDrawableSelectedRegion = R.drawable.map_3;
			break;
		case 4:
			idDrawableSelectedRegion = R.drawable.map_4;
			break;
		case 5:
			idDrawableSelectedRegion = R.drawable.map_5;
			break;
		case 6:
			idDrawableSelectedRegion = R.drawable.map_6;
			break;
		case 7:
			idDrawableSelectedRegion = R.drawable.map_7;
			break;
		case 8:
			idDrawableSelectedRegion = R.drawable.map_8;
			break;
		case 9:
			idDrawableSelectedRegion = R.drawable.map_9;
			break;
		case 10:
			idDrawableSelectedRegion = R.drawable.map_10;
			break;
		case 11:
			idDrawableSelectedRegion = R.drawable.map_11;
			break;
		case 12:
			idDrawableSelectedRegion = R.drawable.map_12;
			break;
		case 13:
			idDrawableSelectedRegion = R.drawable.map_13;
			break;
		case 14:
			idDrawableSelectedRegion = R.drawable.map_14;
			break;
		case 15:
			idDrawableSelectedRegion = R.drawable.map_15;
			break;
		case 16:
			idDrawableSelectedRegion = R.drawable.map_16;
			break;
		case 17:
			idDrawableSelectedRegion = R.drawable.map_17;
			break;
		case 18:
			idDrawableSelectedRegion = R.drawable.map_18;
			break;
		case 19:
			idDrawableSelectedRegion = R.drawable.map_19;
			break;
		case 20:
			idDrawableSelectedRegion = R.drawable.map_20;
			break;
		case 21:
			idDrawableSelectedRegion = R.drawable.map_21;
			break;
		case 22:
			idDrawableSelectedRegion = R.drawable.map_22;
			break;
		case 23:
			idDrawableSelectedRegion = R.drawable.map_23;
			break;
		case 24:
			idDrawableSelectedRegion = R.drawable.map_24;
			break;
		case 25:
			idDrawableSelectedRegion = R.drawable.map_25;
			break;
		default:
			idDrawableSelectedRegion = R.drawable.map_26;
			break;
		}
    	
    	this.mCarteSelectedRegion.setImageDrawable(this.getResources().getDrawable(idDrawableSelectedRegion));
    	this.selectedRegionId = regionId;
    	String selectedRetionName = this.getResources().getStringArray(R.array.region_array)[regionId];
    	Log.i("lbc", "..............Selected region : " + selectedRetionName);
    	
    	this.mSelectedRegion.setText(selectedRetionName);
    	this.mRegionOk.setEnabled(true);
		
	}
}
