package com.example.unilink1;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;

import android.view.LayoutInflater;
import android.view.View;

public class BasePin {
	
	public final static long CATEGORY_NEWS = 0;

	private long uid = 0;
	private long date = 0;
	private double lat = 0.0;
	private double lon = 0.0;
	private Marker marker = null;

	public Marker getMarker() {
		return marker;
	}

	public void setMarker(Marker marker) {
		this.marker = marker;
	}

	public BasePin(long uid, long date, double lat, double lon) {
		this.uid = uid;
		this.date = date;
		this.lat = lat;
		this.lon = lon;
	}

	public Boolean equalsTo(BasePin other) {
		return this.uid == other.uid;
	}
	
	public long getUid() {
		return this.uid;
	}
	
	public long getDate() {
		return this.date;
	}

	public double getLat() {
		return this.lat;
	}

	public double getLon() {
		return this.lon;
	}
	
	public long getType() {
		return -1;
	}
	
	public View getView(LayoutInflater inflater) {
		return null;
	}

	public BitmapDescriptor getResourceIcon() {
		return BitmapDescriptorFactory.fromResource(
				R.drawable.ic_unknown);
	}
}