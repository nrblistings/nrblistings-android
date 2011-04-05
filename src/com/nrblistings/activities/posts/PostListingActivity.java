package com.nrblistings.activities.posts;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.http.client.CommonsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.nrblistings.R;
import com.nrblistings.core.Listing;
import com.nrblistings.util.LocationUtil;

public class PostListingActivity extends Activity {

	public static final String TAG = "PostListingActivity";
	
	protected static final int CAMERA_REQUEST_CODE = 0;
	protected static final int CAMERA_RESULT_OK = -1;
	
	protected static final String PHOTO_TAKEN = "photo_taken";
	
	// ------------------------------------------------------------------------
	// Member Variables
	// ------------------------------------------------------------------------
	
	protected String mImagePath;
	protected boolean mImageTaken = false;
	
	// ------------------------------------------------------------------------
	// Life Cycle Methods
	// ------------------------------------------------------------------------
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_listing);
		
		mImagePath =  Environment.getExternalStorageDirectory() + "/" + TAG + ".jpg";
		
		boolean externalStorageAvailable = false;
		boolean externalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    externalStorageAvailable = externalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    externalStorageAvailable = true;
		    externalStorageWriteable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    externalStorageAvailable = externalStorageWriteable = false;
		}
		
		Button photoButton = (Button) findViewById(R.id.post_listing_take_photo);
		if (externalStorageAvailable && externalStorageWriteable) {
			photoButton.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View view) {
	            	startCameraActivity();
	            }
	        });
		} else {
			photoButton.setVisibility(View.GONE);
			((ImageView) findViewById(R.id.post_listing_image)).setVisibility(View.GONE);
		}
		
		Button confirmButton = (Button) findViewById(R.id.post_listing_confirm);
		confirmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	postListing();
            	finish();
            }
        });
	}
	
	@Override
	protected void onSaveInstanceState( Bundle outState ) {
	    outState.putBoolean(PostListingActivity.PHOTO_TAKEN, mImageTaken);
	}
	@Override
	protected void onRestoreInstanceState( Bundle savedInstanceState) {
	    if( savedInstanceState.getBoolean(PostListingActivity.PHOTO_TAKEN) ) {
	    	onPhotoTaken();
	    }
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			File f = new File(mImagePath);
			if (f.exists()) {
				f.delete();
			}
		} catch (Exception e) {}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Toast.makeText(this, "Request code: " + requestCode + " resultCode: " + resultCode, Toast.LENGTH_LONG).show();
		switch(requestCode) {
	    	case CAMERA_REQUEST_CODE:
	    		if (resultCode == CAMERA_RESULT_OK) {
	    			onPhotoTaken();
	    		}
	    		break;
	    	default:
	    		break;
	    }
	}
	
	// ------------------------------------------------------------------------
	// Protected/Private Methods
	// ------------------------------------------------------------------------

	protected void startCameraActivity() {
		
		// Create parameters for Intent with filename
//		ContentValues values = new ContentValues();
//		values.put(MediaStore.Images.Media.TITLE, mImagePath);
//		values.put(MediaStore.Images.Media.DESCRIPTION,"Image capture by camera");

		
		File file = new File(mImagePath);
		Uri outputFileUri = Uri.fromFile(file);
		
		Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    i.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
	    i.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
	    startActivityForResult(i, CAMERA_REQUEST_CODE);
	}
	
	protected void onPhotoTaken() {
	    BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inSampleSize = 4;
	    
	    Uri selectedImage = Uri.fromFile(new File(mImagePath));
        getContentResolver().notifyChange(selectedImage, null);
        ImageView imageView = (ImageView) findViewById(R.id.post_listing_image);
        ContentResolver cr = getContentResolver();
        Bitmap bitmap;
        try {
        	bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, selectedImage);
            imageView.setImageBitmap(bitmap);
            Toast.makeText(this, selectedImage.toString(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
        }
        

//	    Bitmap bitmap = BitmapFactory.decodeFile(mImagePath, options);
//	    ((ImageView)findViewById(R.id.post_listing_image)).setImageBitmap(bitmap);
	    mImageTaken = true;
	}
	
	/**
	 * Post the listing to the website
	 */
	protected void postListing() {
		
		// Setup the RestTemplate
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(new CommonsClientHttpRequestFactory());
		List<HttpMessageConverter<?>> listHttpMessageConverters = restTemplate.getMessageConverters();
		listHttpMessageConverters.add(new MappingJacksonHttpMessageConverter());			
		restTemplate.setMessageConverters(listHttpMessageConverters);
		
		GeoPoint geoPoint = LocationUtil.getGeoPoint((LocationManager) getSystemService(Context.LOCATION_SERVICE));
		
		// Provide the variables for lat, long, and radius
		Map<String, String> variables = new HashMap<String, String>();
		variables.put("latitude", Double.toString(geoPoint.getLatitudeE6()/1E6));
		variables.put("longitude", Double.toString(geoPoint.getLongitudeE6()/1E6));
		variables.put("name", ((TextView)findViewById(R.id.post_listing_name)).getText().toString());
		variables.put("price", ((TextView)findViewById(R.id.post_listing_price)).getText().toString());
		//variables.put("description", ((TextView)findViewById(R.id.post_listing_description)).getText().toString());
		
		Listing l = new Listing();
		l.setName(((TextView)findViewById(R.id.post_listing_name)).getText().toString());
		l.setPrice(((TextView)findViewById(R.id.post_listing_price)).getText().toString());
		l.setDescription(((TextView)findViewById(R.id.post_listing_description)).getText().toString());
		l.setLatitude(geoPoint.getLatitudeE6()/1E6);
		l.setLongitude(geoPoint.getLongitudeE6()/1E6);
		
		try {
			Log.v(TAG, "Performing post");
			// WORKS!
//			URI uri = restTemplate.postForLocation(
//					LocationMatrixParameters.POST_LISTING_URL, null, 
//					((TextView)findViewById(R.id.post_listing_name)).getText().toString(), 
//					((TextView)findViewById(R.id.post_listing_price)).getText().toString(),
//					((TextView)findViewById(R.id.post_listing_description)).getText().toString(),
//					Double.toString(geoPoint.getLatitudeE6()/1E6),
//					Double.toString(geoPoint.getLongitudeE6()/1E6));
			
			
//			HttpClient httpClient = new DefaultHttpClient();
//            try {
//            	httpClient.getParams().setParameter("http.socket.timeout", new
//            			Integer(90000)); // 90 second
//            	HttpPost post = new HttpPost(new URI("http://128.31.4.188:8080/nrbListings-0.1/productListing/"));
//            	File file = new File(filepath);
//            	FileEntity entity;
//            	if (filepath.substring(filepath.length()-3, filepath.length()).equalsIgnoreCase("txt") ||
//                            filepath.substring(filepath.length()-3, filepath.length()).equalsIgnoreCase("log")) {
//            		entity = new FileEntity(file,"text/plain; charset=\"UTF-8\"");
//            		entity.setChunked(true);
//            	} else {
//            		entity = new FileEntity(file,"binary/octet-stream");
//            		entity.setChunked(true);
//            	}
//            	post.setEntity(entity);
//            	post.addHeader(FILENAME_STR, filename);
//
//            	HttpResponse response = httpClient.execute(post);
//            	if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
//            		Log.e(TAG,"--------Error--------Response Status linecode:"+response.getStatusLine());
//            	} else {
//            		// Here every thing is fine.
//            	} 
//            	HttpEntity resEntity = response.getEntity();
//            	if (resEntity == null) {
//            		Log.e(TAG,"---------Error No Response !!!-----");
//            	}
//            } catch (Exception ex) {
//            	Log.e(TAG,"---------Error-----"+ex.getMessage());
//            	ex.printStackTrace();
//            } finally {
//            	httpClient.getConnectionManager().shutdown();
//            }
			
			//----
            
			HttpClient client = new DefaultHttpClient();
			//client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
			 
			HttpPost post = new HttpPost("http://128.31.4.188:8080/nrbListings-0.1/productListing/");
			MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			 
			// For File parameters
			if (mImageTaken && new File(mImagePath).exists()) {
				entity.addPart("img", new FileBody((new File(mImagePath)), "image/jpeg" ));
			}
			 
			// For usual String parameters
			entity.addPart("name", new StringBody(((TextView)findViewById(R.id.post_listing_name)).getText().toString(), "text/plain", Charset.forName( "UTF-8" )));
			entity.addPart("price", new StringBody(((TextView)findViewById(R.id.post_listing_price)).getText().toString(), "text/plain", Charset.forName( "UTF-8" )));
			entity.addPart("description", new StringBody(((TextView)findViewById(R.id.post_listing_description)).getText().toString(), "text/plain", Charset.forName( "UTF-8" )));
			entity.addPart("latitude", new StringBody(Double.toString(geoPoint.getLatitudeE6()/1E6), "text/plain", Charset.forName( "UTF-8" )));
			entity.addPart("longitude", new StringBody(Double.toString(geoPoint.getLongitudeE6()/1E6), "text/plain", Charset.forName( "UTF-8" )));
			post.setEntity( entity );
			 
			// Here we go!
			//String response = EntityUtils.toString( client.execute( post ).getEntity(), "UTF-8" );
			client.getConnectionManager().shutdown();
			
			Toast.makeText(this, "Successfully Posted " + ((TextView)findViewById(R.id.post_listing_name)).getText().toString(), Toast.LENGTH_SHORT).show();
			
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		
	}
}
