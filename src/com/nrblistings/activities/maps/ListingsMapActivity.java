package com.nrblistings.activities.maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.nrblistings.R;
import com.nrblistings.activities.lists.ListingsCountActivity;
import com.nrblistings.activities.maps.util.ListingItemizedOverlay;
import com.nrblistings.activities.maps.util.ListingOverlayItem;
import com.nrblistings.activities.posts.PostListingActivity;
import com.nrblistings.services.NrbListingsService;
import com.nrblistings.NrbListingsParameters;
import com.nrblistings.NrbListingsParameters.DataType;
import com.nrblistings.activities.details.EventDetailsActivity;
import com.nrblistings.activities.details.ListingDetailsActivity;
import com.nrblistings.core.AbstractListing;
import com.nrblistings.core.Event;
import com.nrblistings.core.Listing;
import com.nrblistings.util.LocationUtil;

public class ListingsMapActivity extends MapActivity {

	// ------------------------------------------------------------------------
	// Constants
	// ------------------------------------------------------------------------
	
	public static final String TAG = "ListingsMapActivity";
	
	/** The request code for when we ask for the location stuff to be turned on */
	private static final int LOCATION_SETTING_REQUEST = 1;
	
	// ------------------------------------------------------------------------
	// Member Variables
	// ------------------------------------------------------------------------
	
	/** The MapView we're working with */
	protected MapView mMapView;
	
	/** The center of the map */
	protected GeoPoint mCenter = NrbListingsParameters.DEFAULT_LOCATION;
	
	/** A progress dialog to wait for the first set up updates */
	private ProgressDialog progressDialog;
	
	/** The overlays for our map */
	protected Map<DataType, ListingItemizedOverlay<? extends AbstractListing>> mType2overlay;
	
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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
    
		// Show a progress dialog for the initial populating of the map
		progressDialog = ProgressDialog.show(this, "Connecting to Server", "Downloading listings...", true, false);
		checkLocationEnabled();
		
		// Bind to the listing service
		bindService(
				new Intent(this, NrbListingsService.class), 
				mServiceConnection, 
				Context.BIND_AUTO_CREATE);

	    mMapView = (MapView) findViewById(R.id.mapview);
	    mMapView.setBuiltInZoomControls(true);
	    
	    mType2overlay = new HashMap<DataType, ListingItemizedOverlay<? extends AbstractListing>>();
	    mType2overlay.put(
	    		DataType.Events, 
	    		new ListingItemizedOverlay<Event>(this, getResources().getDrawable(R.drawable.calendar)) {
	    			public boolean onTap(int index) {
	    				ListingOverlayItem<Event> item = mItems.get(index);
	    				Intent i = new Intent(mContext, EventDetailsActivity.class);
	    		        i.putExtra(DataType.Events.toString(), item.getListing());
	    		        startActivity(i);
	    		        return true;
	    			}
	    		});
	    mType2overlay.put(
	    		DataType.Listings, 
	    		new ListingItemizedOverlay<Listing>(this, getResources().getDrawable(R.drawable.box)) {
	    			public boolean onTap(int index) {
	    				ListingOverlayItem<Listing> item = mItems.get(index);
	    				Intent i = new Intent(mContext, ListingDetailsActivity.class);
	    		        i.putExtra(DataType.Listings.toString(), item.getListing());
	    		        startActivity(i);
	    		        return true;
	    			}
	    		});

		// Get the last known location to start things off
		mCenter = LocationUtil.getGeoPoint((LocationManager) getSystemService(Context.LOCATION_SERVICE));
		
