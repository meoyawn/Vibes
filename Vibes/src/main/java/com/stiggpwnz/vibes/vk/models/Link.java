package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Link {

    public String url;
    public String title;
    public String description;
    public String image_src;
    public String preview_page;
}
