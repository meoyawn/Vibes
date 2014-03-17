package com.stiggpwnz.vibes.player;

import android.media.MediaPlayer;

import com.stiggpwnz.vibes.vk.VKontakte;

import org.jetbrains.annotations.NotNull;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.AnonymousAction;
import org.squirrelframework.foundation.fsm.AnonymousCondition;
import org.squirrelframework.foundation.fsm.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.StateMachineConfiguration;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import lombok.RequiredArgsConstructor;

import static com.stiggpwnz.vibes.player.Event.COMPLETED;
import static com.stiggpwnz.vibes.player.Event.ERROR;
import static com.stiggpwnz.vibes.player.Event.GOT_URL;
import static com.stiggpwnz.vibes.player.Event.NEXT;
import static com.stiggpwnz.vibes.player.Event.NTH;
import static com.stiggpwnz.vibes.player.Event.PLAY_PAUSE;
import static com.stiggpwnz.vibes.player.Event.PREPARED;
import static com.stiggpwnz.vibes.player.Event.PREV;
import static com.stiggpwnz.vibes.player.Event.RESET_COMPLETE;
import static com.stiggpwnz.vibes.player.State.EMPTY;
import static com.stiggpwnz.vibes.player.State.GETTING_URL_TO_EMPTY;
import static com.stiggpwnz.vibes.player.State.GETTING_URL_TO_PREPARE;
import static com.stiggpwnz.vibes.player.State.PAUSED;
import static com.stiggpwnz.vibes.player.State.PLAYING;
import static com.stiggpwnz.vibes.player.State.PREPARING_TO_PAUSE;
import static com.stiggpwnz.vibes.player.State.PREPARING_TO_PLAY;
import static org.squirrelframework.foundation.fsm.Conditions.not;
import static org.squirrelframework.foundation.fsm.Conditions.or;

/**
 * Created by adel on 11/28/13
 */
@RequiredArgsConstructor(suppressConstructorProperties = true)
public class PlayerStateMachine extends AbstractStateMachine<PlayerStateMachine, State, Event, PlayerQueue> {
    @NotNull final MediaPlayer mediaPlayer;
    @NotNull final UrlSource   urlSource;

    static PlayerStateMachine playground(MediaPlayer mediaPlayer, UrlSource urlSource) {
        StateMachineBuilder<PlayerStateMachine, State, Event, PlayerQueue> builder =
                StateMachineBuilderFactory.create(PlayerStateMachine.class, State.class,
                        Event.class, PlayerQueue.class, MediaPlayer.class, VKontakte.class);

        builder.transit().fromAny().toAny().on(NEXT).perform(nextBefore());
        builder.transit().fromAny().toAny().on(PREV).perform(prevBefore());
        builder.transit().fromAny().toAny().on(NTH).perform(nthAndGetUrl());

        builder.onEntry(GETTING_URL_TO_PREPARE).perform(getUrl());

        builder.transit().from(EMPTY).to(GETTING_URL_TO_PREPARE).on(PLAY_PAUSE).perform(getUrl());
        builder.transit().from(EMPTY).to(GETTING_URL_TO_PREPARE).on(NTH); // will perform

        builder.transit().from(GETTING_URL_TO_PREPARE).to(GETTING_URL_TO_EMPTY).on(PLAY_PAUSE); // just switch
        builder.transit().from(GETTING_URL_TO_PREPARE).to(GETTING_URL_TO_PREPARE).on(NTH); // will perform
        builder.transit().from(GETTING_URL_TO_PREPARE).to(PREPARING_TO_PLAY).on(GOT_URL).perform(prepareAction());
        builder.transit().from(GETTING_URL_TO_PREPARE).to(EMPTY).onAny();

        builder.transit().from(GETTING_URL_TO_EMPTY).to(GETTING_URL_TO_PREPARE).on(PLAY_PAUSE); // just switch
        builder.transit().from(GETTING_URL_TO_EMPTY).to(GETTING_URL_TO_PREPARE).on(NTH);

        builder.transit().from(PREPARING_TO_PLAY).to(PREPARING_TO_PAUSE).on(PLAY_PAUSE);
        builder.transit().from(PREPARING_TO_PLAY).to(PLAYING).on(PREPARED).perform(startPlaying());
        builder.transit().from(PREPARING_TO_PLAY).to(PREPARING_TO_PLAY).onAny().perform(resetAndPrepare());

        builder.transit().from(PREPARING_TO_PAUSE).to(PREPARING_TO_PLAY).on(PLAY_PAUSE);
        builder.transit().from(PREPARING_TO_PAUSE).to(PREPARING_TO_PLAY).on(NTH);
        builder.transit().from(PREPARING_TO_PAUSE).to(PAUSED).on(PREPARED);
        builder.transit().from(PREPARING_TO_PAUSE).to(EMPTY).onAny();

        builder.transit().from(PLAYING).to(PAUSED).on(PLAY_PAUSE).perform(pause());
        builder.transit().from(PLAYING).to(PLAYING).on(COMPLETED).when(repeatSingle()).perform(toTheBeginning());
        builder.transit().from(PLAYING).to(PREPARING_TO_PAUSE).on(COMPLETED).when(atTheEndAndDontRepeat());
        builder.transit().from(PLAYING).to(PREPARING_TO_PLAY).on(COMPLETED).when(not(or(repeatSingle(), atTheEndAndDontRepeat()))).perform(resetAndPrepare());
        builder.transit().from(PLAYING).to(PREPARING_TO_PLAY).onAny();

        builder.transit().from(PAUSED).to(PLAYING).on(PLAY_PAUSE).perform(startPlaying());
        builder.transit().from(PAUSED).to(PREPARING_TO_PLAY).on(NTH);
        builder.transit().from(PAUSED).to(EMPTY).onAny();

        StateMachineConfiguration configuration = StateMachineConfiguration.create();
        return builder.newStateMachine(EMPTY, configuration, mediaPlayer, urlSource);
    }

