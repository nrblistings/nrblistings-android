package com.nrblistings.core;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class Event extends AbstractListing {
	private Date startDate;
	private Date endDate;
	
	public Event() {}
	public Event(Parcel in) {
		super(in);
		startDate = (Date)in.readSerializable();
		endDate = (Date)in.readSerializable();
	}
	
	public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }
 
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
    
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	@Override
	public int describeContents() {
		return hashCode();
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeSerializable(startDate);
		dest.writeSerializable(endDate);
	}
	@Override
	public String toString() {
		return "Event [startDate=" + startDate + ", endDate=" + endDate
				+ ", id=" + id + ", name=" + name + ", description="
				+ description + ", price=" + price + ", url=" + url
				+ ", latitude=" + latitude + ", longitude=" + longitude + "]";
	}
}
