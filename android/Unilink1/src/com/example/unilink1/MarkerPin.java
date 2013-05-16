package com.example.unilink1;

public class MarkerPin extends BasePin {
	private long type = 0;
	private long icon = 0;
	
	public MarkerPin (BasePin pin, long type, long icon) {
		super(pin.getUid(), pin.getDate(), pin.getLat(), pin.getLon());
		this.type = type;
		this.icon = icon;
	}
}
