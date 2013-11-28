package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.stiggpwnz.vibes.util.HtmlDeserializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Audio {

    public static final Map<Audio, String> URL_CACHE = new ConcurrentHashMap<Audio, String>();

    public static class Response extends Result<Audio[]> {

    }

    public int aid;
    public int owner_id;

    @JsonDeserialize(using = HtmlDeserializer.class) public String artist;
    @JsonDeserialize(using = HtmlDeserializer.class) public String title;

    public int    duration;
    public int    lyrics_id;
    public String url;

    @JsonIgnore
    public String getAudios() {
        return String.format("%d_%d", owner_id, aid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Audio audio = (Audio) o;

        if (aid != audio.aid) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return aid;
    }
}
