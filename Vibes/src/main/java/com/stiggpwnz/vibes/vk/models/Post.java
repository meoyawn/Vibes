package com.stiggpwnz.vibes.vk.models;

import android.text.Html;
import android.text.Spanned;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Post {

    public int  source_id;
    public long date;

    @JsonDeserialize(using = HtmlParser.class)
    public Spanned text;

    public Attachment[] attachments;

    public List<Photo> photos;
    public List<Audio> audios;

    public Unit unit;

    public Post() {

    }

    public Post(int count) {

    }

    @JsonProperty("from_id")
    public void setFromId(int fromId) {
        source_id = fromId;
    }

    public void setUnit(Set<Profile> profiles, Set<Group> groups) {
        if (source_id >= 0) {
            for (Profile profile : profiles) {
                if (profile.uid == source_id) {
                    unit = profile;
                    return;
                }
            }
        } else {
            for (Group group : groups) {
                if (-group.gid == source_id) {
                    unit = group;
                    return;
                }
            }
        }
    }

    public boolean hasAudios() {
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

    public static class HtmlParser extends JsonDeserializer<Spanned> {

        @Override
        public Spanned deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException {
            return Html.fromHtml(arg0.getValueAsString());
        }
    }
}
