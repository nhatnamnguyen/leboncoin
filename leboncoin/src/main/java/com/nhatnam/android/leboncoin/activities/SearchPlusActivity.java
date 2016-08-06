package com.nhatnam.android.leboncoin.activities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.nhatnam.android.leboncoin.LBC_Application;
import com.nhatnam.android.leboncoin.R;
import com.nhatnam.android.leboncoin.parsers.Criteria;
import com.nhatnam.android.leboncoin.utils.Utils;


public class SearchPlusActivity extends SherlockFragmentActivity implements OnClickListener
{
	private Criteria criteria;
	private EditText mSearchKey, mSearchZipCode;
	private CheckBox mSearchTitleOnly, mSearchUrgentOnly;
	private Spinner mSearchPriceMin, mSearchPriceMax, mSearchParPro, mSearchKmMin, mSearchKmMax, mSearchYearMin, mSearchYearMax, mSearchEnergie, mSearchBoiteVitesse;
   
	public final int MIN_YEAR = 1960;
	
    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_plus_activity);
		
        String temp;
        this.mSearchKey = (EditText) this.findViewById(R.id.search_key);
        this.mSearchKey.setText(temp = LBC_Application.getInstance().getCurrentSearchKey());
        this.mSearchKey.setSelection(temp.length());
        this.mSearchTitleOnly = (CheckBox) this.findViewById(R.id.search_in_title);
        this.mSearchTitleOnly.setChecked(LBC_Application.getInstance().getCurrentSearchInTitle());
        this.mSearchUrgentOnly = (CheckBox) this.findViewById(R.id.search_urgent);
        this.mSearchUrgentOnly.setChecked(LBC_Application.getInstance().getCurrentSearchUrgent());
        
