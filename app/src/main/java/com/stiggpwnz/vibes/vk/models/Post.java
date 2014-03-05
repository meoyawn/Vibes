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

    private int          count;
    private int          sourceId;
    private long         date;
    private String       text;
    private Attachment[] attachments;

    // computed stuff
    private Photo[] photos;
    private Audio[] audios;
    private Unit    unit;

    public Post() { }

    public Post(int count) { this.count = count; }

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

    public boolean hasText() { return !TextUtils.isEmpty(text); }

    public boolean hasPhotos() { return photos != null && photos.length > 0; }

    void setProfileFrom(Feed feed) {
        for (Profile profile : feed.getProfiles()) {
            if (profile.getId() == sourceId) {
                unit = profile;
                break;
            }
        }
    }

    void setGroupFrom(Feed feed) {
        for (Group group : feed.getGroups()) {
            if (-group.getId() == sourceId) {
                unit = group;
                break;
            }
        }
    }

    public void setUnitFrom(Feed feed) {
        if (sourceId >= 0) {
            setProfileFrom(feed);
        } else {
            setGroupFrom(feed);
        }
    }

    public int getCount() { return count; }

    public void setCount(int count) { this.count = count; }

    @JsonProperty("source_id")
    public int getSourceId() { return sourceId; }

    @JsonProperty("source_id")
    public void setSourceId(int sourceId) { this.sourceId = sourceId; }

    @JsonProperty("from_id")
    public void setFromId(int fromId) { this.sourceId = fromId; }

    public long getDate() { return date; }

    public void setDate(long date) { this.date = date; }

    public String getText() { return text; }

    @JsonDeserialize(using = HtmlDeserializer.class)
    public void setText(String text) { this.text = text; }

    public void setAttachments(Attachment[] attachments) { this.attachments = attachments; }

    public Photo[] getPhotos() { return photos; }

    public void setPhotos(Photo[] photos) { this.photos = photos; }

    public Audio[] getAudios() { return audios; }

    public void setAudios(Audio[] audios) { this.audios = audios; }

    public Unit getUnit() { return unit; }

    public void setUnit(Unit unit) { this.unit = unit; }
}
