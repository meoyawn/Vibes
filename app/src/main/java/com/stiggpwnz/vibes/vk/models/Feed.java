package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.Data;
import rx.functions.Func1;

public @Data class Feed implements Serializable {
    Post[]       items;
    Set<Profile> profiles;
    Set<Group>   groups;
    @JsonProperty("new_offset") int newOffset;

    public static Func1<Feed, Feed> removeFirstItem() {
        return feed -> {
            Post[] src = feed.items;
            feed.items = new Post[src.length - 1];
            System.arraycopy(src, 1, feed.items, 0, feed.items.length);
            return feed;
        };
    }

    public static Func1<Feed, Feed> filterAudios() {
        return feed -> {
            List<Post> posts = new ArrayList<>(feed.items.length);
            for (Post post : feed.items) {
                if (post.hasAudios()) {
                    post.setUnitFrom(feed);
                    post.calculateSelfAudios();
                    post.calculateSelfPhotos();
                    posts.add(post);
                }
            }
            feed.items = posts.toArray(new Post[posts.size()]);
            return feed;
        };
    }

    public static class Response extends Result<Feed> {}
}
