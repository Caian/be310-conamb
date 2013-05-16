package com.example.unilink1;

public class NewsPin extends BasePin {
	private String name = "";
	private String text = "";
	private int votes = 0;
	
	public NewsPin (BasePin pin, String name, String text, int votes) {
		super(pin.getUid(), pin.getDate(), pin.getLat(), pin.getLon());
		this.name = name;
		this.text = text;
		this.votes = votes;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getText() {
		return this.text;
	}
	
	public int getVotes() {
		return this.votes;
	}
}
