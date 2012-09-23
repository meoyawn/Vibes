package com.stiggpwnz.vibes.restapi;

public class Playlist {

	public static enum Type {
		AUDIOS, WALL, NEWSFEED, SEARCH
	}

	public Type type;
	public Unit unit;
	public Album album;
	public String search;
	public String name;

	public Playlist(Type type, String name) {
		this.type = type;
		this.name = name;
	}

	public Playlist(Type type, String name, Unit unit) {
		this(type, name);
		this.unit = unit;
	}

	public Playlist(Type type, String name, Unit unit, Album album) {
		this(type, name, unit);
		this.album = album;
	}

	public Playlist(Type type, String name, String search) {
		this(type, name);
		this.search = search;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((album == null) ? 0 : album.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((search == null) ? 0 : search.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Playlist))
			return false;
		Playlist other = (Playlist) obj;
		if (album == null) {
			if (other.album != null)
				return false;
		} else if (!album.equals(other.album))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (search == null) {
			if (other.search != null)
				return false;
		} else if (!search.equals(other.search))
			return false;
		if (type != other.type)
			return false;
		if (unit == null) {
			if (other.unit != null)
				return false;
		} else if (!unit.equals(other.unit))
			return false;
		return true;
	}

}
