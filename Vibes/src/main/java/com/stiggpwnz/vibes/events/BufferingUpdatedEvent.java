package com.stiggpwnz.vibes.events;

/**
 * Created by adel on 11/28/13
 */
public enum BufferingUpdatedEvent {

    INSTANCE;

    public int percent;

    public BufferingUpdatedEvent setPercent(int percent) {
        this.percent = percent;
        return this;
    }
}
