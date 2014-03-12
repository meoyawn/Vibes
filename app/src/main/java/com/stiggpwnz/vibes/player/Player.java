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
        PREPARING,
        READY,
        RESETTING
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

    public static enum Intention {
        PLAY,
        PAUSE
    }


    static void playground() {
        StateMachineBuilder<PlayerStateMachine, State, Event, PlayerQueue> builder =
                StateMachineBuilderFactory.create(PlayerStateMachine.class, State.class, Event.class, PlayerQueue.class);
        builder.externalTransition().from(State.EMPTY).to(State.PREPARING).on(Event.PLAY);
        builder.externalTransition().from(State.EMPTY).to(State.PREPARING).on(Event.NTH);

        builder.externalTransition().from(State.PREPARING).to(State.READY).on(Event.PREPARED);
        builder.externalTransition().from(State.PREPARING).to(State.RESETTING).on(Event.NEXT);
        builder.externalTransition().from(State.PREPARING).to(State.RESETTING).on(Event.PREV);
        builder.externalTransition().from(State.PREPARING).to(State.RESETTING).on(Event.NTH);
    }

    @RequiredArgsConstructor(suppressConstructorProperties = true)
    public static class PlayerStateMachine extends AbstractStateMachine<PlayerStateMachine, State, Event, PlayerQueue> {
        final MediaPlayer mediaPlayer;
        final VKontakte   vKontakte;
    }
}
