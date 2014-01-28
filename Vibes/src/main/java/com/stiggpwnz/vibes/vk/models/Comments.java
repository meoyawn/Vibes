package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Comments implements Serializable {

    private int count;
    private int canPost;

    public int getCount() { return count; }

    public void setCount(int count) { this.count = count; }

    @JsonProperty("can_post")
    public int getCanPost() { return canPost; }

    @JsonProperty("can_post")
    public void setCanPost(int canPost) { this.canPost = canPost; }
}
