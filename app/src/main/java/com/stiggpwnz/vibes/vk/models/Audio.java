package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.stiggpwnz.vibes.util.HtmlDeserializer;

import java.io.Serializable;
import java.util.Locale;

import lombok.Data;
import rx.functions.Func1;

public @Data class Audio implements Serializable {
    public static Func1<Audio[], Audio[]> removeFirstItem() {
        return audios -> {
            Audio[] copy = new Audio[audios.length - 1];
            System.arraycopy(audios, 1, copy, 0, audios.length - 1);
            return copy;
        };
    }

    int aid;
    @JsonProperty("owner_id")                        int    ownerId;
    @JsonDeserialize(using = HtmlDeserializer.class) String artist;
    @JsonDeserialize(using = HtmlDeserializer.class) String title;
    int duration;
    @JsonProperty("lyrics_id") int lyricsId;
    String url;

    public String ownerIdAidParam() { return String.format(Locale.US, "%d_%d", ownerId, aid); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || ((Object) this).getClass() != o.getClass()) return false;

        Audio audio = (Audio) o;

        return aid == audio.aid;
    }

    @Override
    public int hashCode() { return aid; }

    public static class Response extends Result<Audio[]> {}

    public static class UrlResponse extends Result<Audio> {}
}
