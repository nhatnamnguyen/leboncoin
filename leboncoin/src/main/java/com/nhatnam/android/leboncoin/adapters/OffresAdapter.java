package com.nhatnam.android.leboncoin.adapters;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.nhatnam.android.leboncoin.R;
import com.nhatnam.android.leboncoin.activities.OffreDetailActivity;
import com.nhatnam.android.leboncoin.providers.OffresContract.Offres;
import com.nhatnam.android.leboncoin.utils.PreviewImageManager;

public class OffresAdapter extends CursorAdapter implements OnItemClickListener {

	/**
	 * Used to instantiate layout XML file.
	 */
	private final LayoutInflater mInflater;
	
    private final PreviewImageManager mPhotoLoader;
    
    private boolean mModeDeleteFavorite;
    /**
	 * List of offre favorite to delete
	 */
	private final transient Set<String> mOffresSelected = new HashSet<String>();
	
	public OffresAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		
		this.mModeDeleteFavorite = false;
		
		this.mInflater = LayoutInflater.from(context);

        this.mPhotoLoader = PreviewImageManager.getInstance(context);
	}


	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View view;
		final OffreHolder holder = new OffreHolder();
		
		view = this.mInflater.inflate(R.layout.offre_row_view, parent, false);
		
		holder.mOffreId = cursor.getString(cursor.getColumnIndex(Offres._ID));
	    holder.mThumb = (ImageView) view.findViewById(R.id.offre_thumb);
	    holder.mTitle = (TextView) view.findViewById(R.id.offre_title);
	    holder.mPrice = (TextView) view.findViewById(R.id.offre_price);
	    holder.mCategory = (TextView) view.findViewById(R.id.offre_category);
	    holder.mLocalization = (TextView) view.findViewById(R.id.offre_localisation);
	    holder.mDate= (TextView) view.findViewById(R.id.offre_date);
	    holder.mCheckBox = (CheckBox) view.findViewById(R.id.offre_checkbox);
	    
	    view.setTag(holder);
	    return view;
	}
	
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		OffreHolder holder = (OffreHolder)view.getTag();
		
		holder.mOffreId = cursor.getString(cursor.getColumnIndex(Offres._ID));
		holder.mOffreLink = cursor.getString(cursor.getColumnIndex(Offres.LINK));
		holder.mTitle.setText(cursor.getString(cursor.getColumnIndex(Offres.TITLE)));
		holder.mPrice.setText(cursor.getString(cursor.getColumnIndex(Offres.PRICE)));
		holder.mCategory.setText(cursor.getString(cursor.getColumnIndex(Offres.CATEGORY)));
		holder.mLocalization.setText(cursor.getString(cursor.getColumnIndex(Offres.LOCALISATION)));
		holder.mDate.setText(cursor.getString(cursor.getColumnIndex(Offres.DATE)));
		holder.mOffreStatus = cursor.getInt(cursor.getColumnIndex(Offres.STATUS));
		if (this.mModeDeleteFavorite) {
			holder.mCheckBox.setVisibility(View.VISIBLE);
			holder.mCheckBox.setTag(holder.mOffreId);
	    	holder.mCheckBox.setVisibility(View.VISIBLE);
	    	holder.mCheckBox.setChecked(this.mOffresSelected.contains(holder.mOffreId));
		} else {
			holder.mCheckBox.setVisibility(View.GONE);
		}
		
		String thumbUri = cursor.getString(cursor.getColumnIndex(Offres.THUMB));
		this.mPhotoLoader.loadPhoto(
				 holder.mThumb,
				 thumbUri!=null?Uri.parse(thumbUri):null,
	                true, false, true, PreviewImageManager.DEFAULT_IMAGE
	                );
		
		if (cursor.getInt(cursor.getColumnIndex(Offres.STATUS)) == 1) {
			//holder.mTitle.setTypeface(null, Typeface.NORMAL);
			//view.setBackgroundResource(R.drawable.list_item_read_background);
		} else {
			//holder.mTitle.setTypeface(null, Typeface.BOLD);
			//view.setBackgroundResource(R.drawable.list_item_background);
		}
	}

	
	/**
	 * Offre tag class
	 */
	public final class OffreHolder {
		public transient String mOffreId, mOffreLink;
		public transient int mOffreStatus;
		public transient ImageView mThumb;
		public transient TextView mTitle, mPrice, mCategory, mLocalization, mDate;
		public transient CheckBox mCheckBox;
	}


	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
		if (!this.mModeDeleteFavorite) {
	        final Intent intent = new Intent(this.mContext, OffreDetailActivity.class);
	        intent.putExtra("offreId", ((OffreHolder)view.getTag()).mOffreId);
	        intent.putExtra("offreLink", ((OffreHolder)view.getTag()).mOffreLink);
	        intent.putExtra("offreStatus", ((OffreHolder)view.getTag()).mOffreStatus);
	        
	        this.mContext.startActivity(intent);
		} else {
			
		}
	}
	
	
	public void setModeDeleteFavorite(final boolean modeDeleteFavorite) {
		this.mModeDeleteFavorite = modeDeleteFavorite;
	}
	
	public boolean isModeDeleteFavorite() {
		return this.mModeDeleteFavorite;
	}
	
	public final Set<String> getOffresSelected() {
		return this.mOffresSelected;
	}
	
	public final boolean addOffreToDelete(final String offreId) {
		return this.mOffresSelected.add(offreId);
	}
	
	public final boolean removeOffreToDelete(final String offreId) {
		return this.mOffresSelected.remove(offreId);
	}
}
