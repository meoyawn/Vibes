package com.stiggpwnz.vibes;

import java.util.List;

public class Unit {

	public int id;
	public String name;
	public String photo;
	public List<Album> albums;

	public Unit(int id, String name, String photo) {
		this.id = id;
		this.name = name;
		this.photo = photo;
	}
}
