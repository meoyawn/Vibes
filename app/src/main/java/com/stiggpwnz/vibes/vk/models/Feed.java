package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.Data;

public @Data class Feed implements Serializable {
    @NotNull                    Post[]       items;
    @Nullable                   Set<Profile> profiles;
    @Nullable                   Set<Group>   groups;
    @JsonProperty("new_offset") int          newOffset;

    public void removeFirstItem() {
        Post[] src = items;
        items = new Post[src.length - 1];
        System.arraycopy(src, 1, items, 0, items.length);
    }

    public void filterAudios() {
        List<Post> posts = new ArrayList<>(items.length);
        for (Post post : items) {
            if (post.hasAudios()) {
                post.setUnitFrom(this);
                post.calculateMedia();
                posts.add(post);
            }
        }
        items = posts.toArray(new Post[posts.size()]);
    }
}
