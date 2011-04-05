package com.nrblistings.activities.lists;

import java.util.List;

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
import com.nrblistings.activities.details.EventDetailsActivity;
import com.nrblistings.core.Event;

public class EventListingsActivity extends AbstractListingsActivity<Event> {
		
	// ------------------------------------------------------------------------
	// Life Cycle Methods
	// ------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.events_list);
		super.onCreate(savedInstanceState);
	}
	
	// ------------------------------------------------------------------------
	// Overridden Methods from AbstractListingActivity
	// ------------------------------------------------------------------------
    
	/**
     * Setup the list adapter.
     */
    protected void setupListAdapter() {
    	mListingAdapter = new EventListingAdapter(this, R.layout.events_row, mListings);
    }
    
    /**
	 * This method does the work of filling the ListView with the data in
	 * mListings.
	 */
    public void updateListings() {
    	if (mService != null) {
    		List<Event> events = mService.getEvents();
    		if (events != null) {
    			mListingAdapter.notifyDataSetChanged();
    			mListings.clear();
    			mListings.addAll(events);
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
        Event al = mListings.get((int)id);
        Intent i = new Intent(this, EventDetailsActivity.class);
        i.putExtra(DataType.Events.toString(), al);
        startActivity(i);
    }
	
    // ------------------------------------------------------------------------
	// Private Classes
	// ------------------------------------------------------------------------
    
    /**
     * A ArrayAdapter that we'll use to make the Event objects show up in 
     * the ListView.
     * 
     * @author prschmid
     *
     */
    private class EventListingAdapter extends ArrayAdapter<Event> {
    	
    	/** A local reference to the items */
    	private List<Event> items;
    	
    	/** The resource ID for this View */
    	private int textViewResourceId;
    	
    	/**
    	 * Create a new Adapter. Although the events are passed to the 
    	 * constructor, note that this does not make a local copy and
    	 * only is a reference to the list of events that is held by
    	 * the EventListingActivity.
    	 * @param context
    	 * @param textViewResourceId
    	 * @param items
    	 */
		public EventListingAdapter(Context context, int textViewResourceId, List<Event> items) {
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
			Event o = items.get((int)position);
			if (o != null) {
				TextView tt = (TextView) v.findViewById(R.id.event_date);
				TextView bt = (TextView) v.findViewById(R.id.event_description);
				if (tt != null) {
					tt.setText("Date:"+o.getStartDate());
				}
				if(bt != null){
					bt.setText("Summary: "+ o.getDescription());
				}
			}
			return v;
		}
	}
}
