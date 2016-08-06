package com.nhatnam.android.leboncoin.fragments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nhatnam.android.leboncoin.LBC_Application;
import com.nhatnam.android.leboncoin.R;
import com.nhatnam.android.leboncoin.activities.CarteFranceActivity;
import com.nhatnam.android.leboncoin.activities.SearchPlusActivity;
import com.nhatnam.android.leboncoin.adapters.OffresAdapter;
import com.nhatnam.android.leboncoin.parsers.ParsingCriterias;
import com.nhatnam.android.leboncoin.parsers.ParsingOffresList;
import com.nhatnam.android.leboncoin.providers.OffresContract.Offres;
import com.nhatnam.android.leboncoin.utils.LeboncoinConstant;
import com.nhatnam.android.leboncoin.utils.Utils;

public class DashOffreFragment extends SherlockListFragment implements LoaderCallbacks<Cursor>, OnClickListener, android.content.DialogInterface.OnClickListener
{

	private OffresAdapter 						mOffresAdapter;
	private boolean                           	mIsLoading = false;
	private MenuItem                          	mLoadingItem;

	private int mScrollBarPosotion[] = new int[2];
	private View mFooterList;
	
	private LeboncoinDialogFragment myAnnouncesFragment;
	
	    
	/**
     * Filter action to handle broadcasts
     */
    private transient IntentFilter            mFilter;
    private final transient BroadcastReceiver mBroadcastReceiver        = new BroadcastReceiver()
    {
        @Override
        public final void onReceive(final Context context, final Intent intent)
        {
            try
            {
                //final Bundle bundle = intent.getExtras();
                final String action = intent.getAction();

                if (LeboncoinConstant.INTENT_BROADCAST_DOWNLOADED.equalsIgnoreCase(action))
                {
                    stopProgressSpin();
                } else if (LeboncoinConstant.INTENT_BROADCAST_CRITERIA_DOWNLOADED.equalsIgnoreCase(action)) {
                	final Intent intentSearchPlus = new Intent(DashOffreFragment.this.getActivity(), SearchPlusActivity.class);
    				startActivity(intentSearchPlus);
                }
            }
            catch (final Exception ex){ Log.e("lbc", ex.getMessage());}
        }
    };
    
	
	public void onActivityCreated(final Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);
        
		//Footer view for List
		this.mFooterList = this.getLayoutInflater(savedInstanceState).inflate(R.layout.offre_row_footer_list, null);
		this.mFooterList.setOnClickListener(this);
		
		this.mFilter = new IntentFilter(LeboncoinConstant.INTENT_BROADCAST_DOWNLOADED);
		this.mFilter.addAction(LeboncoinConstant.INTENT_BROADCAST_CRITERIA_DOWNLOADED);
		
		if (savedInstanceState==null) {
	    	//Download offres for the first time
	    	ParsingOffresList parsingOffreSax = new ParsingOffresList();
	        parsingOffreSax.execute(LBC_Application.getInstance().getCurrentRequestOffresUri());
	        startProgressSpin();
		} else {
			//Restore the scroll bar position
			this.mScrollBarPosotion[0] = savedInstanceState.getInt("scrollbar0", 0);
			this.mScrollBarPosotion[1] = savedInstanceState.getInt("scrollbar1", 0);
		}
		
