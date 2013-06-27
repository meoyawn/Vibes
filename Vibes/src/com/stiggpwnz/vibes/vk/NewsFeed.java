package com.stiggpwnz.vibes.vk;

import java.util.List;
import java.util.Set;

public class NewsFeed {

	public List<Post> items;
	public Set<Profile> profiles;
	public Set<Group> groups;

	public int new_offset;
	public String new_from;

	public static class Result {

		public NewsFeed response;
		public Error error;
	}
}
