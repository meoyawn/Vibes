package com.stiggpwnz.vibes.vk.models;

import android.text.TextUtils;
import android.text.format.DateUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.stiggpwnz.vibes.util.HtmlDeserializer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Post implements Serializable {

    public                                                  int          count;
    public                                                  int          source_id;
    public                                                  long         date;
    @JsonDeserialize(using = HtmlDeserializer.class) public String       text;
    public                                                  Attachment[] attachments;

    // computed stuff
    public Photo[] photos;
    public Audio[] audios;
    public Unit    unit;

    public Post() {

    }

    public Post(int count) {
        this.count = count;
    }

    @JsonProperty("from_id")
    public void setFromId(int fromId) {
        source_id = fromId;
    }

    public boolean hasAudios() {
        if (attachments != null) {
            for (Attachment attachment : attachments) {
                if (attachment.getType() == Attachment.Type.AUDIO) {
                    return true;
                }
            }
        }
        return false;
    }

    public void calculateSelfAudios() {
        if (attachments != null) {
            List<Audio> audioList = new ArrayList<>(attachments.length);
            for (Attachment attachment : attachments) {
                if (attachment.getType() == Attachment.Type.AUDIO) {
                    audioList.add(attachment.getAudio());
                }
            }
            audios = audioList.toArray(new Audio[audioList.size()]);
        }
    }

    public void calculateSelfPhotos() {
        if (attachments != null) {
            List<Photo> photoList = new ArrayList<>(attachments.length);
            for (Attachment attachment : attachments) {
                if (attachment.getType() == Attachment.Type.PHOTO) {
                    photoList.add(attachment.getPhoto());
                }
            }
            photos = photoList.toArray(new Photo[photoList.size()]);
        }
    }

    public CharSequence relativeTimeString() {
        return DateUtils.getRelativeTimeSpanString(date * 1000);
    }

    public boolean hasText() {
        return !TextUtils.isEmpty(text);
    }

    public boolean hasPhotos() {
        return photos != null && photos.length > 0;
    }

    void setProfileFrom(Feed feed) {
        for (Profile profile : feed.profiles) {
            if (profile.getId() == source_id) {
                unit = profile;
                break;
            }
        }
    }

    void setGroupFrom(Feed feed) {
        for (Group group : feed.groups) {
            if (-group.getId() == source_id) {
                unit = group;
                break;
            }
        }
    }

    public void setUnitFrom(Feed feed) {
        if (source_id >= 0) {
            setProfileFrom(feed);
        } else {
            setGroupFrom(feed);
        }
    }
}