    private static List<Action<PlayerStateMachine, State, Event, PlayerQueue>> nthAndGetUrl() {
        return Arrays.asList(nthBefore(), getUrl());
    }

    private static Action<PlayerStateMachine, State, Event, PlayerQueue> getUrl() {
        return new AnonymousAction<PlayerStateMachine, State, Event, PlayerQueue>() {
            @Override public boolean isAsync() { return true; }

            @Override
            public void execute(State from, State to, Event event, PlayerQueue context, PlayerStateMachine stateMachine) {
                try {
                    stateMachine.urlSource.tryToGetAndMaybeSaveUrl(context.currentAudio());
                    stateMachine.fire(GOT_URL, context);
                } catch (Exception e) {
                    stateMachine.fire(ERROR.setException(e), context);
                }
            }
        };
    }

    private static Action<PlayerStateMachine, State, Event, PlayerQueue> pause() {
        return new AnonymousAction<PlayerStateMachine, State, Event, PlayerQueue>() {
            @Override
            public void execute(State from, State to, Event event, PlayerQueue context, PlayerStateMachine stateMachine) {
                stateMachine.mediaPlayer.pause();
            }
        };
    }

    private static Action<PlayerStateMachine, State, Event, PlayerQueue> toTheBeginning() {
        return new AnonymousAction<PlayerStateMachine, State, Event, PlayerQueue>() {
            @Override
            public void execute(State from, State to, Event event, PlayerQueue context, PlayerStateMachine stateMachine) {
                stateMachine.mediaPlayer.seekTo(0);
                stateMachine.mediaPlayer.start();
            }
        };
    }

    private static Action<PlayerStateMachine, State, Event, PlayerQueue> reset() {
        return new AnonymousAction<PlayerStateMachine, State, Event, PlayerQueue>() {
            @Override public boolean isAsync() { return true; }

            @Override
            public void execute(State from, State to, Event event, PlayerQueue context, PlayerStateMachine stateMachine) {
                stateMachine.mediaPlayer.reset();
                stateMachine.fire(RESET_COMPLETE, context);
            }
        };
    }

    private static Action<PlayerStateMachine, State, Event, PlayerQueue> startPlaying() {
        return new AnonymousAction<PlayerStateMachine, State, Event, PlayerQueue>() {
            @Override
            public void execute(State from, State to, Event event, PlayerQueue context, PlayerStateMachine stateMachine) {
                stateMachine.mediaPlayer.start();
            }
        };
    }

    private static Action<PlayerStateMachine, State, Event, PlayerQueue> nthBefore() {
        return new AnonymousAction<PlayerStateMachine, State, Event, PlayerQueue>() {
            @Override public int weight() { return BEFORE_WEIGHT; }

            @Override
            public void execute(State from, State to, Event event, PlayerQueue context, PlayerStateMachine stateMachine) {
                context.getPosition().set(event.getPosition());
            }
        };
    }

    private static Action<PlayerStateMachine, State, Event, PlayerQueue> prevBefore() {
        return new AnonymousAction<PlayerStateMachine, State, Event, PlayerQueue>() {
            @Override public int weight() { return BEFORE_WEIGHT; }

            @Override
            public void execute(State from, State to, Event event, PlayerQueue context, PlayerStateMachine stateMachine) {
                context.prev();
            }
        };
    }

    private static Action<PlayerStateMachine, State, Event, PlayerQueue> nextBefore() {
        return new AnonymousAction<PlayerStateMachine, State, Event, PlayerQueue>() {
            @Override public int weight() { return BEFORE_WEIGHT; }

            @Override
            public void execute(State from, State to, Event event, PlayerQueue context, PlayerStateMachine stateMachine) {
                context.next(true);
            }
        };
    }

    private static Action<PlayerStateMachine, State, Event, PlayerQueue> prepareAction() {
        return new AnonymousAction<PlayerStateMachine, State, Event, PlayerQueue>() {
            @Override public boolean isAsync() { return true; }

            @Override
            public void execute(State from, State to, Event event, PlayerQueue context, PlayerStateMachine machine) {
                try {
                    String savedUrl = machine.urlSource.getSavedUrl(context.currentAudio());
                    machine.mediaPlayer.setDataSource(savedUrl);
                    machine.mediaPlayer.prepare();
                } catch (IOException e) {
                    machine.fire(Event.ERROR.setException(e), context);
                }
            }
        };
    }

    private static AnonymousCondition<PlayerQueue> atTheEndAndDontRepeat() {
        return new AnonymousCondition<PlayerQueue>() {
            @Override public boolean isSatisfied(PlayerQueue context) {
                return context.atTheEnd() && context.getRepeat() == PlayerQueue.Repeat.DONT_REPEAT;
            }
        };
    }

    private static AnonymousCondition<PlayerQueue> repeatSingle() {
        return new AnonymousCondition<PlayerQueue>() {
            @Override public boolean isSatisfied(PlayerQueue context) {
                return context.getRepeat() == PlayerQueue.Repeat.SINGLE;
            }
        };
    }


}
