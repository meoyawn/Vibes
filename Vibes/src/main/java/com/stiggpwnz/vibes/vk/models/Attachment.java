package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Attachment implements Serializable {

    public Photo photo;
    public Audio audio;
    public Link  link;
}
