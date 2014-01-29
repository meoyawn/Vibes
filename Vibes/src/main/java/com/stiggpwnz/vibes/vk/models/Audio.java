package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.stiggpwnz.vibes.util.HtmlDeserializer;

import java.io.Serializable;
import java.util.Locale;

import rx.util.functions.Func1;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Audio implements Serializable {

    public static Func1<Audio[], Audio[]> removeFirstItem() {
        return new Func1<Audio[], Audio[]>() {
            @Override
            public Audio[] call(Audio[] audios) {
                Audio[] copy = new Audio[audios.length - 1];
                System.arraycopy(audios, 1, copy, 0, audios.length - 1);
                return copy;
            }
        };
    }

    private int    aid;
    private int    ownerId;
    private String artist;
    private String title;
    private int    duration;
    private int    lyricsId;
    private String url;

    public String ownerIdAidParam() { return String.format(Locale.US, "%d_%d", ownerId, aid); }

    public int getAid() { return aid; }

    public void setAid(int aid) { this.aid = aid; }

    @JsonProperty("owner_id")
    public int getOwnerId() { return ownerId; }

    @JsonProperty("owner_id")
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public String getArtist() { return artist; }

    @JsonDeserialize(using = HtmlDeserializer.class)
    public void setArtist(String artist) { this.artist = artist; }

    public String getTitle() { return title; }

    @JsonDeserialize(using = HtmlDeserializer.class)
    public void setTitle(String title) { this.title = title; }

    public int getDuration() { return duration; }

    public void setDuration(int duration) { this.duration = duration; }

    @JsonProperty("lyrics_id")
    public int getLyricsId() { return lyricsId; }

    @JsonProperty("lyrics_id")
    public void setLyricsId(int lyricsId) { this.lyricsId = lyricsId; }

    public String getUrl() { return url; }

    public void setUrl(String url) { this.url = url; }

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
