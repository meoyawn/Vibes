package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import rx.util.functions.Func1;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Feed implements Serializable {

    private Post[]       items;
    private Set<Profile> profiles;
    private Set<Group>   groups;
    private int          newOffset;

    public static Func1<Feed, Feed> removeFirstItem() {
        return new Func1<Feed, Feed>() {
            @Override
            public Feed call(Feed feed) {
                Post[] src = feed.getItems();
                feed.setItems(new Post[src.length - 1]);
                System.arraycopy(src, 1, feed.getItems(), 0, feed.getItems().length);
                return feed;
            }
        };
    }

    public static Func1<Feed, Feed> filterAudios() {
        return new Func1<Feed, Feed>() {
            @Override
            public Feed call(Feed feed) {
                List<Post> posts = new ArrayList<>(feed.getItems().length);
                for (Post post : feed.getItems()) {
                    if (post.hasAudios()) {
                        post.setUnitFrom(feed);
                        post.calculateSelfAudios();
                        post.calculateSelfPhotos();
                        posts.add(post);
                    }
                }
                feed.setItems(posts.toArray(new Post[posts.size()]));
                return feed;
            }
        };
    }

    public Post[] getItems() { return items; }

    public void setItems(Post... items) { this.items = items; }

    public void setWall(Post... wall) { this.items = wall; }

    public Set<Profile> getProfiles() { return profiles; }

    public void setProfiles(Set<Profile> profiles) { this.profiles = profiles; }

    public Set<Group> getGroups() { return groups; }

    public void setGroups(Set<Group> groups) { this.groups = groups; }

    @JsonProperty("new_offset")
    public int getNewOffset() { return newOffset; }

    @JsonProperty("new_offset")
    public void setNewOffset(int newOffset) { this.newOffset = newOffset; }

    public static class Response extends Result<Feed> {}
}