		getLoaderManager().initLoader(0, null, this);
    }
	

	@Override
	public void onResume() {
		super.onResume();
		
		this.getActivity().registerReceiver(this.mBroadcastReceiver, this.mFilter);
	}


	@Override
	public void onStop() {
		super.onStop();
		
		this.getActivity().unregisterReceiver(this.mBroadcastReceiver);
	}

	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        return new CursorLoader(getActivity(),
	                /* FROM */Offres.CONTENT_URI.buildUpon().appendPath("offres").build(),
	                /* COLS */null,
	                /* WHERE */null, null,
	                /* ORDER */Offres.TIMEDOWNLOAD + " ASC");
	}
	

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_offre, menu);
	}
	
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		this.mLoadingItem = menu.findItem(R.id.menu_refresh);
		if (this.mIsLoading) {
			final LayoutInflater inflater = (LayoutInflater) this.getActivity()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.mLoadingItem.setActionView(inflater.inflate(
					R.layout.action_bar_loading, null));
		} else {
			this.mLoadingItem.setIcon(R.drawable.ic_menu_refresh);
		}
	}

	
	@Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_refresh:
            	if (!this.mIsLoading)
 		        {
 		        	//Reload offres
 		            Utils.reloadOffresList(null);
 		
 		            // Start the progress bar
 		            startProgressSpin();
 		        }
 		        return true;
            case R.id.menu_category:
		    	//Create list category dialog
		    	LeboncoinDialogFragment categoryFragment = LeboncoinDialogFragment.newInstance(this, LeboncoinDialogFragment.DIALOG_CATEGORY_ID);
			    categoryFragment.show(this.getSherlockActivity().getSupportFragmentManager(), "DialogCategory");
		    	return true;
		    case R.id.menu_localisation:
		    	//Create list localisation dialog
		    	LeboncoinDialogFragment localisationFragment = LeboncoinDialogFragment.newInstance(this, LeboncoinDialogFragment.DIALOG_DEPARTEMENT_ID);
			    localisationFragment.show(this.getSherlockActivity().getSupportFragmentManager(), "DialogLocalisation");
			    return true;
		    case R.id.menu_about:
		    	//Rechercher mes announces en ligne
		    	LeboncoinDialogFragment aboutFragment = LeboncoinDialogFragment.newInstance(this, LeboncoinDialogFragment.DIALOG_ABOUT_ID);
		    	aboutFragment.show(this.getSherlockActivity().getSupportFragmentManager(), "DialogAbout");
			    return true;
		    case R.id.menu_mes_annonces_en_ligne:
		    	//Rechercher mes announces en ligne
		    	myAnnouncesFragment = LeboncoinDialogFragment.newInstance(this, LeboncoinDialogFragment.DIALOG_MY_ANNOUNCES_ID);
		    	myAnnouncesFragment.show(this.getSherlockActivity().getSupportFragmentManager(), "DialogMyAnnounces");
			    return true;
		    case R.id.menu_change_localisation:
		    	final Intent intent = new Intent(this.getActivity(), CarteFranceActivity.class);
		    	intent.putExtra("fromMenu", true);
				startActivity(intent);
		    	return true;
		    case R.id.menu_search:
		    	//Request criteria
				ParsingCriterias parsingCriterias = new ParsingCriterias();
				parsingCriterias.execute(LBC_Application.getInstance()
						.getCurrentRequestCriteriasUri());
		    	return true;
		    case R.id.menu_delete_annonce:
		    	requestDeleteAnnounce("376746629");
		    
		    	return true;
            default:
                return false;
        }
    }


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		
		//Add footer view to load more offres
//		if (LBC_Application.getInstance().getCurrentPageNumber() < LBC_Application.getInstance().getCurrentPageTotal()) 
		{
			if (getListView().getFooterViewsCount() == 0)
				getListView().addFooterView(this.mFooterList);
		} 
