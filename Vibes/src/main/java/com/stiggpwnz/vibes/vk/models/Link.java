package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Link implements Serializable {

    public String url;
    public String title;
    public String description;
    public String image_src;
    public String preview_page;
}
