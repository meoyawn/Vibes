package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.stiggpwnz.vibes.vk.models.Post.HtmlParser;

public class Audio {

	public int aid;
	public int owner_id;

	@JsonDeserialize(using = HtmlParser.class)
	public String performer;

	@JsonDeserialize(using = HtmlParser.class)
	public String title;

	public int duration;
}
