package com.nrblistings.core;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class AbstractListing implements Parcelable {
	
	protected String id;
	protected String name;
	protected String description;
	protected String price;
	protected String url;
	protected String imgUrl;
	protected Date dateCreated;
	protected double latitude;
	protected double longitude;
	protected double distance;
	
	public AbstractListing() {
	}
	
	public AbstractListing(Parcel in) {
		id = in.readString();
		name = in.readString();
		description = in.readString();
		price = in.readString();
		url = in.readString();
		imgUrl = in.readString();
		dateCreated = (Date)in.readSerializable();
		longitude = in.readDouble();
		latitude = in.readDouble();
		distance = in.readDouble();
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getImgUrl() {
		return imgUrl;
	}
	public Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	public void setImgUrl(String thumbnailUrl) {
		this.imgUrl = thumbnailUrl;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	@Override
	public int describeContents() {
		return hashCode();
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(name);
		dest.writeString(description);
		dest.writeString(price);
		dest.writeString(url);
		dest.writeString(imgUrl);
		dest.writeSerializable(dateCreated);
		dest.writeDouble(longitude);
		dest.writeDouble(latitude);
		dest.writeDouble(distance);
	}
	@Override
	public String toString() {
		return "AbstractListing [id=" + id + ", name=" + name
				+ ", description=" + description + ", price=" + price
				+ ", url=" + url + ", imgUrl=" + imgUrl + ", dateCreated="
				+ dateCreated + ", latitude=" + latitude + ", longitude="
				+ longitude + ", distance=" + distance + "]";
	}
}
