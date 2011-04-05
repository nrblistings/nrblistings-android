package com.nrblistings.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.nrblistings.NrbListingsParameters;
import com.nrblistings.NrbListingsParameters.DataType;
import com.nrblistings.core.AbstractListing;
import com.nrblistings.core.Event;
import com.nrblistings.core.Listing;
import com.nrblistings.util.FetchListingsAsyncTask;
import com.nrblistings.util.LocationUtil;

/**
 * The Service that does all of the work of getting the data from the 
 * web and populating the list data that can be displayed. It sends out a
 * broadcast to alert any Activity that is bound to it of any updates.
 * 
 * @author prschmid
 *
 */
public class NrbListingsService extends Service implements LocationListener {

	// ------------------------------------------------------------------------
	// Constants
	// ------------------------------------------------------------------------
	
	/** The broadcast action for updated events */
	public static final String BROADCAST_ACTION = "com.locationmatrix.service.ListingUpdate";
	
	/** A Binder that simply returns this */
	public class LocalBinder extends Binder { 
		public NrbListingsService getService() {
			return NrbListingsService.this;
		}
	}
	
	/** The tag for this class */
	public static final String TAG = "LocationMatrixService";
	
	// ------------------------------------------------------------------------
	// Member Variables
	// ------------------------------------------------------------------------

	/** A broadcast Intent */
	private Intent mBroadcast = new Intent(BROADCAST_ACTION);
	
	/** A binder that is returned to any Activity that binds to this */
	private final IBinder mBinder = new LocalBinder();
	
	/** A timer for periodic checking of the data on the web */
	private static Timer mTimer = null;
	
	/** The Handler to use to handle the tasks of getting the data from the web */
	private final Handler mFetchListingsHandler = new Handler();
		
	/** A map that goes from the type to the listings of that type */
	private static Map<DataType, List<? extends AbstractListing>> mType2listings = 
		new HashMap<DataType, List<? extends AbstractListing>>();
	
	/** The number of milliseconds to wait between queries */
	private static final int LISTING_QUERY_INTERVAL = 1000 * 10;
	
	/** The  number of milliseconds to wait between location queries */
	private static final int LOCATION_QUERY_INTERVAL = 1000 * 60;
	
	/** A local reference to the LocationManager */
	private LocationManager mLocationManager;
	
	/** The current best-guess to the actual location */
	private Location mActualLocation = null;
	
	/** The location the user said to use */
	private GeoPoint mCustomLocation = null;
	
	/** The radius to query data for */
	private int mRadius = NrbListingsParameters.DEFAULT_RADIUS;
	
	// ------------------------------------------------------------------------
	// Life Cycle Methods
	// ------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intnet) {
		Log.v(TAG, "onBind()");
		return(mBinder);
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		
		Toast.makeText(this, "Service Started ...", Toast.LENGTH_SHORT).show();
		Log.v(TAG, "Starting service");
		
		// Acquire a reference to the system Location Manager
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		// Get the last known location to start things off
		mActualLocation = LocationUtil.getLocation(mLocationManager);