		// Center the map on the data points and force it to redraw
    	MapController mc = mMapView.getController();
    	mc.animateTo(mCenter);
    	mc.setZoom(14); 
        mMapView.invalidate();
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
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, NrbListingsParameters.LIST_VIEW, 0, R.string.menu_list_view);
        menu.add(0, NrbListingsParameters.POST_LISTING, 0, R.string.menu_create_post);
        return true;
    }
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case NrbListingsParameters.LIST_VIEW:
            	startActivity(new Intent(this, ListingsCountActivity.class));
                return true;
            case NrbListingsParameters.POST_LISTING:
            	startActivity(new Intent(this, PostListingActivity.class));
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
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
	// MapActivity Methods
	// ------------------------------------------------------------------------
    
    /*
	 * (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#isRouteDisplayed()
	 */
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    
    // ------------------------------------------------------------------------
	// Callback Methods
	// ------------------------------------------------------------------------

    /*
	 * (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == LOCATION_SETTING_REQUEST && resultCode == 0){
        	if (checkLocationEnabled()) {
        		mService.forceRequery();
        	}
        }
    }
	
    // ------------------------------------------------------------------------
	// Public Methods
	// ------------------------------------------------------------------------
    
    /**
	 * This method does the work of filling the ListView with the data in
	 * mListings.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void updateListings() {
		if (mService != null) {
			int count = 0;
    		for (DataType type : DataType.values()) {
				ListingItemizedOverlay<? extends AbstractListing> overlay = mType2overlay.get(type);
    			List<? extends AbstractListing> listings = mService.getListings(type);
    			if (listings != null) {
	    			for (AbstractListing al : listings) {
		    			int lat = (int)(al.getLatitude() * 1E6);
		    			int lng = (int)(al.getLongitude() * 1E6);
		    			overlay.addOverlay(
		    					new ListingOverlayItem(
		    							new GeoPoint(lat, lng), 
		    							al.getName(), 
		    							"", 
		    							al));
		    			++count;
	    			}
    			}
    		}
    		
    		if (count > 0 && progressDialog.isShowing()) {
    			progressDialog.dismiss();
    		}
    		redrawMap();
    	}
	}
	
	// ------------------------------------------------------------------------
	// Protected/Private Methods
	// ------------------------------------------------------------------------
	
	private void redrawMap() {
    	
		Log.v(TAG, "Redrawing the map.");
		
    	// Get the overlay and clear it
    	List<Overlay> mapOverlays = mMapView.getOverlays();
    	mapOverlays.clear();
    	
    	List<GeoPoint> points = new ArrayList<GeoPoint>();
    	
    	// Loop over all data types and add the overlays
    	int[] minmaxLat = new int[] {Integer.MAX_VALUE, Integer.MIN_VALUE};
    	int[] minmaxLon = new int[] {Integer.MAX_VALUE, Integer.MIN_VALUE};
    	for (ListingItemizedOverlay<? extends AbstractListing> overlay : mType2overlay.values()) {
    		mapOverlays.add(overlay);
    		GeoPoint gp = overlay.getCenter();
    		if (gp != null) {
    			points.add(gp);
    		}
    		int[] minmax = overlay.getMinMaxLatE6();
    		if (minmax[0] < minmaxLat[0]) { minmaxLat[0] = minmax[0]; }
    		if (minmax[1] > minmaxLat[1]) { minmaxLat[1] = minmax[1]; }
    		minmax = overlay.getMinMaxLonE6();
    		if (minmax[0] < minmaxLon[0]) { minmaxLon[0] = minmax[0]; }
    		if (minmax[1] > minmaxLon[1]) { minmaxLon[1] = minmax[1]; }
    	}
	    
    	if (!points.isEmpty()) {
    		mCenter = LocationUtil.getCenter(points);
    	}
    	
    	// Center the map on the data points and force it to redraw
//    	MapController mc = mMapView.getController();
//    	mc.animateTo(mCenter);
//    	mc.zoomToSpan((minmaxLat[1] - minmaxLat[0]), (minmaxLon[1] - minmaxLon[0]));
        mMapView.invalidate();
    }
    
	/**
	 * Check to see if the location service has been enabled
	 */
	private boolean checkLocationEnabled() {
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Toast.makeText(this, "GPS is Enabled in your devide", Toast.LENGTH_SHORT).show();
			return true;
		} else {
			showAlertMessageNoGps();
			return false;
		}
	}
	
	/**
	 * Show a dialog asking the user if they want to go to Settings to change
	 * the location settings.
	 */
	private void showAlertMessageNoGps() {
	    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage("Yout GPS seems to be disabled, do you want to enable it?")
	           .setCancelable(false)
	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, final int id) {
	            	   Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	            	   startActivityForResult(intent, LOCATION_SETTING_REQUEST);
	               }
	           })
	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, final int id) {
	                    dialog.cancel();
	               }
	           });
	    final AlertDialog alert = builder.create();
	    alert.show();
	}
    
}
