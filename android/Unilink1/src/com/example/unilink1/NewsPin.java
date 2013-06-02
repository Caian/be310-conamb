package com.example.unilink1;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class NewsPin extends BasePin {
	
	private String name = "";
	private String text = "";
	private long upvotes = 0;
	private long downvotes = 0;
	private Boolean liked = false;
	private Boolean disliked = false;
	
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
	
	public Boolean getLiked() {
		return this.liked;
	}
	
	public Boolean getDisliked() {
		return this.disliked;
	}
	
	@Override
	public long getType() {
		return BasePin.CATEGORY_NEWS;
	}

	public void setUpVotes(long upvotes) {
		this.upvotes = upvotes;
	}

	public void setDownVotes(long downvotes) {
		this.downvotes = downvotes;
	}
	
	@Override
	public View getView(LayoutInflater inflater) {
		View v = inflater.inflate(R.layout.news_marker, null);
		TextView t = (TextView) v.findViewById(R.id.textTitle);
		t.setText(this.name);
		t = (TextView) v.findViewById(R.id.textUpvotes);
		t.setText(((Long)this.upvotes).toString());
		t = (TextView) v.findViewById(R.id.textDnvotes);
		t.setText(((Long)this.downvotes).toString());
		return v;
	}
	
	@Override
	public BitmapDescriptor getResourceIcon() {
		if (this.upvotes == this.downvotes) {
			return BitmapDescriptorFactory.fromResource(
					R.drawable.ic_comment);
		}
		else if (this.upvotes > this.downvotes) {
			return BitmapDescriptorFactory.fromResource(
					R.drawable.ic_commentg);
		}
		else {
			return BitmapDescriptorFactory.fromResource(
					R.drawable.ic_commentr);
		}
	}

	public Boolean like() {
		if (this.liked)
			return false;
		UnilinkDB db = UnilinkDB.getDatabase();
		if (!db.isValidated())
			return false;
		db.like(this.getUid());
		this.liked = true;
		this.upvotes++;
		if (this.disliked) {
			this.downvotes--;
			this.disliked = false;
		}
		return true;
	}

	public Boolean dislike() {
		if (this.disliked)
			return false;
		UnilinkDB db = UnilinkDB.getDatabase();
		if (!db.isValidated())
			return false;
		db.dislike(this.getUid());
		this.disliked = true;
		this.downvotes++;
		if (this.liked) {
			this.upvotes--;
			this.liked = false;
		}
		return false;
	}
}
