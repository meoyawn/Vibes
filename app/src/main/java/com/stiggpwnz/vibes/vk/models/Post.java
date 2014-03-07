package com.stiggpwnz.vibes.vk.models;

import android.text.TextUtils;
import android.text.format.DateUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.stiggpwnz.vibes.util.HtmlDeserializer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Set;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Post implements Serializable {
    int count;
    @JsonProperty("source_id") int sourceId;
    long date;
    @JsonDeserialize(using = HtmlDeserializer.class) @Nullable String       text;
    @Nullable                                                  Attachment[] attachments;

    // computed stuff
    @Nullable Photo[] photos;
    @NotNull  Audio[] audios;
    @NotNull  Unit    unit;

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

    public void calculateMedia() {
        audios = Audio.arrayFrom(attachments);
        photos = Photo.arrayFrom(attachments);
    }

    public CharSequence relativeTimeString() {
        return DateUtils.getRelativeTimeSpanString(date * 1000);
    }

    public boolean hasText() { return !TextUtils.isEmpty(text); }

    public boolean hasPhotos() { return photos != null && photos.length > 0; }

    static Unit from(Set<? extends Unit> units, int sourceId) {
        for (Unit unit : units) {
            if (unit.getId() == sourceId) {
                return unit;
            }
        }
        return null;
    }

    public void setUnitFrom(Feed feed) {
        Set<? extends Unit> units = sourceId >= 0 ?
                feed.getProfiles() :
                feed.getGroups();
        unit = from(units, sourceId);
    }

    @JsonProperty("from_id") public void setFromId(int fromId) { this.sourceId = fromId; }
}
