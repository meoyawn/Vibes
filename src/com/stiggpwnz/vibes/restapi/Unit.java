package com.stiggpwnz.vibes.restapi;

import java.io.Serializable;
import java.util.ArrayList;

public class Unit implements Serializable {

	private static final long serialVersionUID = -2084663183200225959L;

	public int id;
	public String name;
	public String photo;
	public ArrayList<Album> albums;

	public Unit(int id, String name, String photo) {
		this.id = id;
		this.name = name;
		this.photo = photo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((albums == null) ? 0 : albums.hashCode());
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((photo == null) ? 0 : photo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Unit))
			return false;
		Unit other = (Unit) obj;
		if (albums == null) {
			if (other.albums != null)
				return false;
		} else if (!albums.equals(other.albums))
			return false;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (photo == null) {
			if (other.photo != null)
				return false;
		} else if (!photo.equals(other.photo))
			return false;
		return true;
	}

}
