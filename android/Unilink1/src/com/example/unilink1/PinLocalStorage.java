package com.example.unilink1;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

public class PinLocalStorage {
	private List<MarkerPin> markers;
	private List<NewsPin> news;
	
	public PinLocalStorage() {
		this.markers = new ArrayList<MarkerPin>();
		this.news = new ArrayList<NewsPin>();
	}
	
	public void removeOld(List<BasePin> pins) {
		for (int j = 0; j < this.markers.size(); j++) {
			BasePin pin = this.markers.get(j);
			for (int i = pins.size()-1; i >= 0; i--) {
				if (pins.get(i).equalsTo(pin) && pins.get(i).getDate() > pin.getDate())
					pins.remove(i);
			}
		}
		for (int j = 0; j < this.news.size(); j++) {
			BasePin pin = this.news.get(j);
			for (int i = pins.size()-1; i >= 0; i--) {
				if (pins.get(i).equalsTo(pin) && pins.get(i).getDate() > pin.getDate())
					pins.remove(i);
			}
		}
	}
	
	public void loadFromLocalStorage() {
		
	}
	
	public void updateLocation(LatLng location) {
		
	}
}