//        this.mSearchPriceMin = (Spinner) this.findViewById(R.id.search_price_min);
//        ArrayList<String> itemsPriceMin = new ArrayList<String>();
//        Collections.addAll(itemsPriceMin, this.getResources().getStringArray(R.array.criteria_voiture_prix));
//        itemsPriceMin.add(0, "Min Prix");
//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsPriceMin);
//        this.mSearchPriceMin.setAdapter(arrayAdapter);
//        
//        this.mSearchPriceMax = (Spinner) this.findViewById(R.id.search_price_max);
//        ArrayList<String> itemsPriceMax = new ArrayList<String>();
//        Collections.addAll(itemsPriceMax, this.getResources().getStringArray(R.array.criteria_voiture_prix));
//        itemsPriceMax.set(0, "Max Prix");
//        itemsPriceMax.add("Plus de " + itemsPriceMax.get(itemsPriceMax.size()-1));
//        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsPriceMax);
//        this.mSearchPriceMax.setAdapter(arrayAdapter);
        
		this.mSearchPriceMin = (Spinner) this.findViewById(R.id.search_price_min);
		ArrayList<String> itemsPriceMin = new ArrayList<String>();
		int size = LBC_Application.getInstance().getCurrentCriteria().getPrices().size();
		itemsPriceMin.add("Min Prix");
		for (int i = 0; i < size; i++) {
			itemsPriceMin.add(LBC_Application.getInstance().getCurrentCriteria().getPrices().get(i));
		}
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, itemsPriceMin);
		this.mSearchPriceMin.setAdapter(arrayAdapter);

		this.mSearchPriceMax = (Spinner) this.findViewById(R.id.search_price_max);
		ArrayList<String> itemsPriceMax = new ArrayList<String>();
		itemsPriceMax.add("Max prix");
		for (int i = 1; i < size; i++) {
			itemsPriceMax.add(LBC_Application.getInstance().getCurrentCriteria().getPrices().get(i));
		}
		itemsPriceMax.add("Plus de " + itemsPriceMax.get(itemsPriceMax.size() - 1));
		arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsPriceMax);
		this.mSearchPriceMax.setAdapter(arrayAdapter);
        
        this.mSearchKmMin = (Spinner) this.findViewById(R.id.search_km_min);
        ArrayList<String> itemsKmMin = new ArrayList<String>();
        Collections.addAll(itemsKmMin, this.getResources().getStringArray(R.array.criteria_voiture_km));
        itemsKmMin.add(0, "Min Km");
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsKmMin);
        this.mSearchKmMin.setAdapter(arrayAdapter);
        
        this.mSearchKmMax = (Spinner) this.findViewById(R.id.search_km_max);
        ArrayList<String> itemsKmMax = new ArrayList<String>();
        Collections.addAll(itemsKmMax, this.getResources().getStringArray(R.array.criteria_voiture_km));
        itemsKmMax.set(0, "Max Km");
        itemsKmMax.add("Plus de " + itemsKmMax.get(itemsKmMax.size()-1));
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsKmMax);
        this.mSearchKmMax.setAdapter(arrayAdapter);
        
        this.mSearchYearMin = (Spinner) this.findViewById(R.id.search_year_min);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        ArrayList<String> itemsYearMin = new ArrayList<String>();
        itemsYearMin.add("Année modèle min");
        for (int i = year; i > MIN_YEAR; i--) {
			itemsYearMin.add(String.valueOf(i));
		}
        itemsYearMin.add(MIN_YEAR + " ou avant");
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsYearMin);
        this.mSearchYearMin.setAdapter(arrayAdapter);
        
        this.mSearchYearMax = (Spinner) this.findViewById(R.id.search_year_max);
        ArrayList<String> itemsYearMax = new ArrayList<String>();
        itemsYearMax.add("Année modèle max");
        for (int i = year; i >= MIN_YEAR; i--) {
        	itemsYearMax.add(String.valueOf(i));
		}
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsYearMax);
        this.mSearchYearMax.setAdapter(arrayAdapter);
        
        this.mSearchEnergie = (Spinner) this.findViewById(R.id.search_energie);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, this.getResources().getStringArray(R.array.criteria_voiture_energie));
        this.mSearchEnergie.setAdapter(arrayAdapter);
        this.mSearchBoiteVitesse = (Spinner) this.findViewById(R.id.search_boite_vitesse);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, this.getResources().getStringArray(R.array.criteria_voiture_boite_vitesse));
        this.mSearchBoiteVitesse.setAdapter(arrayAdapter);
        
        this.mSearchParPro = (Spinner) this.findViewById(R.id.search_par_pro);
        final String[] arrayParOrPro = this.getResources().getStringArray(R.array.par_pro_arrays);
        final String currentParOrPro = LBC_Application.getInstance().getCurrentParticulierOrProfessionel();
        int i = arrayParOrPro.length;
        for (; --i > 0; ) {
			if (arrayParOrPro[i].equalsIgnoreCase(currentParOrPro))
				break;
		}
        this.mSearchParPro.setSelection(i);
        
        this.mSearchZipCode = (EditText) this.findViewById(R.id.search_zip_code);
        temp = LBC_Application.getInstance().getCurrentSearchZipCode();
        this.mSearchZipCode.setText(temp);
        this.mSearchZipCode.setSelection(temp.length());
        this.findViewById(R.id.text_search_in_title).setOnClickListener(this);
        this.findViewById(R.id.text_search_urgent).setOnClickListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Sélectionnez plus de critères");
	}

    
    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        getSupportMenuInflater().inflate(R.menu.menu_search_plus, menu);
        return true;
    }

 
    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
    	switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
	            return true;
			case R.id.menu_search_plus_ok:
	        	
				//Search key
				LBC_Application.getInstance().setCurrentSearchKey(this.mSearchKey.getText().toString().trim());
				//Particulier ou Professionnel
				LBC_Application.getInstance().setCurrentParticulierOrProfessionel(
						this.getResources().getStringArray(R.array.par_pro_key_arrays)[this.mSearchParPro.getSelectedItemPosition()]);
				//Search only in title
				LBC_Application.getInstance().setCurrentSearchInTitle(this.mSearchTitleOnly.isChecked());
				//Search only Urgent
				LBC_Application.getInstance().setCurrentSearchUrgent(this.mSearchUrgentOnly.isChecked());
				//Search zip code
				LBC_Application.getInstance().setCurrentSearchZipCode(this.mSearchZipCode.getText().toString().trim());
				
				criteria = new Criteria();
				
				if (this.mSearchPriceMin.getSelectedItemPosition()>0) {
					criteria.priceMin = this.mSearchPriceMin.getSelectedItemPosition() - 1;
				}
				if (this.mSearchPriceMax.getSelectedItemPosition()>0) {
					criteria.priceMax = this.mSearchPriceMax.getSelectedItemPosition();
				}
				if (this.mSearchYearMin.getSelectedItemPosition()>0) {
					criteria.yearMin = (String)this.mSearchYearMin.getSelectedItem();
				} else if (this.mSearchYearMin.getSelectedItemPosition() == this.mSearchYearMin.getCount()-1) {
					criteria.yearMin = String.valueOf(MIN_YEAR);
				}
				if (this.mSearchYearMax.getSelectedItemPosition()>0) {
					criteria.yearMax = (String)this.mSearchYearMax.getSelectedItem();
				}
				if (this.mSearchKmMin.getSelectedItemPosition()>0) {
					criteria.kmMin = ((String)this.mSearchKmMin.getSelectedItem()).trim();
				}
				if (this.mSearchKmMax.getSelectedItemPosition()>0) {
					criteria.kmMax = ((String)this.mSearchKmMax.getSelectedItem()).trim();
				} else if (this.mSearchKmMax.getSelectedItemPosition() == this.mSearchKmMax.getCount()-1) {
					criteria.kmMax = "999999";
				}
				if (this.mSearchEnergie.getSelectedItemPosition()>0) {
					criteria.energie = this.mSearchEnergie.getSelectedItemPosition();
				}
				if (this.mSearchBoiteVitesse.getSelectedItemPosition()>0) {
					criteria.boiteVitesse = this.mSearchBoiteVitesse.getSelectedItemPosition();
				}
				
				Utils.reloadOffresList(criteria);
				finish();
		        return true;
    	}
        return false;
    }


	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.text_search_in_title:
			this.mSearchTitleOnly.setChecked(!this.mSearchTitleOnly.isChecked());
			
			break;
		case R.id.text_search_urgent:
			this.mSearchUrgentOnly.setChecked(!this.mSearchUrgentOnly.isChecked());
			
			break;
		default:
			break;
		}
		
	}
}