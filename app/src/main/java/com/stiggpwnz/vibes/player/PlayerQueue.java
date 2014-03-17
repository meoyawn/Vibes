package com.stiggpwnz.vibes.player;

import com.stiggpwnz.vibes.util.Persistence;
import com.stiggpwnz.vibes.vk.models.Audio;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import rx.Observable;

/**
 * Created by adel on 11/03/14
 */
@Data
@RequiredArgsConstructor(suppressConstructorProperties = true)
public abstract class PlayerQueue {
    public static enum Repeat {
        DONT_REPEAT, SINGLE, ALL
    }

    protected final @NotNull AtomicInteger position;
    private final @NotNull   Persistence   persistence;

    @NotNull public abstract Audio currentAudio();

    protected abstract int length();

    protected abstract int sectionStart();

    protected abstract int sectionEndInclusive();

    protected abstract boolean repeatingCurrentSection();

    protected abstract void stopRepeatingCurrentSection();

    @NotNull public Audio next(boolean user) {
        if (atTheEnd()) {
            if (user) {
                stopRepeatingCurrentSection();
                position.set(0);
            } else {
                if (repeatingCurrentSection()) {
                    position.set(sectionStart());
                } else {
                    if (getRepeat() == Repeat.ALL) {
                        position.set(0);
                    }
                }
            }
        } else if (atTheEndOfTheSection()) {
            if (user) {
                stopRepeatingCurrentSection();
                position.incrementAndGet();
            } else {
                if (repeatingCurrentSection()) {
                    position.set(sectionStart());
                } else {
                    position.incrementAndGet();
                }
            }
        } else {
            position.incrementAndGet();
        }
        return currentAudio();
    }

    @NotNull public Audio prev() {
        if (!position.compareAndSet(0, length() - 1)) {
            position.decrementAndGet();
        }
        return currentAudio();
    }

    public boolean atTheEndOfTheSection() {return position.get() == sectionEndInclusive(); }

    public boolean atTheEnd() { return position.get() == length() - 1; }

    public abstract Observable<PlayerQueue> shuffle();

    public Repeat getRepeat() { return persistence.repeat(); }
}
