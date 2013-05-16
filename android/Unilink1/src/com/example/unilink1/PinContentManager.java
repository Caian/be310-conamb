package com.example.unilink1;

import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

public class PinContentManager implements InfoWindowAdapter {

	private LayoutInflater inflater;
	
	@Override
	public View getInfoContents(Marker arg0) {
		return inflater.inflate(R.layout.news_marker, null);
		//return null;
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public PinContentManager(LayoutInflater inflater) {
		this.inflater = inflater;
	}
}
