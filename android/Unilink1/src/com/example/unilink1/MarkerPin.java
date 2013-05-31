package com.example.unilink1;

import com.google.android.gms.maps.model.BitmapDescriptor;

public class MarkerPin extends BasePin {
	private long type = 0;
	private long icon = 0;
	
	public MarkerPin (long uid, long date, double lat, double lon, long type, long icon) {
		super(uid, date, lat, lon);
		this.type = type;
		this.icon = icon;
	}
	
	public long getType() {
		return this.type;
	}
	
	public long getIcon() {
		return this.icon;
	}
	
	@Override
	public BitmapDescriptor getResourceIcon() {
		return PinLocalStorage.getStorage().
				getResourceIcon(this.icon);
	}
}
