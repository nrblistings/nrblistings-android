package com.nrblistings.activities.lists;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.nrblistings.services.NrbListingsService;

/**
 * An abstract class that does the work of implementing the logic for updating
 * a list with data from the website. Subclasses just need to implement how
 * the ListView is going to be populated.
 * 
 * @author prschmid
 *
 * @param <T>
 */
public abstract class AbstractListingsActivity<T> extends ListActivity {
	
	public static final String TAG = "AbstractListingsActivity";
	
	// ------------------------------------------------------------------------
	// Member Variables
	// ------------------------------------------------------------------------
	
	/** The listings **/
	protected List<T> mListings = new ArrayList<T>();;
	
	/** The adapter to place all of the items in the list */
	protected ArrayAdapter<T> mListingAdapter = null;
	
	/** A reference to the LocationMatrixService that we bind to */
	protected NrbListingsService mService = null;
	
	/** A ServiceConnection to the LocationMatrixService */
	private ServiceConnection mServiceConnection = new ServiceConnection() { 
		public void onServiceConnected(ComponentName className, IBinder rawBinder) {
			mService = ((NrbListingsService.LocalBinder)rawBinder).getService();
			updateListings();
		}
		public void onServiceDisconnected(ComponentName className) {
			mService = null;
		}
	};
	
	/** A BroadcastReceiver for broadcasts from the LocationMatrixService */
	private BroadcastReceiver mReceiver = new BroadcastReceiver() { 
		public void onReceive(Context context, Intent intent) {
			updateListings();
		} 
	};
	
	// ------------------------------------------------------------------------
	// Life Cycle Methods
	// ------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.v(TAG, "onCreate(): " + this.getLocalClassName());
		
		// Bind to the listing service
		bindService(
				new Intent(this, NrbListingsService.class), 
				mServiceConnection, 
				Context.BIND_AUTO_CREATE);
		
		// Setup the list of things that will be displayed and the adapter to 
		// use to display them
		setupListAdapter();
		setListAdapter(mListingAdapter);		
	}
    
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override 
    public void onResume() {
    	super.onResume(); 
    	registerReceiver(mReceiver, new IntentFilter(NrbListingsService.BROADCAST_ACTION));
    }
    
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onPause()
     */
    @Override 
    public void onPause() {
    	super.onPause(); 
    	unregisterReceiver(mReceiver);
    }
    
    /*
     * (non-Javadoc)
     * @see android.app.ListActivity#onDestroy()
     */
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	Log.v(TAG, "onDestroy(): " + this.getLocalClassName());
    	unbindService(mServiceConnection);
    }
	
	// ------------------------------------------------------------------------
	// Public Abstract Methods
	// ------------------------------------------------------------------------
	
	/**
	 * This method does the work of filling the ListView with the data in
	 * mListings.
	 */
	public abstract void updateListings();
    
    // ------------------------------------------------------------------------
	// Protected Abstract Methods
	// ------------------------------------------------------------------------
    
    /**
     * Setup the list adapter.
     */
    protected abstract void setupListAdapter();
}
