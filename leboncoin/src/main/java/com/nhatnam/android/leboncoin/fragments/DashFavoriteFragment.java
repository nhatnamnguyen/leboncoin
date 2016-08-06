package com.nhatnam.android.leboncoin.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nhatnam.android.leboncoin.R;
import com.nhatnam.android.leboncoin.adapters.OffresAdapter;
import com.nhatnam.android.leboncoin.providers.OffresContract.Offres;

public class DashFavoriteFragment extends SherlockListFragment implements LoaderCallbacks<Cursor>, OnClickListener
{
	
	private OffresAdapter mOffresFavoriteAdapter;
	
	/**
	 * Mode select favorite to delete
	 */
	public transient boolean mDeleteMode = false;
	private transient ViewGroup mBottomBar;
	/**
	 * Position of scroll bar
	 */
	private int mScrollBarPosotion[] = new int[2];
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.favorite_list_fragment, container, false);
	}
	
	
	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);
        
        this.mBottomBar = (ViewGroup) getView().findViewById(R.id.action_bottom_bar);
        getView().findViewById(R.id.favorite_delete_button).setOnClickListener(this);
        getView().findViewById(R.id.favorite_delete_cancel_button).setOnClickListener(this);
        
        getLoaderManager().initLoader(0, null, this);
        
        //Restore the scroll bar position
		if (null!=savedInstanceState) {
			this.mScrollBarPosotion[0] = savedInstanceState.getInt("scrollbar0", 0);
			this.mScrollBarPosotion[1] = savedInstanceState.getInt("scrollbar1", 0);
		}
    }
	
	
	
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        return new CursorLoader(getActivity(),
	                /* FROM */Offres.CONTENT_URI.buildUpon().appendPath("offres").build(),
	                /* COLS */null,
	                /* WHERE */ "("+ Offres.STATUS + "&" + Offres.OFFRE_STATUS_FAVORITED + ") <> 0", null,
	                /* ORDER */null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		this.mOffresFavoriteAdapter = new OffresAdapter(getActivity(), cursor, true);
		setListAdapter(this.mOffresFavoriteAdapter);
		getListView().setOnItemClickListener(this.mOffresFavoriteAdapter);
		
		//Restore the scroll bar position
		getListView().setSelectionFromTop(this.mScrollBarPosotion[0], this.mScrollBarPosotion[1]);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursor) {
		 this.mOffresFavoriteAdapter.changeCursor(null);
	}
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_favorite, menu);
	}
	
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
	}

	
	@Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_favorite_delete:
            	
 		        return true;
            default:
                return false;
        }
    }

	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		//Save scroll bar position
		this.mScrollBarPosotion[0] = getListView().getFirstVisiblePosition();
		View first = getListView().getChildAt(0);
		this.mScrollBarPosotion[1] = 0;
		if (null!=first) {
			this.mScrollBarPosotion[1] = first.getTop();
		}
		
		outState.putInt("scrollbar0", this.mScrollBarPosotion[0]);
		outState.putInt("scrollbar1", this.mScrollBarPosotion[1]);
	}
   
	
	 /**
     * Hide/unhide bottom bar with an animation.
     */
    public final void animateBottomBar() {
		if (/*getListAdapter().getConvSelected().isEmpty()*/ true) {
		    this.mBottomBar.setAnimation(AnimationUtils.loadAnimation(this.getActivity(), R.anim.push_down_out));
		    this.mBottomBar.startLayoutAnimation();
	
		    this.mBottomBar.setVisibility(View.GONE);
		}
		else if (!this.mBottomBar.isShown()) {
		    this.mBottomBar.setVisibility(View.VISIBLE);
	
		    this.mBottomBar.setAnimation(AnimationUtils.loadAnimation(this.getActivity(), R.anim.push_up_in));
		    this.mBottomBar.startLayoutAnimation();
		}
    }


	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.favorite_delete_cancel_button:
			animateBottomBar();
		    break;
		case R.id.favorite_delete_button:
			
		    break;
		default:
		    break;
		}
	}
}
