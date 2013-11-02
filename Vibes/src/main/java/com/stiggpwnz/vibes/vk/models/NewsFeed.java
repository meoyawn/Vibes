package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsFeed {

    public List<Post>   items;
    public Set<Profile> profiles;
    public Set<Group>   groups;

    public int    new_offset;
    public String new_from;

    @JsonProperty("wall")
    public void setWall(List<Post> wall) {
        items = wall;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result extends com.stiggpwnz.vibes.vk.models.Result<NewsFeed> {

        public NewsFeed response;

        @Override
        public NewsFeed getResponse() {
            return response;
        }
    }
}
