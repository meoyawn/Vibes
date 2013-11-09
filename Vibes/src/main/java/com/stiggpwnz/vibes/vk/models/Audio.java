package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.stiggpwnz.vibes.util.HtmlDeserializer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Audio {

    public int aid;
    public int owner_id;

    @JsonDeserialize(using = HtmlDeserializer.class) public String artist;
    @JsonDeserialize(using = HtmlDeserializer.class) public String title;

    public int duration;
    public int lyrics_id;
}
