package com.stiggpwnz.vibes.events;

/**
 * Created by adel on 11/28/13
 */
public enum ProgressUpdatedEvent {

    INSTANCE;

    public int position;
    public int max;

    public ProgressUpdatedEvent setPosition(int progress, int max) {
        this.position = progress;
        this.max = max;
        return this;
    }
}
