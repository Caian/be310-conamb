package com.example.unilink1;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import android.view.LayoutInflater;
import android.view.View;

public class NewsPin extends BasePin {
	private String name = "";
	private String text = "";
	private long upvotes = 0;
	private long downvotes = 0;
	
	public NewsPin (long uid, long date, double lat, double lon, 
			String name, String text, long upvotes, long dnvotes) {
		super(uid, date, lat, lon);
		this.name = name;
		this.text = text;
		this.upvotes = upvotes;
		this.downvotes = dnvotes;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getText() {
		return this.text;
	}
	
	public long getUpVotes() {
		return this.upvotes;
	}
	
	public long getDownVotes() {
		return this.downvotes;
	}
	
	@Override
	public View getView(LayoutInflater inflater) {
		return inflater.inflate(R.layout.news_marker, null);
	}
	
	@Override
	public BitmapDescriptor getResourceIcon() {
		return BitmapDescriptorFactory.fromResource(
				R.drawable.ic_comment);
	}
}
