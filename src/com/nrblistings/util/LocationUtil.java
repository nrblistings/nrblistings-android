package com.nrblistings.util;

import java.util.List;

import android.location.Location;
import android.location.LocationManager;

import com.google.android.maps.GeoPoint;
import com.nrblistings.NrbListingsParameters;

/**
 * Some helper methods for location based stuff.
 * 
 * Much of the code was taken (and possibly modified) from:
 * 	http://developer.android.com/guide/topics/location/obtaining-user-location.html
 * 
 * @author Patrick R. Schmid (prschmid@gmail.com)
 *
 */
public class LocationUtil {

	// ------------------------------------------------------------------------
	// Member Variables
	// ------------------------------------------------------------------------
	
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	
	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------
	
	/**
	 * Don't allow instantiations of this class.
	 */
	private LocationUtil() {}
		
	// ------------------------------------------------------------------------
	// Public Methods
	// ------------------------------------------------------------------------
	
	/** 
	 * Determines whether one Location reading is better than the current Location fix
	 * @param location  The new Location that you want to evaluate
	 * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	 */
	public static final boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
		// If the new location is more than two minutes older, it must be worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	public static final Location getLocation(LocationManager locationManager) {
		
		// Get the last known location to start things off
		// FIXME: delete this line!
//		Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		// FIXME: uncomment this line!
		Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//		if (location == null) {
//			location = this.locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//		}
		
		// Center the map on the data points
//    		Geocoder geoCoder = new Geocoder(this, Locale.getDefault());    
//	        try {
//	            List<Address> addresses = geoCoder.getFrolocationName(mDefaultLocation, 5);
//	            if (addresses.size() > 0) {
//	            	Log.v(TAG, "Setting location to address: " + addresses.get(0));
//	                mc.animateTo(new GeoPoint(
//	                        (int) (addresses.get(0).getLatitude() * 1E6), 
//	                        (int) (addresses.get(0).getLongitude() * 1E6)));
//	            }    
//	        } catch (IOException e) {
//	            e.printStackTrace();
//	        }
		
		return location;
	}
	public static final GeoPoint getGeoPoint(LocationManager locationManager) {
		Location l = getLocation(locationManager);
		if (l != null) {
			return locationToGeoPoint(l);
		} else {
			return NrbListingsParameters.DEFAULT_LOCATION;
		}
	}
	
	public static final GeoPoint locationToGeoPoint(Location location) {
		return new GeoPoint(
				(int)(location.getLatitude() * 1E6), 
				(int)(location.getLongitude() * 1E6));
	}
	
	public static final GeoPoint getCenter(List<GeoPoint> geoPoints) {
		long lat = 0;
		long lng = 0;
		for (GeoPoint gp : geoPoints) {
			lat += gp.getLatitudeE6();
			lng += gp.getLongitudeE6();
		}
		return new GeoPoint((int)(lat / geoPoints.size()), (int)(lng / geoPoints.size()));
	}
		
	// ------------------------------------------------------------------------
	// Protected/Private Methods
	// ------------------------------------------------------------------------

	/** 
	 * Checks whether two providers are the same 
	 */
	private static final boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
		  return provider2 == null;
		}
		return provider1.equals(provider2);
	}
}
