package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.stiggpwnz.vibes.util.HtmlDeserializer;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Post {

    public int count;

    public int  source_id;
    public long date;

    @JsonDeserialize(using = HtmlDeserializer.class)
    public String text;

    public Attachment[] attachments;

    // computed stuff
    public List<Photo> photos;
    public List<Audio> audios;
    public Unit        unit;

    @SuppressWarnings("unused")
    public Post() {

    }

    @SuppressWarnings("unused")
    public Post(int count) {
        this.count = count;
    }

    @JsonProperty("from_id")
    public void setFromId(int fromId) {
        source_id = fromId;
    }

    public boolean calculateAudiosAndPhotos() {
        boolean hasAudios = false;
        if (attachments != null) {
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

}