		// Start requesting location updates
		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_QUERY_INTERVAL, 0, this);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_QUERY_INTERVAL, 0, this);
		
		// Start the background timer to fetch the data
		if (mTimer == null) {
			mTimer = new Timer();
		}
		mTimer.scheduleAtFixedRate(new FetchListingTimerTask(), 0, LISTING_QUERY_INTERVAL);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		Toast.makeText(this, "Service Stopped ...", Toast.LENGTH_SHORT).show();
		Log.v(TAG, "Stopping service");
		mLocationManager.removeUpdates(this);
		mTimer.cancel();
		mTimer = null;
	}
	
	// ------------------------------------------------------------------------
	// Public Methods
	// ------------------------------------------------------------------------
	
	/**
	 * Get all of the Events
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Event> getEvents() {
		return (List<Event>)getListings(DataType.Events);
	}
	
	/**
	 * Get all of the Listings
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Listing> getListings() {
		return (List<Listing>)getListings(DataType.Listings);
	}
	
	/**
	 * Get the number of listings for each DataType. If the resulting map is
	 * missing a DataType, it means the count is 0.
	 * @return
	 */
	public Map<DataType, Integer> getListingCounts() {
		Map<DataType, Integer> counts = new HashMap<DataType, Integer>();
		for(Map.Entry<DataType, List<? extends AbstractListing>> entry : mType2listings.entrySet()) {
			counts.put(entry.getKey(), entry.getValue().size());
		}
		return counts;
	}
	
	/**
	 * Get the listings of the given type. This is guaranteed to return a 
	 * non-null list. If there is nothing of that type, it will return 
	 * an empty list.
	 * @param type
	 * @return
	 */
	public List<? extends AbstractListing> getListings(DataType type) {
		List<? extends AbstractListing> l = mType2listings.get(type);
		if (l == null) {
			return new ArrayList<AbstractListing>();
		} else {
			return l;
		}
	}
	
	/**
	 * Requery for events.
	 */
	public void forceEventsRequery() {
		forceRequery(DataType.Events);
	}
	
	/**
	 * Requery for listings.
	 */
	public void forceListingsRequery() {
		forceRequery(DataType.Listings);
	}
	
	/**
	 * Force the requerying of all data types.
	 */
	public void forceRequery() {
		for (DataType type : DataType.values()) {
    		forceRequery(type);
    	}
	}

	/**
	 * Force the requerying of the given data type
	 * @param type
	 */
	public void forceRequery(DataType type) {
		Log.v(TAG, "Forcing query");
		
		if (mActualLocation != null) {
			switch(type) {
			case Events:
				new FetchListingsTask<Event>(
						NrbListingsParameters.TYPE_2_URL.get(DataType.Events), 
						Event[].class,
						LocationUtil.locationToGeoPoint(mActualLocation),
						DataType.Events).execute();
				break;
			case Listings:
				new FetchListingsTask<Listing>(
						NrbListingsParameters.TYPE_2_URL.get(DataType.Listings), 
						Listing[].class,
						LocationUtil.locationToGeoPoint(mActualLocation),
						DataType.Listings).execute();
				break;
			}
		} else {
			Log.w(TAG, "Cannot force requery. Not location available.");
		}
	}
	
	/**
	 * Get the location (as a GeoPoint) for where the listings are for
	 * @return
	 */
	public GeoPoint getLocation() {
		if (mCustomLocation != null) {
			return mCustomLocation;
		} else if (mActualLocation != null) {
			return LocationUtil.locationToGeoPoint(mActualLocation);
		} else {
			return null;
		}
	}
	
	/**
	 * Set the location (as a GeoPoint) for where the listings should be
	 * obtained for. This will force a requery of the data.
	 * @param geoPoint
	 */
	public void seCustomLocation(GeoPoint geoPoint) {
		mCustomLocation = geoPoint;
		forceRequery();
	}
	
	/**
	 * Unset the custom location for where the listings should be obtained for 
	 * and use the actual location. This will force a requery of the data.
	 */
	public void unsetCustomLocation() {
		mCustomLocation = null;
		forceRequery();
	}
	
	// ------------------------------------------------------------------------
	// Callback Methods for LocationListener
	// ------------------------------------------------------------------------
	
	/*
	 * (non-Javadoc)
	 * @see android.location.LocationListener#onLocationChanged(android.location.Location)
	 */
	public void onLocationChanged(Location location) {
		if (LocationUtil.isBetterLocation(location, this.mActualLocation)) {
			mActualLocation = location;
			if (mCustomLocation == null) {
				forceRequery();
			}
		}
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {}
	public void onProviderEnabled(String provider) {}
	public void onProviderDisabled(String provider) {}
	
	// ------------------------------------------------------------------------
	// Private Methods
	// ------------------------------------------------------------------------
	
	// ------------------------------------------------------------------------
	// Private Classes
	// ------------------------------------------------------------------------
	
	/**
	 * A TimerTask that does the job of executing the AsyncTask and 
	 * posting it to the Handler.
	 * 
	 * @author prschmid
	 *
	 */
	private class FetchListingTimerTask extends TimerTask { 
		private Runnable runnable = new Runnable() {
            public void run() {
            	forceRequery();
            }
        };

        public void run() {
        	Log.v(TAG, "Posting (Async) FetchListingTask");
        	mFetchListingsHandler.post(runnable);
        }
	}
	
	/**
     * The task that will perform the obtaining of data (FetchListingsAsyncTask)
     * with the proper implementation of onPostExecute() to make sure the
     * ListView is updated properly.
     * @author prschmid
     *
     */
    protected class FetchListingsTask<T extends AbstractListing> extends FetchListingsAsyncTask<T> {
		
		public FetchListingsTask(String queryUrl,
				Class<? extends T[]> listingClass, GeoPoint geoPoint,
				DataType type) {
			super(queryUrl, listingClass, geoPoint, mRadius, type);
		}

		protected void onPostExecute(T[] results) {
			Log.v(TAG, "onPostExecute()");
			if (results == null) return;
			if (results.length == 0) {
				mRadius = mRadius * 2;
			}
			mType2listings.put(type, Arrays.asList(results));
			sendBroadcast(mBroadcast);
		}
    }
	
}
