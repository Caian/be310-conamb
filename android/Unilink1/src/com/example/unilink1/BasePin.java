package com.example.unilink1;

public class BasePin {

	private long uid = 0;
	private long date = 0;
	private double lat = 0.0;
	private double lon = 0.0;

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

}