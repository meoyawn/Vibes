package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Profile implements Unit {

	public int uid;
	public String first_name;
	public String last_name;
	public String photo_medium_rec;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + uid;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Profile other = (Profile) obj;
		if (uid != other.uid)
			return false;
		return true;
	}

	@Override
	public int getId() {
		return uid;
	}

	@Override
	public String getName() {
		return first_name + " " + last_name;
	}

	@Override
	public String getProfilePic() {
		return photo_medium_rec;
	}
}
