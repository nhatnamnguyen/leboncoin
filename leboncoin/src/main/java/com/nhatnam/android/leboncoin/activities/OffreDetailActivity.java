package com.nhatnam.android.leboncoin.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.nhatnam.android.leboncoin.R;
import com.nhatnam.android.leboncoin.parsers.ParsingOffreDetail;
import com.nhatnam.android.leboncoin.providers.OffresContract.Offres;
import com.nhatnam.android.leboncoin.utils.PreviewImageManager;


public class OffreDetailActivity extends SherlockFragmentActivity implements OnClickListener, LoaderManager.LoaderCallbacks<Cursor>
{

    private String            mOffreId, mOffreLink, mOffreSendEmailLink, mOffreTitle;
    
    private ScrollView scrollView;
    private ViewGroup vgParent;
    private TextView tvOffreTitle, tvOffreAuthorName, tvOffreDate, tvOffrePrice,
    	tvOffreLocalisation, tvOffreDescription;
    private ImageView ivOffreImage1, ivOffreImage2, ivOffreImage3;
    
    private Button btCallMe, btAddMe, btSendMe, btSuggestMe, btSaveMe, btDeleteMe; 
    
    private PreviewImageManager mPhotoLoader;
    
    
   
    SimpleOnGestureListener simpleOnGestureListener  = new SimpleOnGestureListener(){
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			String swipe = "";
			float sensitvity = 50;
			
			// TODO Auto-generated method stub
			if((e1.getX() - e2.getX()) > sensitvity){
				swipe += "Swipe Left\n";
			}else if((e2.getX() - e1.getX()) > sensitvity){
				swipe += "Swipe Right\n";
			}else{
				swipe += "\n";
			}
			
			if((e1.getY() - e2.getY()) > sensitvity){
				swipe += "Swipe Up\n";
			}else if((e2.getY() - e1.getY()) > sensitvity){
				swipe += "Swipe Down\n";
			}else{
				swipe += "\n";
			}
			
			Log.i("SWIPE", ".............................." + swipe);
			
			return super.onFling(e1, e2, velocityX, velocityY);
		}
    };
    
    GestureDetector gestureDetector = new GestureDetector(simpleOnGestureListener);
    
    
    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.mOffreId = getIntent().getStringExtra("offreId");
        this.mOffreLink = getIntent().getStringExtra("offreLink");
        
        setContentView(R.layout.offre_detail_fragment);
		
        this.scrollView = (ScrollView) findViewById(R.id.scroll_id);
        this.vgParent = (ViewGroup) findViewById(R.id.offre_parent);
		this.tvOffreTitle = (TextView) findViewById(R.id.title);
        this.ivOffreImage1 = (ImageView) findViewById(R.id.image1);
        this.ivOffreImage2 = (ImageView) findViewById(R.id.image2);
        this.ivOffreImage3 = (ImageView) findViewById(R.id.image3);
        this.tvOffreAuthorName = (TextView) findViewById(R.id.author_name);
        this.tvOffreDate = (TextView) findViewById(R.id.date);
        this.tvOffrePrice = (TextView) findViewById(R.id.price);
        this.tvOffreLocalisation = (TextView) findViewById(R.id.localisation);
        this.tvOffreDescription = (TextView) findViewById(R.id.description);
        
        this.btCallMe = (Button) findViewById(R.id.call_me);
        this.btAddMe = (Button) findViewById(R.id.add_me);
        this.btSendMe = (Button) findViewById(R.id.send_me);
        this.btSuggestMe = (Button) findViewById(R.id.suggest_me);
        this.btSaveMe = (Button) findViewById(R.id.save_me);
        this.btDeleteMe = (Button) findViewById(R.id.delete_me);
        
        
        ////Only Get and Parse the detail when neccesary 
        if ((getIntent().getIntExtra("offreStatus", 0) & Offres.OFFRE_STATUS_DOWNLOADED_DETAIL) == 0) {
	        ParsingOffreDetail parsingOffreDetail = new ParsingOffreDetail();
	        parsingOffreDetail.execute(this.mOffreId, this.mOffreLink);
        }
        
        getSupportLoaderManager().initLoader(0, null, this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Description de l'offre");
        
        this.mPhotoLoader = PreviewImageManager.getInstance(this);
	}
    
    @Override
   	public boolean onTouchEvent(MotionEvent event) {
   		// TODO Auto-generated method stub
       	return gestureDetector.onTouchEvent(event);
   	}
    
    
    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args)
    {
        return new CursorLoader(this,
                /* FROM */Offres.CONTENT_URI.buildUpon().appendEncodedPath("offres").build(),
                /*PROJECTION*/ null,
                /* WHERE*/Offres._ID + "='" + this.mOffreId + "'",
                /* WHEREARGS */null,
                /* ORDER */"date DESC");
    }
    

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor)
    {
        //findViewById(android.R.id.empty).setVisibility(View.GONE);
        
        if (null!=cursor && cursor.moveToFirst()) {
        	
        	this.mOffreTitle = cursor.getString(cursor.getColumnIndex(Offres.TITLE));
        	this.tvOffreTitle.setText(this.mOffreTitle);
	        this.tvOffreAuthorName.setText(cursor.getString(cursor.getColumnIndex(Offres.AUTHOR_NAME)));
	        this.mOffreSendEmailLink = cursor.getString(cursor.getColumnIndex(Offres.SEND_EMAIL_LINK));
	        
	        if (cursor.getLong(cursor.getColumnIndex(Offres.TIMESTAMP))>0L) {
	        	this.tvOffreDate.setText(
		        		DateUtils.formatDateTime(this, cursor.getLong(cursor.getColumnIndex(Offres.TIMESTAMP)), 
		        		DateUtils.FORMAT_24HOUR|DateUtils.FORMAT_SHOW_DATE|DateUtils.FORMAT_SHOW_TIME));
	        } else {
	        	this.tvOffreDate.setText(cursor.getString(cursor.getColumnIndex(Offres.DATE)));
	        }
	        
	        this.tvOffrePrice.setText(cursor.getString(cursor.getColumnIndex(Offres.PRICE)));
	        this.tvOffreLocalisation.setText(cursor.getString(cursor.getColumnIndex(Offres.LOCALISATION)));
	        this.tvOffreDescription.setText(cursor.getString(cursor.getColumnIndex(Offres.DESCRIPTION)));
	        // Do this for each view added to the grid
	        this.vgParent.setOnClickListener(this);
	        
	        String tel = cursor.getString(cursor.getColumnIndex(Offres.AUTHOR_TEL)); 
	        if (!TextUtils.isEmpty(tel)) {
	        	this.btCallMe.setEnabled(true);
	        	this.btCallMe.setTag(tel);
	        	this.btCallMe.setOnClickListener(this);
	        } else {
	        	this.btCallMe.setEnabled(false);
	        }
	        
	        
	        this.btAddMe = (Button) findViewById(R.id.add_me);
	        this.btSendMe = (Button) findViewById(R.id.send_me);
	        this.btSendMe.setOnClickListener(this);
	        
	        this.btSuggestMe = (Button) findViewById(R.id.suggest_me);
	        this.btSuggestMe.setOnClickListener(this);
	        
	        int offreStatus = cursor.getInt(cursor.getColumnIndex(Offres.STATUS)); 
	        this.btSaveMe = (Button) findViewById(R.id.save_me);
	        this.btSaveMe.setTag(offreStatus);
	        this.btSaveMe.setOnClickListener(this);
	        if ((offreStatus&Offres.OFFRE_STATUS_FAVORITED) != 0) {
	        	this.btSaveMe.setText("Unsave");
	        } else {
	        	this.btSaveMe.setText("Save");
	        }
	        
	        this.btDeleteMe = (Button) findViewById(R.id.delete_me);
	        
	        String images = cursor.getString(cursor.getColumnIndex(Offres.IMAGES)); 
	        if (!TextUtils.isEmpty(images)) {
	        	String[] imagesUri = images.split("&");
	        	
	        	this.ivOffreImage1.setVisibility(View.VISIBLE);
	        	this.mPhotoLoader.loadPhoto(
					 this.ivOffreImage1,
					 Uri.parse(imagesUri[0]),
		                true, false, false, PreviewImageManager.DEFAULT_IMAGE
		                );
	        	
	        	if (imagesUri.length > 1) {
	        		this.ivOffreImage2.setVisibility(View.VISIBLE);
	        		this.mPhotoLoader.loadPhoto(
	   					 this.ivOffreImage2,
	   					 Uri.parse(imagesUri[1]),
	   		                true, false, false, PreviewImageManager.DEFAULT_IMAGE
	   		                );
	        	} else {
	        		this.ivOffreImage2.setVisibility(View.GONE);
	        	}
	        	
	        	if (imagesUri.length > 2) {
	        		this.ivOffreImage3.setVisibility(View.VISIBLE);
		        	this.mPhotoLoader.loadPhoto(
							 this.ivOffreImage3,
							 Uri.parse(imagesUri[2]),
				                true, false, false, PreviewImageManager.DEFAULT_IMAGE
				                );
	        	} else {
	        		this.ivOffreImage3.setVisibility(View.GONE);
	        	}
	        } else {
	        	this.ivOffreImage1.setVisibility(View.GONE);
	        	this.ivOffreImage2.setVisibility(View.GONE);
	        	this.ivOffreImage3.setVisibility(View.GONE);
	        }
	        
        }
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader)
    {
    	// this.mViewPager.setAdapter(null);
    }

    
	@Override
	public void onClick(View view) {

		switch (view.getId()) {
			case R.id.call_me:
				Log.i("LBC", "Call me .............:" + (String)view.getTag());
				
				Intent callIntent = new Intent(Intent.ACTION_VIEW);
				//Ou new Intent(Intent.ACTION_CALL);
				callIntent.setData(Uri.parse((String)view.getTag()));
				startActivity(callIntent);
				
				break;
			case R.id.add_me:
				Log.i("LBC", "Add me .............");
				break;
			case R.id.send_me:
				final Intent intentSendEmail = new Intent(this, SendEmailActivity.class);
				intentSendEmail.putExtra("offreId", this.mOffreId);
				intentSendEmail.putExtra("sendEmailLink", this.mOffreSendEmailLink);
				
				this.startActivity(intentSendEmail);
				break;
			case R.id.suggest_me:
				Log.i("LBC", "Suggest me .............");
				final Intent suggestIntent = new Intent();
				suggestIntent.setAction(Intent.ACTION_SEND);
				suggestIntent.putExtra(Intent.EXTRA_SUBJECT, String.format(this.getString(R.string.suggest_friend_title), this.mOffreTitle));
				suggestIntent.putExtra(Intent.EXTRA_TEXT, String.format(this.getString(R.string.suggest_friend_message), mOffreLink));
				suggestIntent.setType("text/plain");
				startActivity(Intent.createChooser(suggestIntent, this.getString(R.string.suggest_friend)));
				
				break;
			case R.id.save_me:
				Log.i("LBC", "Save or Unsave me .............");
				int status = (Integer) view.getTag();
				ContentValues cv = new ContentValues();
				cv.put(Offres._ID, mOffreId);
				if ((status & Offres.OFFRE_STATUS_FAVORITED) == 0) {
					cv.put(Offres.STATUS, status|Offres.OFFRE_STATUS_FAVORITED);
				} else {
					cv.put(Offres.STATUS, status&(~Offres.OFFRE_STATUS_FAVORITED));
				}
				
				this.getContentResolver().update(Offres.CONTENT_URI.buildUpon().appendEncodedPath("offres").appendEncodedPath(this.mOffreId).build(), 
						cv, null, null);
				break;
			case R.id.delete_me:
				Log.i("LBC", "Delete me .............");
				break;
			default:
				break;
		}
	}
	

	public void postData() {
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(
				"http://mobile.leboncoin.fr/sendmail.html?id=369176131&ca=12_s&w=3&f=a&cg=0&th=2");

		try {
			// Add data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("q", ""));
			nameValuePairs.add(new BasicNameValuePair("name", "totoAnd"));
			nameValuePairs.add(new BasicNameValuePair("email", "trai_12t1@yahoo.com"));
			nameValuePairs.add(new BasicNameValuePair("phone", "0612345678"));
			nameValuePairs.add(new BasicNameValuePair("adreply_body", "Bonjour"));
			nameValuePairs.add(new BasicNameValuePair("cc", "1"));
			nameValuePairs.add(new BasicNameValuePair("id", "369176131"));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			Log.i("postData", "reponse.................." + response.getEntity().toString());

		} catch (ClientProtocolException e) {
			Log.e("postDataClient", e.getMessage());
		} catch (IOException e) {
			Log.e("postDataIO", e.getMessage());
		}
	}

}