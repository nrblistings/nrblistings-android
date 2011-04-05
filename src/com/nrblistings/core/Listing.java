package com.nrblistings.core;

import android.os.Parcel;
import android.os.Parcelable;

public class Listing extends AbstractListing {
	
	public Listing() {
	}
	
	public Listing(Parcel in) {
		super(in);
	}
	
	public static final Parcelable.Creator<Listing> CREATOR = new Parcelable.Creator<Listing>() {
        public Listing createFromParcel(Parcel in) {
            return new Listing(in);
        }
 
        public Listing[] newArray(int size) {
            return new Listing[size];
        }
    };
}
