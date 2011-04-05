package com.nrblistings.activities.lists;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nrblistings.R;
import com.nrblistings.NrbListingsParameters.DataType;

/**
 * This Activity will be the main entry point to the app and will display
 * the counts for each type of data. Selecting the type will then open
 * the Activity associated with that particular data type.
 * 
 * @author prschmid
 *
 */
public class ListingsCountActivity extends AbstractListingsActivity<Object> {
	
	// ------------------------------------------------------------------------
	// Constants
	// ------------------------------------------------------------------------

	/** The tag for this class */
	public static final String TAG = "ListingsCountActivity";
	
	// ------------------------------------------------------------------------
	// Life Cycle Methods
	// ------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.listing_count_list);
		super.onCreate(savedInstanceState);
	}
    
	// ------------------------------------------------------------------------
	// Overridden Methods from AbstractListingActivity
	// ------------------------------------------------------------------------
    
	/**
     * Setup the list adapter.
     */
    protected void setupListAdapter() {
    	mListingAdapter = new TypeCountListingAdapter(this, R.layout.listing_count_row, mListings);
    }
    
    /**
	 * This method does the work of filling the ListView with the data in
	 * mListings.
	 */
    public void updateListings() {
    	if (mService != null) {
    		Map<DataType, Integer> counts = mService.getListingCounts();
    		if (counts != null) {
    			mListingAdapter.notifyDataSetChanged();
    			mListings.clear();		
    			for (Map.Entry<DataType, Integer> entry : counts.entrySet()) {
    				mListings.add(new TypeCount(entry.getKey(), entry.getValue()));
    			}
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
        
        // Figure out what it was that was clicked
        TextView typeTv = (TextView) v.findViewById(R.id.listing_count_type);
        DataType type = null;
        if (typeTv != null) {
        	type = DataType.valueOf(typeTv.getText().toString());
        }
        
        if (type != null) {
        	switch (type) {
        		case Events:
        			startActivity(new Intent(this, EventListingsActivity.class));
        			break;
        		case Listings:
        			startActivity(new Intent(this, ListingListingsActivity.class));
        			break;
        	}
        }
    }
	
    // ------------------------------------------------------------------------
	// Private Classes
	// ------------------------------------------------------------------------
    
    /**
     * A ArrayAdapter that we'll use to make the Event Events show up in 
     * the ListView.
     * 
     * @author prschmid
     *
     */
    private class TypeCountListingAdapter extends ArrayAdapter<Object> {
    	
    	/** A local reference to the items */
    	private List<Object> items;
    	
    	/** The resource ID for this View */
    	private int textViewResourceId;
    	
    	/**
    	 * Create a new Adapter. Although the Objects are passed to the 
    	 * constructor, note that this does not make a local copy and
    	 * only is a reference to the list of events that is held by
    	 * the EventListingActivity.
    	 * @param context
    	 * @param textViewResourceId
    	 * @param items
    	 */
		public TypeCountListingAdapter(Context context, int textViewResourceId, List<Object> items) {
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
			Object o = items.get(position);
			if (o != null) {
				TextView tt = (TextView) v.findViewById(R.id.listing_count_count);
				TextView bt = (TextView) v.findViewById(R.id.listing_count_type);
				if (tt != null) {
					tt.setText(((TypeCount)o).count + "");
				}
				if (bt != null){
					bt.setText(((TypeCount)o).type.toString());
				}
			}
			return v;
		}
	}

    private class TypeCount {
    	public TypeCount(DataType type, int count) {
    		this.type = type;
    		this.count = count;
    	}
    	DataType type;
    	int count;
    }
}
