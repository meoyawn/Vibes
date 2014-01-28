package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import rx.util.functions.Func1;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Feed implements Serializable {

    public Post[]       items;
    public Set<Profile> profiles;
    public Set<Group>   groups;
    public int          new_offset;

    public static Func1<Feed, Feed> removeFirstItem() {
        return new Func1<Feed, Feed>() {
            @Override
            public Feed call(Feed feed) {
                Post[] src = feed.items;
                feed.items = new Post[src.length - 1];
                System.arraycopy(src, 1, feed.items, 0, feed.items.length);
                return feed;
            }
        };
    }

    public static Func1<Feed, Feed> filterAudios() {
        return new Func1<Feed, Feed>() {
            @Override
            public Feed call(Feed feed) {
                List<Post> posts = new ArrayList<>();
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
            }
        };
    }

    public void setWall(Post[] wall) {
        items = wall;
    }

    public static class Response extends Result<Feed> {}
}
