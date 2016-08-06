package com.nhatnam.android.leboncoin.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.nhatnam.android.leboncoin.LBC_Application;
import com.nhatnam.android.leboncoin.utils.LeboncoinConstant;

public class Launcher extends SherlockFragmentActivity
{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences sharedPreferences = this.getSharedPreferences(LeboncoinConstant.PREF_SESSION, Context.MODE_PRIVATE);
        int prefDepartement = sharedPreferences.getInt(LeboncoinConstant.PREF_DEPARTEMENT, 0);
		LBC_Application.getInstance().setCurrentDepartement(prefDepartement, false);
		int prefRegion = sharedPreferences.getInt(LeboncoinConstant.PREF_REGION, 0);
		LBC_Application.getInstance().setCurrentRegion(prefRegion, false);
		int prefCategory = sharedPreferences.getInt(LeboncoinConstant.PREF_CATEGORY, 0);
		LBC_Application.getInstance().setCurrentCategory(prefCategory, false);
		
		Intent intent = null;
		if (prefDepartement>0 || prefRegion>0) {
			intent = new Intent(this, MainActivity.class);
		} else {
			intent = new Intent(this, CarteFranceActivity.class);
		}
		
		startActivity(intent);
		
		finish();
	}
}
