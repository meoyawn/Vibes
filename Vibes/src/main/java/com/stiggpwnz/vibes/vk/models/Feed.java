package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Feed {

    public List<Post>   items;
    public Set<Profile> profiles;
    public Set<Group>   groups;
    public int          new_offset;

    @JsonProperty("wall")
    public void setWall(List<Post> wall) {
        items = wall;
    }

    public void assignUnit(Post post) {
        if (post.source_id >= 0) {
            setProfile(post);
        } else {
            setGroup(post);
        }
    }

    void setProfile(Post post) {
        for (Profile profile : profiles) {
            if (profile.uid == post.source_id) {
                post.unit = profile;
                return;
            }
        }
    }

    void setGroup(Post post) {
        for (Group group : groups) {
            if (-group.gid == post.source_id) {
                post.unit = group;
                return;
            }
        }
    }

//    void append(Feed feed) {
//        new_offset = feed.new_offset;
//
//        items.addAll(feed.items);
//        profiles.addAll(feed.profiles);
//        groups.addAll(feed.groups);
//    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response extends Result<Feed> {

        public Feed response;

        @Override
        public Feed getResponse() {
            return response;
        }
    }
}
