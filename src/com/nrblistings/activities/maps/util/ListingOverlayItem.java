package com.nrblistings.activities.maps.util;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;
import com.nrblistings.core.AbstractListing;

public class ListingOverlayItem<T extends AbstractListing> extends OverlayItem {

	private T mListing;
	
	public ListingOverlayItem(GeoPoint point, String title, String snippet) {
		super(point, title, snippet);
	}

	public ListingOverlayItem(GeoPoint point, String title, String snippet, T listing) {
		super(point, title, snippet);
		mListing = listing;
	}
	
	public T getListing() {
		return mListing;
	}
	public void setListing(T listing) {
		mListing = listing;
	}
}
