package com.nhatnam.android.leboncoin.fragments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nhatnam.android.leboncoin.R;
import com.nhatnam.android.leboncoin.utils.Utils;

public class DashDeposeFragment extends SherlockFragment implements OnClickListener, android.content.DialogInterface.OnClickListener, OnScanCompletedListener
{
	
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE 	= 0;
	private static final int PICKUP_IMAGE_ACTIVITY_REQUEST_CODE 	= 1;
	
	private BasicHttpContext mHttpContext;
	private String cookieToSet = null;
	private Button mInsertPhotos;
	private ArrayList<Uri> urisPhotos = new ArrayList<Uri>(3);
	private Uri tmpFile;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.depose_offre_activity, container, false);
	}
	
	
	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);
        
        /** Check if this device has a camera */
        this.mInsertPhotos = (Button) this.getView().findViewById(R.id.depose_insert_photos);
        if (this.getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
        {
	        this.mInsertPhotos.setOnClickListener(this);
        } else {
        	this.mInsertPhotos.setVisibility(View.GONE);
        }
        
        
    }


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_depose, menu);
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
            case R.id.menu_depose_ok:
            	new Thread(new Runnable() {
					
					@Override
					public void run() {
						getCookieToSubmitOffre();
						
						uploadPhotos();
					}
				}).start();
            	
 		        return true;
            default:
                return false;
        }
    }


	private void getCookieToSubmitOffre() {

		String url = "http://www2.leboncoin.fr/ai/form/1";
		Log.i("lbc", "Fetch database detail offre from ............: " + url);

		try {
			HttpClient httpclient = new DefaultHttpClient();
			mHttpContext = new BasicHttpContext();
			CookieStore mCookieStore      = new BasicCookieStore();        
			mHttpContext.setAttribute(ClientContext.COOKIE_STORE, mCookieStore);
			
			HttpResponse response = httpclient.execute(new HttpGet(url), mHttpContext);
			Header headersCookie[] = response.getHeaders("Set-Cookie");
			if (headersCookie!=null && headersCookie.length > 0) {
				cookieToSet = headersCookie[0].getValue().substring(2);
				Log.i("lbc", "Cookie to set when submitting offre .....................: " + cookieToSet);
			}
		} catch (Exception e) {
			Log.i("lbc", "Network exception: ", e);
		}
	}
	
	
	private void uploadPhotos() {
		String url = "http://www2.leboncoin.fr/ai/form/1";
		
		try {
		    DefaultHttpClient httpclient = new DefaultHttpClient();

		    HttpPost httppost = new HttpPost(url);

//		    CookieStore cookieStore = httpclient.getCookieStore();
//		    BasicClientCookie cookie = new BasicClientCookie("s", this.cookieToSet);
//		    cookie.setDomain(url);
//		    cookieStore.addCookie(cookie);

		    File file = new File(Environment.getExternalStorageDirectory(),
			        "pictures/LeBonCoinApp/4.jpg");
		    InputStreamEntity reqEntity = new InputStreamEntity(
		            new FileInputStream(file), -1);
		    reqEntity.setContentType("binary/octet-stream");
		    reqEntity.setChunked(true); // Send in multiple parts if needed
		    
		    httppost.setEntity(reqEntity);
		    HttpResponse response = httpclient.execute(httppost, mHttpContext);
		    
		    //InputStream inputStream = response.getEntity().getContent();
		    //Do something with response...
		    
		    /*Print all*/
		    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            final StringBuilder out = new StringBuilder();
            String line;
            try {
                while ((line = rd.readLine()) != null) {
                    out.append(line);
                }
                rd.close();
            }
            catch (Exception e) {
            	Log.i("lbc", "error when print response: " + e);
            }
            Log.i("lbc", "serverResponse: " + out.toString());
            /*Fin Print all*/
            

		} catch (Exception e) {
			Log.i("lbc", "uploadphotos exception: ", e);
		}
	}
	
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.depose_insert_photos:
			Log.i("lbc", "Take photo or Choose photo");
			//Create list localisation dialog
	    	LeboncoinDialogFragment takeOrChoosePhotoFragment = LeboncoinDialogFragment.newInstance(this, LeboncoinDialogFragment.DIALOG_TAKEORCHOOSEPHOTO_ID);
	    	takeOrChoosePhotoFragment.show(this.getSherlockActivity().getSupportFragmentManager(), "DialogTakeOrChoosePhoto");
		    
	    	break;
		default:
			break;
		}
	}


	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case 0:
			//TODO Take photo
			// create Intent to take a picture and return control to the calling application
		    Intent intentCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		    Uri fileUri = getOutputMediaFileUri(); // create a file to save the image
		    intentCapture.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

		    // start the image capture Intent
		    startActivityForResult(intentCapture, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			
			break;
		default:
			Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		    // start the image pickup Intent
		    startActivityForResult(pickPhoto, PICKUP_IMAGE_ACTIVITY_REQUEST_CODE);
			
			break;
		}
	}
	
	
	/** Create a file Uri for saving an image or video */
	private Uri getOutputMediaFileUri(){
	      return Uri.fromFile(getOutputMediaFile());
	}

	/** Create a File for saving an image or video */
	private File getOutputMediaFile(){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "LeBonCoinApp");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("lbc", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    tmpFile = Uri.parse(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");
	    File mediaFile = new File(tmpFile.getPath());

	    return mediaFile;
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (resultCode) {
			case Activity.RESULT_OK :
				final List<Parcelable> uris;
				Uri uri = null;
				switch (requestCode) {
	    			case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE :
	
	    				if (null != intent) {
	    					try {
	    						if (null == (uri = intent.getData())) {
	    							Bitmap photo = (Bitmap)intent.getExtras().get("data");
	
	    							if (null != photo) {
	    								FileOutputStream stream = null;
	
//	    								try {
//	    									photo.compress(CompressFormat.JPEG, 70, stream = FileUtils.openOutputStream(this.tmpFile));
//	
//	    									final int degrees = ImageUtils.getExifOrientation(this.tmpFile.getPath());
//	
//	    									if (0 != degrees) {
//	    										photo = ImageUtils.rotate(ImageUtils.createThumbnailFromEXIF(this.tmpFile.getPath(),
//					    																				 	 ImageUtils.UNCONSTRAINED,
//					    																				 	 ImageUtils.MAX_NUM_PIXELS_THUMBNAIL),
//					    												  degrees);
//	
//	    										photo.compress(CompressFormat.PNG, 70, stream);
//	    									}
//	    								}
//	    								finally {
//	    									IOUtils.closeQuietly(stream);
//	    									ImageUtils.recycleSilently(photo);
//	    								}
	    							}
	    						}
	    					}
	    					catch (final Throwable ex) {
	    						Log.e("lbc", ex.getMessage());
	    					}
	    				}
	    				
	    				if (null == uri || TextUtils.isEmpty(uri.toString())) {
	    					MediaScannerConnection.scanFile(this.getActivity(), new String[] {this.tmpFile.getPath()}, null, this);
	    				}
	    				else {
	    					urisPhotos.add(uri);
	    				}
	    					
	    				break;
	    			case PICKUP_IMAGE_ACTIVITY_REQUEST_CODE:
	    				
	    				if (intent!=null) {
		    				uri = intent.getData();
		    				if (uri!=null) {
		    					
		    					Utils.applyBitmapForImageViewFromUri(this.getActivity(), (ImageView) this.getView().findViewById(R.id.depose_image_1), uri);
		    				} else {
		    					Log.e("lbc", "uri of pickup phot is null ...........");
		    				}
	    				} else {
	    					Log.e("lbc", "intent of pickup phot is null ...........");
	    				}
	    				break;
	    			default :
	    				break;
				}
				break;
			default :
				break;
		}
	}
	

	@Override
	public void onScanCompleted(String path, Uri uri) {
		urisPhotos.add(uri);
		
		Utils.applyBitmapForImageViewFromUri(this.getActivity(), (ImageView) this.getView().findViewById(R.id.depose_image_1), uri);
	}
}
