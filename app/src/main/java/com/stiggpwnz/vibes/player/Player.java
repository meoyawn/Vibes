package com.stiggpwnz.vibes.player;

import android.media.MediaPlayer;

import com.stiggpwnz.vibes.db.DatabaseHelper;
import com.stiggpwnz.vibes.vk.VKontakte;
import com.stiggpwnz.vibes.vk.models.Audio;

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

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import static org.squirrelframework.foundation.fsm.Conditions.not;
import static org.squirrelframework.foundation.fsm.Conditions.or;

/**
 * Created by adel on 11/28/13
 */
public class Player {
    public static enum State {
        EMPTY,
        PREPARING_TO_PLAY,
        PREPARING_TO_PAUSE,
        PLAYING,
        PAUSED
    }

    @Accessors(chain = true)
    public static enum Event {
        PLAY,
        NEXT,
        PREV,
        NTH,
        PREPARED,
        COMPLETED,
        RESETTED,
        ERROR;

        @Getter @Setter int         position;
        @Getter @Setter IOException exception;
    }

    @Data
    @RequiredArgsConstructor(suppressConstructorProperties = true)
    static class UrlSource {
        final VKontakte      vKontakte;
        final DatabaseHelper databaseHelper;

        public String getUrl(Audio audio) {
            String url = databaseHelper.getUrl(audio);
            if (url == null) {
                url = vKontakte.getUrl(audio);
                databaseHelper.putUrl(audio);
            }
            return url;
        }
    }

    @RequiredArgsConstructor(suppressConstructorProperties = true)
    public static class PlayerStateMachine extends AbstractStateMachine<PlayerStateMachine, State, Event, PlayerQueue> {
        final MediaPlayer mediaPlayer;
        final UrlSource   urlSource;
    }


    static PlayerStateMachine playground(MediaPlayer mediaPlayer, UrlSource urlSource) {
        StateMachineBuilder<PlayerStateMachine, State, Event, PlayerQueue> builder =
                StateMachineBuilderFactory.create(PlayerStateMachine.class, State.class,
                        Event.class, PlayerQueue.class, MediaPlayer.class, VKontakte.class);

        builder.transit().fromAny().toAny().on(Event.NEXT).perform(nextBefore());
        builder.transit().fromAny().toAny().on(Event.PREV).perform(prevBefore());
        builder.transit().fromAny().toAny().on(Event.NTH).perform(nth());
        builder.transit().fromAny().to(State.EMPTY).onAny().perform(reset());

        builder.transit().from(State.EMPTY).to(State.PREPARING_TO_PLAY).on(Event.PLAY).perform(resetAndPrepare());
        builder.transit().from(State.EMPTY).to(State.PREPARING_TO_PLAY).on(Event.NTH);
        builder.transit().from(State.EMPTY).to(State.EMPTY).onAny();

        builder.transit().from(State.PREPARING_TO_PLAY).to(State.PREPARING_TO_PAUSE).on(Event.PLAY);
        builder.transit().from(State.PREPARING_TO_PLAY).to(State.PLAYING).on(Event.PREPARED).perform(startPlaying());
        builder.transit().from(State.PREPARING_TO_PLAY).to(State.PREPARING_TO_PLAY).onAny().perform(resetAndPrepare());

        builder.transit().from(State.PREPARING_TO_PAUSE).to(State.PREPARING_TO_PLAY).on(Event.PLAY);
        builder.transit().from(State.PREPARING_TO_PAUSE).to(State.PREPARING_TO_PLAY).on(Event.NTH);
        builder.transit().from(State.PREPARING_TO_PAUSE).to(State.PAUSED).on(Event.PREPARED);
        builder.transit().from(State.PREPARING_TO_PAUSE).to(State.EMPTY).onAny();

        builder.transit().from(State.PLAYING).to(State.PAUSED).on(Event.PLAY).perform(pause());
        builder.transit().from(State.PLAYING).to(State.PLAYING).on(Event.COMPLETED).when(repeatSingle()).perform(toTheBeginning());
        builder.transit().from(State.PLAYING).to(State.PREPARING_TO_PAUSE).on(Event.COMPLETED).when(atTheEndAndDontRepeat());
        builder.transit().from(State.PLAYING).to(State.PREPARING_TO_PLAY).on(Event.COMPLETED).when(not(or(repeatSingle(), atTheEndAndDontRepeat()))).perform(next());
        builder.transit().from(State.PLAYING).to(State.PREPARING_TO_PLAY).onAny();

        builder.transit().from(State.PAUSED).to(State.PLAYING).on(Event.PLAY).perform(startPlaying());
        builder.transit().from(State.PAUSED).to(State.PREPARING_TO_PLAY).on(Event.NTH);
        builder.transit().from(State.PAUSED).to(State.EMPTY).onAny();

        StateMachineConfiguration configuration = StateMachineConfiguration.create();
        return builder.newStateMachine(State.EMPTY, configuration, mediaPlayer, urlSource);
    }

