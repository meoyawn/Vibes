package com.stiggpwnz.vibes.player;

/**
 * Created by adelnizamutdinov on 17/03/2014
 */
public enum Event {
    PLAY_PAUSE,
    NEXT,
    PREV,
    NTH,
    GOT_URL,
    PREPARED,
    COMPLETED,
    RESET_COMPLETE,
    SEEK_COMPLETE,
    ERROR;

    int       position;
    Exception exception;
    String    url;

    public int getPosition() { return position; }

    public Event setException(Exception exception) {
        this.exception = exception;
        return this;
    }
}
