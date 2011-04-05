package com.nrblistings.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

public abstract class AbstractFetchThumbnailImage extends AsyncTask<String, Void, Drawable> {

	public static final String TAG = "AbstractFetchThumbnailImage";
	
	@Override
	protected Drawable doInBackground(String... urls) {
		
		if (urls == null) return null;
		if (urls.length == 0) return null;
		
		Drawable d = null;
		InputStream is = null;
		try {
			is = (InputStream) new URL(urls[0]).getContent();
			d = Drawable.createFromStream(is, "src");
		} catch (MalformedURLException e) {
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		} finally {
			try {is.close();} catch (Exception e) {}
		}
		return d;
	}
}