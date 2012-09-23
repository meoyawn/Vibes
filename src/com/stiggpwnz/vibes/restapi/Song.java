package com.stiggpwnz.vibes.restapi;

public class Song {

	public int aid;
	public int ownerid;
	public int myAid;

	public String performer;
	public String title;
	public String url;
	public String albumImageUrl;

	public boolean loved;

	public Song(int id, int owner, String artist, String name) {
		aid = id;
		performer = artist;
		ownerid = owner;
		title = name;
	}

	public Song(int id, int owner, String artist, String name, String url) {
		aid = id;
		performer = artist;
		ownerid = owner;
		title = name;
		this.url = url;
	}

	@Override
	public String toString() {
		return String.format("%s - %s", performer, title);
	}

}