    private static List<Action<PlayerStateMachine, State, Event, PlayerQueue>> next() {
        return Arrays.asList(nextBefore(), resetAndPrepare());
    }

    private static List<Action<PlayerStateMachine, State, Event, PlayerQueue>> nth() {
        return Arrays.asList(nthBefore(), resetAndPrepare());
    }


    private static AnonymousAction<PlayerStateMachine, State, Event, PlayerQueue> pause() {
        return new AnonymousAction<PlayerStateMachine, State, Event, PlayerQueue>() {
            @Override
            public void execute(State from, State to, Event event, PlayerQueue context, PlayerStateMachine stateMachine) {
                stateMachine.mediaPlayer.pause();
            }
        };
    }

    private static AnonymousAction<PlayerStateMachine, State, Event, PlayerQueue> toTheBeginning() {
        return new AnonymousAction<PlayerStateMachine, State, Event, PlayerQueue>() {
            @Override
            public void execute(State from, State to, Event event, PlayerQueue context, PlayerStateMachine stateMachine) {
                stateMachine.mediaPlayer.seekTo(0);
                stateMachine.mediaPlayer.start();
            }
        };
    }

    private static AnonymousAction<PlayerStateMachine, State, Event, PlayerQueue> reset() {
        return new AnonymousAction<PlayerStateMachine, State, Event, PlayerQueue>() {
            @Override public boolean isAsync() { return true; }

            @Override
            public void execute(State from, State to, Event event, PlayerQueue context, PlayerStateMachine stateMachine) {
                stateMachine.mediaPlayer.reset();
            }
        };
    }

    private static AnonymousAction<PlayerStateMachine, State, Event, PlayerQueue> startPlaying() {
        return new AnonymousAction<PlayerStateMachine, State, Event, PlayerQueue>() {
            @Override
            public void execute(State from, State to, Event event, PlayerQueue context, PlayerStateMachine stateMachine) {
                stateMachine.mediaPlayer.start();
            }
        };
    }

    private static AnonymousAction<PlayerStateMachine, State, Event, PlayerQueue> nthBefore() {
        return new AnonymousAction<PlayerStateMachine, State, Event, PlayerQueue>() {
            @Override public int weight() { return BEFORE_WEIGHT; }

            @Override
            public void execute(State from, State to, Event event, PlayerQueue context, PlayerStateMachine stateMachine) {
                context.getPosition().set(event.getPosition());
            }
        };
    }

    private static AnonymousAction<PlayerStateMachine, State, Event, PlayerQueue> prevBefore() {
        return new AnonymousAction<PlayerStateMachine, State, Event, PlayerQueue>() {
            @Override public int weight() { return BEFORE_WEIGHT; }

            @Override
            public void execute(State from, State to, Event event, PlayerQueue context, PlayerStateMachine stateMachine) {
                context.prev();
            }
        };
    }

    private static AnonymousAction<PlayerStateMachine, State, Event, PlayerQueue> nextBefore() {
        return new AnonymousAction<PlayerStateMachine, State, Event, PlayerQueue>() {
            @Override public int weight() { return BEFORE_WEIGHT; }

            @Override
            public void execute(State from, State to, Event event, PlayerQueue context, PlayerStateMachine stateMachine) {
                context.next(true);
            }
        };
    }

    private static AnonymousAction<PlayerStateMachine, State, Event, PlayerQueue> resetAndPrepare() {
        return new AnonymousAction<PlayerStateMachine, State, Event, PlayerQueue>() {
            @Override public boolean isAsync() { return true; }

            @Override
            public void execute(State from, State to, Event event, PlayerQueue context, PlayerStateMachine machine) {
                machine.mediaPlayer.reset();
                try {
                    String url = machine.urlSource.getUrl(context.current());
                    machine.mediaPlayer.setDataSource(url);
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
