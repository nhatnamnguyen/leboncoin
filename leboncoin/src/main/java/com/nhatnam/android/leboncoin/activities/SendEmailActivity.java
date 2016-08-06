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

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.nhatnam.android.leboncoin.R;


public class SendEmailActivity extends SherlockFragmentActivity implements OnClickListener
{
	private String mOffreId, mSendEmailLink;
	private EditText mEditTextName, mEditTextEmail, mEditTextPhone, mEditTextText;
	private CheckBox mCheckBoxCopie;
	private TextView mTextViewCopie;
   
    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.mOffreId = getIntent().getStringExtra("offreId");
        this.mSendEmailLink = getIntent().getStringExtra("sendEmailLink");
        
        setContentView(R.layout.send_email_activity);
		
        this.mEditTextName = (EditText) findViewById(R.id.name);
        this.mEditTextEmail = (EditText) findViewById(R.id.email);
		this.mEditTextPhone = (EditText) findViewById(R.id.phone);
        this.mEditTextText = (EditText) findViewById(R.id.text);
       
        this.mCheckBoxCopie = (CheckBox) findViewById(R.id.cc);
        this.mTextViewCopie = (TextView) findViewById(R.id.textview_cc);
        this.mTextViewCopie.setOnClickListener(this);
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Envoyer un email à l'annonceur");
	}

    
    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        getSupportMenuInflater().inflate(R.menu.menu_send_email, menu);
        return true;
    }

 
    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
    	switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
	            return true;
			case R.id.menu_send_email:
	        	if (checkInfoBeforeSendEmail()) {
		        	new Thread(new Runnable() {
						@Override
						public void run() {
							requestSendEmail();
						}
					}).start();
		        
		        	finish();
	        	} else {
	        		//TODO
	        	}
		        return true;
    	}
        return false;
    }
    
    
    private boolean checkInfoBeforeSendEmail() {
    	//TODO
    	if (this.mEditTextPhone.getText().toString().length()!=10) {
    		this.mEditTextPhone.setError("Don't believe you", this.getResources().getDrawable(R.drawable.indicator_input_error));
    		this.mEditTextPhone.setFocusableInTouchMode(true);
    		this.mEditTextPhone.requestFocus();
    		return false;
    	}
    	return true;
    }
	
    /**
     * Request to send email to annouceur
     */
    public void requestSendEmail() {
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(this.mSendEmailLink);

		try {
			// Add data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("q", ""));
			nameValuePairs.add(new BasicNameValuePair("name", this.mEditTextName.getText().toString()));
			nameValuePairs.add(new BasicNameValuePair("email", this.mEditTextEmail.getText().toString()));
			nameValuePairs.add(new BasicNameValuePair("phone", this.mEditTextPhone.getText().toString()));
			nameValuePairs.add(new BasicNameValuePair("adreply_body", this.mEditTextText.getText().toString()));
			nameValuePairs.add(new BasicNameValuePair("cc", this.mCheckBoxCopie.isChecked()?"1":"0"));
			nameValuePairs.add(new BasicNameValuePair("id", this.mOffreId));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			Log.i("lbc", "reponse send email.................." + response.getEntity().toString());

		} catch (ClientProtocolException e) {
			Log.e("lbc", e.getMessage());
		} catch (IOException e) {
			Log.e("lbc", e.getMessage());
		}
	}


	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.textview_cc:
			this.mCheckBoxCopie.setChecked(!this.mCheckBoxCopie.isChecked());
			
			break;
		default:
			break;
		}
	}
}