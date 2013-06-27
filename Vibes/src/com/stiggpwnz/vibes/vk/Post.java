package com.stiggpwnz.vibes.vk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Post {

	private static final int MAX_LENGTH = 100;

	public int source_id;
	public long date;
	public int post_id;
	public String post_type;

	@JsonDeserialize(using = HtmlParser.class)
	public String text;
	public int signer_id;
	public List<Attachment> attachments;
	public Comments comments;
	public Likes likes;
	public Reposts reposts;
	public long copy_post_date;
	public int copy_owner_id;
	public int copy_post_id;
	public String copy_text;

	public List<Photo> photos;
	public List<Audio> audios;

	public String shortText;

	public boolean hasAudios() {
		boolean hasAudios = false;
		if (attachments != null) {
			shortText = text.substring(0, text.length() < MAX_LENGTH ? text.length() : MAX_LENGTH);

			List<Photo> photos = new ArrayList<Photo>();
			List<Audio> audios = new ArrayList<Audio>();

			for (Attachment attachment : attachments) {
				if (attachment.audio != null) {
					hasAudios = true;
					audios.add(attachment.audio);
				} else if (attachment.photo != null) {
					photos.add(attachment.photo);
				}
			}

			this.photos = photos;
			this.audios = audios;
		}
		return hasAudios;
	}

	public static class HtmlParser extends JsonDeserializer<String> {

		@Override
		public String deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException, JsonProcessingException {
			return Jsoup.parse(arg0.getValueAsString()).text();
		}

	}
}
