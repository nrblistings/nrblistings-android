package com.nrblistings.activities.details;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.nrblistings.R;
import com.nrblistings.NrbListingsParameters.DataType;
import com.nrblistings.core.Listing;
import com.nrblistings.util.AbstractFetchThumbnailImage;

public class ListingDetailsActivity extends Activity {

	public static final String TAG = "ListingDetailsActivity";
	
	// ------------------------------------------------------------------------
	// Life Cycle Methods
	// ------------------------------------------------------------------------
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listing_details);
		
		Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Listing listing = extras.getParcelable(DataType.Listings.toString());
            ((TextView)findViewById(R.id.listing_details_name)).setText(listing.getName());
            ((TextView)findViewById(R.id.listing_details_price)).setText(listing.getPrice());
            ((TextView)findViewById(R.id.listing_details_description)).setText(listing.getDescription());
            
            TextView tv = (TextView)findViewById(R.id.listing_details_webpage);
            tv.setText(listing.getUrl());
            Linkify.addLinks(tv, Linkify.WEB_URLS);
            
            // If the listing has a thumbnail, go download it
            String thumbnailUrl = listing.getImgUrl();
            if (thumbnailUrl != null && !thumbnailUrl.equals("")) {
            	new FetchThumbnailImage(((ImageView)findViewById(R.id.listing_details_image))).execute(thumbnailUrl);
            }
        } else {
        	Log.e(TAG, "No listing passed in Bundle.");
        	finish();
        }
	}
	
	private class FetchThumbnailImage extends AbstractFetchThumbnailImage {
		private ImageView iv;
		public FetchThumbnailImage(ImageView iv) {
			this.iv = iv;
		}
		@Override
		protected void onPostExecute(Drawable drawable) {
			if (drawable != null) {
				iv.setImageDrawable(drawable);
			}
		}
	}
}
