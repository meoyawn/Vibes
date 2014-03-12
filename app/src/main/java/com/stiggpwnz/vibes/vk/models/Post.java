package com.stiggpwnz.vibes.vk.models;

import android.text.TextUtils;
import android.text.format.DateUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.stiggpwnz.vibes.util.Text;

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
    @NotNull @JsonDeserialize(using = Text.class)                   String       text;
    @Nullable                                                       Attachment[] attachments;
    @JsonProperty("copy_post_date")                                 long         copyPostDate;
    @JsonProperty("copy_owner_id")                                  int          copyOwnerId;
    @JsonProperty("copy_text") @JsonDeserialize(using = Text.class) String       copyText;

    // computed stuff
    @Nullable Photo[] photos;
    @NotNull  Audio[] audios;
    @NotNull  Unit    originalUnit;
    @Nullable Unit    repostedUnit;

    @SuppressWarnings("unused") public Post(int count) { this.count = count; }

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

    public CharSequence repostRelativeTimeString() {
        return DateUtils.getRelativeTimeSpanString(copyPostDate * 1000);
    }

    public CharSequence relativeTimeString() {
        return DateUtils.getRelativeTimeSpanString(date * 1000);
    }

    public boolean hasRepostedText() { return !TextUtils.isEmpty(copyText); }

    public boolean hasOriginalText() { return !TextUtils.isEmpty(text); }

    @Nullable public Photo getFirstPhoto() {
        return photos != null && photos.length > 0 ?
                photos[0] :
                null;
    }

    @Nullable static Unit from(Set<? extends Unit> units, int sourceId) {
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
        originalUnit = from(units, sourceId);
    }

    public void setRepostedUnitFrom(Feed feed) {
        Set<? extends Unit> units = copyOwnerId >= 0 ?
                feed.getProfiles() :
                feed.getGroups();
        repostedUnit = from(units, copyOwnerId);
    }

    @JsonProperty("from_id") public void setFromId(int fromId) { this.sourceId = fromId; }
}
