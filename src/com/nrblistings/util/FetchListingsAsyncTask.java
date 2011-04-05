package com.nrblistings.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.client.CommonsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.nrblistings.NrbListingsParameters.DataType;
import com.nrblistings.core.AbstractListing;

/**
 * An abstract AsyncTask that does the job of querying the web with the given
 * parameters.
 * 
 * It is the responsibility of the subclass to implement the onPostExecute(T[] result)
 * method to do something with the results.
 * 
 * @author prschmid
 *
 * @param <T>
 */
public abstract class FetchListingsAsyncTask<T extends AbstractListing> extends AsyncTask<Void, Void, T[]> {

	/** The tag for this class */
	public static final String TAG = "FetchListingsAsyncTask";
	
	protected String queryUrl;
	protected Class<? extends T[]> listingClass;
	protected GeoPoint geoPoint;
	protected DataType type;
	protected int radius;
	protected int startOffset = 0;
	protected int endOffset = 50;
	protected List<String> tags;
	
	public String getQueryUrl() {
		return queryUrl;
	}
	public void setQueryUrl(String queryUrl) {
		this.queryUrl = queryUrl;
	}
	public Class<? extends T[]> getListingClass() {
		return listingClass;
	}
	public void setListingClass(Class<? extends T[]> resultClass) {
		this.listingClass = resultClass;
	}
	public GeoPoint getGeoPoint() {
		return geoPoint;
	}
	public void setGeoPoint(GeoPoint geoPoint) {
		this.geoPoint = geoPoint;
	}
	public DataType getDataType() {
		return type;
	}
	public void setDataType(DataType type) {
		this.type = type;
	}
	public int getRadius() {
		return radius;
	}
	public void setRadius(int radius) {
		this.radius = radius;
	}
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	public int getStartOffset() {
		return startOffset;
	}
	public void setStartOffset(int startOffset) {
		this.startOffset = startOffset;
	}
	public int getEndOffset() {
		return endOffset;
	}
	public void setEndOffset(int endOffset) {
		this.endOffset = endOffset;
	}
	
	public FetchListingsAsyncTask(String queryUrl, Class<? extends T[]> listingClass, GeoPoint geoPoint, int radius, DataType type) {
		this.queryUrl = queryUrl;
		this.listingClass = listingClass;
		this.geoPoint = geoPoint;
		this.radius = radius;
		this.type = type;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override 
	protected T[] doInBackground(Void... v) {
				
		//
		// Setup the query
		//
		
		// Setup the RestTemplate
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(new CommonsClientHttpRequestFactory());
		MappingJacksonHttpMessageConverter jsonConverter = new MappingJacksonHttpMessageConverter();
		List<HttpMessageConverter<?>> listHttpMessageConverters = restTemplate.getMessageConverters();
		listHttpMessageConverters.add(jsonConverter);			
		restTemplate.setMessageConverters(listHttpMessageConverters);
		
		// Provide the variables for lat, long, and radius
		Map<String, String> variables = new HashMap<String, String>();
		variables.put("latitude", Double.toString(geoPoint.getLatitudeE6()/1E6));
		variables.put("longitude", Double.toString(geoPoint.getLongitudeE6()/1E6));
		variables.put("radius", Integer.toString(radius));
		variables.put("startOffset", Integer.toString(startOffset));
		variables.put("endOffset", Integer.toString(endOffset));
		
		if (tags != null && !tags.isEmpty()) {
			StringBuffer sb = new StringBuffer();
			for (String tag : tags) {
				sb.append(tag);
				sb.append(";");
			}
			sb.deleteCharAt(sb.length()-1);
			variables.put("tags", tags.toString());
		}
		
		//
		// Perform the query
		//
		
		// Do the query
		T[] results = null;
		try {
			results = restTemplate.getForObject(queryUrl, listingClass, variables);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	    
	    return results;
	}
	
	@Override
	public String toString() {
		return "FetchListingsAsyncTask [queryUrl=" + queryUrl + ", listingClass="
				+ listingClass + ", geoPoint=" + geoPoint + ", radius="
				+ radius + ", tags=" + tags + ", startOffset=" + startOffset
				+ ", endOffset=" + endOffset + "]";
	}
}
