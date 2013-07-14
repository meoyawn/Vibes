package com.stiggpwnz.vibes.vk.models;

import java.util.List;
import java.util.Set;

public class NewsFeed {

	public List<Post> items;
	public Set<Profile> profiles;
	public Set<Group> groups;

	public int new_offset;
	public String new_from;

	public static class Result extends com.stiggpwnz.vibes.vk.models.Result {
		public NewsFeed response;
	}
}
