package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Likes {

    private int count;
    private int userLikes;
    private int canLike;
    private int can_publish;

    public int getCount() { return count; }

    public void setCount(int count) { this.count = count; }

    @JsonProperty("user_likes")
    public int getUserLikes() { return userLikes; }

    @JsonProperty("user_likes")
    public void setUserLikes(int userLikes) { this.userLikes = userLikes; }

    @JsonProperty("can_like")
    public int getCanLike() { return canLike; }

    @JsonProperty("can_like")
    public void setCanLike(int canLike) { this.canLike = canLike; }

    @JsonProperty("can_publish")
    public int getCan_publish() { return can_publish; }

    @JsonProperty("can_publish")
    public void setCan_publish(int can_publish) { this.can_publish = can_publish; }
}