//		else {
//			getListView().removeFooterView(this.mFooterList);
//		}
		
		if (this.mOffresAdapter==null) {
			this.mOffresAdapter = new OffresAdapter(getActivity(), cursor, true);
			setListAdapter(this.mOffresAdapter);
			//Restore the scroll bar position
			getListView().setSelectionFromTop(this.mScrollBarPosotion[0], this.mScrollBarPosotion[1]);
		} else {
			this.mOffresAdapter.swapCursor(cursor);
		}
		getListView().setOnItemClickListener(this.mOffresAdapter);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursor) {
		 this.mOffresAdapter.changeCursor(null);
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

	
	@Override
	public void onClick(View v) {
		saveScrollBarPosition();
		
		//Request more offres
		ParsingOffresList parsingOffreSax = new ParsingOffresList();
		parsingOffreSax.execute(LBC_Application.getInstance()
				.getCurrentRequestOffresUri() + "&o=" + LBC_Application.getInstance().getNextPageNumber());
		this.startProgressSpin();
	}
	
	
	private void saveScrollBarPosition() {
		//Save scroll bar position
		this.mScrollBarPosotion[0] = getListView().getFirstVisiblePosition();
		View first = getListView().getChildAt(0);
		this.mScrollBarPosotion[1] = 0;
		if (null!=first) {
			this.mScrollBarPosotion[1] = first.getTop();
		}
	}
	
	
	/**
     * Start the progress spin
     */
    public void startProgressSpin()
    {
    	this.mIsLoading = true;
    	if (this.mLoadingItem!=null) {
	        final LayoutInflater inflater = (LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        this.mLoadingItem.setActionView(inflater.inflate(R.layout.action_bar_loading, null));
    	}
    }

    
    /**
     * Stop the progress spin
     */
    public void stopProgressSpin()
    {
    	this.mIsLoading = false;
    	if (this.mLoadingItem!=null) {
    		this.mLoadingItem.setActionView(null);
    		this.mLoadingItem.setIcon(R.drawable.ic_menu_refresh);
    	}
    }
    
    
    @Override
	public void onClick(DialogInterface dialog, int which) {
		switch (LeboncoinDialogFragment.mCurrentDialogId) {
			case LeboncoinDialogFragment.DIALOG_CATEGORY_ID:
				int newCategory = LBC_Application.getInstance().getCategoryList().get(which).getValue();
				if (newCategory != LBC_Application.getInstance().getCurrentCategory()) {
					
					LBC_Application.getInstance().setCurrentCategory(newCategory, true);
					
					//Reload offres
					Utils.reloadOffresList(null);
					
					// Start the progress bar
	                startProgressSpin();
				}
                
				//Close dialog list
				dialog.dismiss();
				break;
			case LeboncoinDialogFragment.DIALOG_DEPARTEMENT_ID:
				int newLocalisation = LBC_Application.getInstance().getDepartementList().get(which).getValue();
				if (newLocalisation != LBC_Application.getInstance().getCurrentDepartement()) {
					
					LBC_Application.getInstance().setCurrentDepartement(newLocalisation, true);
					
					//Reload offres
					Utils.reloadOffresList(null);

					// Start the progress bar
	                startProgressSpin();
				}
				
				//Close dialog list
				dialog.dismiss();
				break;
			case LeboncoinDialogFragment.DIALOG_MY_ANNOUNCES_ID:
				final EditText etEmail = (EditText) myAnnouncesFragment.getDialog().findViewById(R.id.email); 
				String email = etEmail.getText().toString();
				if (!TextUtils.isEmpty(email)) {
					Log.i("lbc", "Request announces online of : " + email);
					requestMyAnnouncesOnline(email);
				} else {
					etEmail.setError("Email non valid");
					//TODO : Avoid to dismiss dialogFragment
				}
				break;
			default :
				break;
		}
	}
    
    
    /**
     * Send a request of my announces in a Thread
     * @param email
     */
    void requestMyAnnouncesOnline(String email) {
        class RequestMyAnnouncesTask implements Runnable {
            String emailAnnounceur;
            RequestMyAnnouncesTask(String email) { emailAnnounceur = email;}
            public void run() {
            	// Create a new HttpClient and Post Header
        		HttpClient httpclient = new DefaultHttpClient();
        		HttpPost httppost = new HttpPost("http://www2.leboncoin.fr/ma");

        		try {
        			// Add data
        			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        			nameValuePairs.add(new BasicNameValuePair("ca", "21_s"));
        			nameValuePairs.add(new BasicNameValuePair("email", emailAnnounceur));
        			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        			// Execute HTTP Post Request
        			HttpResponse response = httpclient.execute(httppost);
        			Log.i("lbc", "reponse request my announces online.................." + response.getEntity().toString());

        		} catch (ClientProtocolException e) {
        			Log.e("lbc", e.getMessage());
        		} catch (IOException e) {
        			Log.e("lbc", e.getMessage());
        		}
            }
        }
        Thread thread = new Thread(new RequestMyAnnouncesTask(email));
        thread.start();
    }
    
    
    
    /**
     * Request to delete an announce
     * @param announce id
     */
    void requestDeleteAnnounce(String announceId) {
        class RequestDeleteAnnounceTask implements Runnable {
            String announceId;
            RequestDeleteAnnounceTask(String id) { announceId = id;}
            public void run() {
            	
        		try {
            		//Request cookie
        			String requestString = new String("http://www2.leboncoin.fr/ai/load/0?ca=21_s&id=" + announceId + "&cmd=delete");
        			HttpClient httpclient = new DefaultHttpClient();
        			BasicHttpContext mHttpContext = new BasicHttpContext();
        			CookieStore mCookieStore      = new BasicCookieStore();        
        			mHttpContext.setAttribute(ClientContext.COOKIE_STORE, mCookieStore);
        			HttpResponse response = httpclient.execute(new HttpGet(requestString), mHttpContext);
        			Log.i("lbc", EntityUtils.toString(response.getEntity()));
        			
        			httpclient = new DefaultHttpClient();
        			requestString = new String("http://www2.leboncoin.fr/ai/manage_delete/0");
        			response = httpclient.execute(new HttpGet(requestString), mHttpContext);
        			Log.i("lbc", EntityUtils.toString(response.getEntity()));
        			
        			//Send delete request
        			httpclient = new DefaultHttpClient();
        			HttpPost httppost = new HttpPost("http://www2.leboncoin.fr/ai/delete/0");
        			
        			// Add data
        			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        			nameValuePairs.add(new BasicNameValuePair("encode", "1"));
        			nameValuePairs.add(new BasicNameValuePair("passwd", "nhatnam"));
        			nameValuePairs.add(new BasicNameValuePair("delete_reason", "3"));
        			nameValuePairs.add(new BasicNameValuePair("cmd", "load"));
        			nameValuePairs.add(new BasicNameValuePair("continue", "Continuer"));
        			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        			
        			response = httpclient.execute(httppost, mHttpContext);
        			Log.i("lbc", EntityUtils.toString(response.getEntity()));
        		} catch (Exception e) {
        			Log.i("lbc", "Network exception: ", e);
        		}
        		
        		
            }
        }
        Thread thread = new Thread(new RequestDeleteAnnounceTask(announceId));
        thread.start();
    }
}
