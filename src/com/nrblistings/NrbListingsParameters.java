package com.nrblistings;

import java.util.HashMap;
import java.util.Map;

import com.google.android.maps.GeoPoint;
import com.nrblistings.core.AbstractListing;
import com.nrblistings.core.Event;
import com.nrblistings.core.Listing;

public class NrbListingsParameters {

	public static final int MAP_VIEW = 0;
	public static final int LIST_VIEW = 1;
	public static final int POST_LISTING = 2;
	
	/** The types of data that we support */
	public static enum DataType { Events, Listings };

	/** The URLs to query for each DataType */
	public static final Map<DataType, String> TYPE_2_URL;
	static {
		TYPE_2_URL = new HashMap<DataType, String>();
		TYPE_2_URL.put(DataType.Listings, "http://128.31.4.188:8080/nrbListings-0.1/listings?latitude={latitude}&longitude={longitude}&radius={radius}&startOffset={startOffset}&endOffset={endOffset}");
		TYPE_2_URL.put(DataType.Events, "http://128.31.4.188:8080/nrbListings-0.1/events?latitude={latitude}&longitude={longitude}&radius={radius}&startOffset={startOffset}&endOffset={endOffset}");
//		TYPE_2_URL.put(DataType.Listings, "http://10.0.2.2/~prschmid/LocationMatrix/listings.json?latitude={latitude}&longitude={longitude}&radius={radius}&startOffset={startOffset}&endOffset={endOffset}");
//		TYPE_2_URL.put(DataType.Events, "http://10.0.2.2/~prschmid/LocationMatrix/events.json?latitude={latitude}&longitude={longitude}&radius={radius}&startOffset={startOffset}&endOffset={endOffset}");
	}
	
	//public static final String POST_LISTING_URL = "http://10.0.2.2/~prschmid/";
	public static final String POST_LISTING_URL = "http://128.31.4.188:8080/nrbListings-0.1/productListing?name={name}&price={price}&description={description}&latitude={latitude}&longitude={longitude}";
	
	/** The data types of the results of the query */
	public static final Map<DataType, Class<? extends AbstractListing[]>> TYPE_2_CLASS;
	static {
		TYPE_2_CLASS = new HashMap<DataType, Class<? extends AbstractListing[]>>();
		TYPE_2_CLASS.put(DataType.Listings, Listing[].class);
		TYPE_2_CLASS.put(DataType.Events, Event[].class);
	}
	
	public static GeoPoint DEFAULT_LOCATION = new GeoPoint((int)(42.375*1E6), (int)(-71.1061111*1E6));
	
	public static String DEFAULT_ADDRESS = "Cambridge, MA, USA";
	
	public static int DEFAULT_RADIUS = 10000;
}
