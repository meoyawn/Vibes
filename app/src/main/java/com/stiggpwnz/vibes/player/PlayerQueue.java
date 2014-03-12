package com.stiggpwnz.vibes.player;

import com.stiggpwnz.vibes.vk.models.Audio;

import rx.Observable;

/**
 * Created by adel on 11/03/14
 */
public interface PlayerQueue {
    public Audio current();

    public Audio next();

    public Audio prev();

    public Audio nth(int n);

    public Observable<PlayerQueue> shuffle();

    int position();

    int isPlaying();
}
