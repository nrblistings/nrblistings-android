package com.nrblistings.activities.details;

import android.app.Activity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.TextView;

import com.nrblistings.R;
import com.nrblistings.NrbListingsParameters.DataType;
import com.nrblistings.core.Event;

public class EventDetailsActivity extends Activity {

	public static final String TAG = "EventDetailsActivity";
	
	// ------------------------------------------------------------------------
	// Life Cycle Methods
	// ------------------------------------------------------------------------
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_details);
		
		Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Event event = extras.getParcelable(DataType.Events.toString());
            ((TextView)findViewById(R.id.event_details_name)).setText(event.getName());
            ((TextView)findViewById(R.id.event_details_price)).setText(event.getPrice());
            ((TextView)findViewById(R.id.event_details_description)).setText(event.getDescription());
            
            TextView tv = (TextView)findViewById(R.id.event_details_webpage);
            tv.setText(event.getUrl());
            Linkify.addLinks(tv, Linkify.WEB_URLS);
        } else {
        	Log.e(TAG, "No event passed in Bundle.");
        	finish();
        }
	}
}
