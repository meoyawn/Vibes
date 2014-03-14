package com.stiggpwnz.vibes.player;

import android.media.MediaPlayer;

import com.stiggpwnz.vibes.vk.VKontakte;

import org.squirrelframework.foundation.fsm.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Created by adel on 11/28/13
 */
public class Player {
    public static enum State {
        EMPTY,
        PREPARING_TO_PLAY,
        PREPARING_TO_PAUSE,
        PLAYING,
        PAUSED,
        RESETTING_TO_PLAY,
        RESETTING_TO_PAUSE
    }

    public static enum Event {
        PLAY,
        NEXT,
        PREV,
        NTH,
        PREPARED,
        COMPLETED;

        @Getter @Setter int position;
    }

    static void playground() {
        StateMachineBuilder<PlayerStateMachine, State, Event, PlayerQueue> builder =
                StateMachineBuilderFactory.create(PlayerStateMachine.class, State.class, Event.class, PlayerQueue.class);

        builder.transition().from(State.EMPTY).to(State.PREPARING_TO_PLAY).on(Event.PLAY);
        builder.transition().from(State.EMPTY).to(State.EMPTY).on(Event.NEXT);
        builder.transition().from(State.EMPTY).to(State.EMPTY).on(Event.PREV);
        builder.transition().from(State.EMPTY).to(State.PREPARING_TO_PLAY).on(Event.NTH);

        builder.transition().from(State.PREPARING_TO_PLAY).to(State.PREPARING_TO_PAUSE).on(Event.PLAY);
        builder.transition().from(State.PREPARING_TO_PLAY).to(State.RESETTING_TO_PLAY).on(Event.NEXT);
        builder.transition().from(State.PREPARING_TO_PLAY).to(State.RESETTING_TO_PLAY).on(Event.PREV);
        builder.transition().from(State.PREPARING_TO_PLAY).to(State.RESETTING_TO_PLAY).on(Event.NTH);
        builder.transition().from(State.PREPARING_TO_PLAY).to(State.PLAYING).on(Event.PREPARED);

        builder.transition().from(State.PREPARING_TO_PAUSE).to(State.PREPARING_TO_PLAY).on(Event.PLAY);
        builder.transition().from(State.PREPARING_TO_PAUSE).to(State.RESETTING_TO_PAUSE).on(Event.NEXT);
        builder.transition().from(State.PREPARING_TO_PAUSE).to(State.RESETTING_TO_PAUSE).on(Event.PREV);
        builder.transition().from(State.PREPARING_TO_PAUSE).to(State.RESETTING_TO_PLAY).on(Event.NTH);
        builder.transition().from(State.PREPARING_TO_PAUSE).to(State.PAUSED).on(Event.PREPARED);
    }

    @RequiredArgsConstructor(suppressConstructorProperties = true)
    public static class PlayerStateMachine extends AbstractStateMachine<PlayerStateMachine, State, Event, PlayerQueue> {
        final MediaPlayer mediaPlayer;
        final VKontakte   vKontakte;
    }
}
