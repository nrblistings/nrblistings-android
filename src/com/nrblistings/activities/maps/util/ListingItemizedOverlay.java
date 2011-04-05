package com.nrblistings.activities.maps.util;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.nrblistings.core.AbstractListing;

public class ListingItemizedOverlay<T extends AbstractListing> extends ItemizedOverlay<ListingOverlayItem<T>> {

	protected Context mContext;
	protected ArrayList<ListingOverlayItem<T>> mItems = new ArrayList<ListingOverlayItem<T>>();

	public ListingItemizedOverlay(Context context, Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;
		populate();
	}
	
	public void addOverlay(ListingOverlayItem<T> overlay) {
	    mItems.add(overlay);
	    populate();
	}
	
	@Override
	protected ListingOverlayItem<T> createItem(int i) {
		return mItems.get(i);
	}

	@Override
	public int size() {
		return mItems.size();
	}
	
	@Override
	public GeoPoint getCenter() {
		if (!mItems.isEmpty()) {
			long lat = 0;
			long lng = 0;
			for (ListingOverlayItem<T> item : mItems) {
				lat += item.getPoint().getLatitudeE6();
				lng += item.getPoint().getLongitudeE6();
			}
			return new GeoPoint((int)(lat / mItems.size()), (int)(lng / mItems.size()));
		} else {
			return null;
		}
	}

	public int[] getMinMaxLatE6() {
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (ListingOverlayItem<T> item : mItems) {
			if (item.getPoint().getLatitudeE6() < min) min = item.getPoint().getLatitudeE6();
			if (item.getPoint().getLatitudeE6() > max) max = item.getPoint().getLatitudeE6();
		}
		return new int[] {min, max};
	}
	
	public int[] getMinMaxLonE6() {
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (ListingOverlayItem<T> item : mItems) {
			if (item.getPoint().getLongitudeE6() < min) min = item.getPoint().getLongitudeE6();
			if (item.getPoint().getLongitudeE6() > max) max = item.getPoint().getLongitudeE6();
		}
		return new int[] {min, max};
	}
	
	@Override
	public int getLatSpanE6() {
		int[] minmax = getMinMaxLatE6();
		return minmax[1] - minmax[0];
	}
	
	@Override
	public int getLonSpanE6() {
		int[] minmax = getMinMaxLonE6();
		return minmax[1] - minmax[0];
	}
	
	public void clear() {
		mItems.clear();
	}
	
}
