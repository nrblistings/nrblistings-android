package com.nrblistings.activities.lists;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nrblistings.R;
import com.nrblistings.NrbListingsParameters.DataType;
import com.nrblistings.activities.details.ListingDetailsActivity;
import com.nrblistings.core.Listing;
import com.nrblistings.util.AbstractFetchThumbnailImage;

public class ListingListingsActivity extends AbstractListingsActivity<Listing> {
	
	// ------------------------------------------------------------------------
	// Life Cycle Methods
	// ------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.listings_list);
		super.onCreate(savedInstanceState);
	}
	
	// ------------------------------------------------------------------------
	// Overridden Methods from AbstractListingActivity
	// ------------------------------------------------------------------------
    
	/**
     * Setup the list adapter.
     */
    protected void setupListAdapter() {
    	mListingAdapter = new ListingListingAdapter(this, R.layout.listings_row, mListings);
    }
    
    /**
	 * This method does the work of filling the ListView with the data in
	 * mListings.
	 */
    public void updateListings() {
    	if (mService != null) {
    		List<Listing> listings = mService.getListings();
    		if (listings != null) {
    			mListingAdapter.notifyDataSetChanged();
    			mListings.clear();
    			mListings.addAll(listings);
    			mListingAdapter.notifyDataSetChanged();
    		}
    	}
    }
    
    // ------------------------------------------------------------------------
	// Callback Methods
	// ------------------------------------------------------------------------
	
	/*
	 * (non-Javadoc)
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Listing al = mListings.get((int)id);
        Intent i = new Intent(this, ListingDetailsActivity.class);
        i.putExtra(DataType.Listings.toString(), al);
        startActivity(i);
    }
	
    // ------------------------------------------------------------------------
	// Private Classes
	// ------------------------------------------------------------------------
    
    /**
     * A ArrayAdapter that we'll use to make the Listing objects show up in 
     * the ListView.
     * 
     * @author prschmid
     *
     */
    private class ListingListingAdapter extends ArrayAdapter<Listing> {
    	
    	/** A local reference to the items */
    	private List<Listing> items;
    	
    	/** The resource ID for this View */
    	private int textViewResourceId;
    	
    	/**
    	 * Create a new Adapter. Although the events are passed to the 
    	 * constructor, note that this does not make a local copy and
    	 * only is a reference to the list of events that is held by
    	 * the ListingListingActivity.
    	 * @param context
    	 * @param textViewResourceId
    	 * @param items
    	 */
		public ListingListingAdapter(Context context, int textViewResourceId, List<Listing> items) {
			super(context, textViewResourceId, items);
			this.items = items;
			this.textViewResourceId = textViewResourceId;
		}
	
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
			    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			    v = vi.inflate(textViewResourceId, null);
			}
			Listing listing = items.get(position);
			if (listing != null) {
				((TextView) v.findViewById(R.id.listing_name)).setText("Name: " + listing.getName());
				((TextView) v.findViewById(R.id.listing_description)).setText("Summary: " + listing.getDescription());
				
				String thumbnailUrl = listing.getImgUrl();
	            if (thumbnailUrl != null && !thumbnailUrl.equals("")) {
	            	new FetchThumbnailImage(((ImageView) v.findViewById(R.id.listing_image))).execute(thumbnailUrl);
	            }
			}
			return v;
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
				this.iv.setImageDrawable(drawable);
			}
		}
	}
}
