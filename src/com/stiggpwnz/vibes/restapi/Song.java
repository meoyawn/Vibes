package com.stiggpwnz.vibes.restapi;

public class Song {

	public int aid;
	public int ownerid;
	public int myAid;

	public String performer;
	public String title;
	public String url;

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + aid;
		result = prime * result + (loved ? 1231 : 1237);
		result = prime * result + myAid;
		result = prime * result + ownerid;
		result = prime * result + ((performer == null) ? 0 : performer.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Song))
			return false;
		Song other = (Song) obj;
		if (aid != other.aid)
			return false;
		if (loved != other.loved)
			return false;
		if (myAid != other.myAid)
			return false;
		if (ownerid != other.ownerid)
			return false;
		if (performer == null) {
			if (other.performer != null)
				return false;
		} else if (!performer.equals(other.performer))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

}