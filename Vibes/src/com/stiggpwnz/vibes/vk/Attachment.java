package com.stiggpwnz.vibes.vk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Attachment {

	public Photo photo;
	public Audio audio;
	public Link link;
}
