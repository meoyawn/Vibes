package com.stiggpwnz.vibes.media;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

import com.stiggpwnz.vibes.vk.VKontakte;
import com.stiggpwnz.vibes.vk.models.Audio;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Func1;
import timber.log.Timber;

/**
 * Created by adel on 11/28/13
 */
@Singleton
public class Player {
    public static enum State {PREPARING, PLAYING, PAUSED, RESETTING, IDLE}

    private Subscription subscription;


    State           state;
    MediaPlayer     mediaPlayer;
    Lazy<VKontakte> vKontakteLazy;
    Lazy<Handler>   handlerLazy;

    public Audio audio;

    @Inject
    public Player(Lazy<VKontakte> vKontakteLazy, Lazy<Handler> handlerLazy) {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener());
        this.vKontakteLazy = vKontakteLazy;
        this.handlerLazy = handlerLazy;
    }

    public void release() {
        mediaPlayer.release();
    }

    boolean isPrepared() {
        return state == State.PREPARING ||
                state == State.PLAYING ||
                state == State.PAUSED;
    }


    public void play(final Audio audio) {
        this.audio = audio;
        if (subscription != null) {
            subscription.unsubscribe();
        }
        Observable<MediaPlayer> prepare;
        if (isPrepared()) {
            Timber.d("prepared, will reset");
            prepare = resetObservable().flatMap(new Func1<MediaPlayer, Observable<? extends MediaPlayer>>() {

                @Override
                public Observable<? extends MediaPlayer> call(MediaPlayer mediaPlayer) {
                    return prepareObservable(audio);
                }
            });
        } else {
            prepare = prepareObservable(audio);
        }
        subscription = prepare.subscribeOn(Schedulers.io()).subscribe(onPreparedObserver());
    }

    private Observable<MediaPlayer> resetObservable() {
        return Observable.create(new Observable.OnSubscribeFunc<MediaPlayer>() {

            @Override
            public Subscription onSubscribe(Observer<? super MediaPlayer> observer) {
                state = State.RESETTING;
                handlerLazy.get().removeCallbacks(progressUpdater);
                mediaPlayer.reset();
                state = State.IDLE;
                observer.onNext(mediaPlayer);
                return Subscriptions.empty();
            }
        });
    }

    private Observable<MediaPlayer> prepareObservable(Audio audio) {
        // TODO check from URL cache
        if (false) {
            Timber.d("has url in cache");
            return playObservable(audio);
        } else {
            return vKontakteLazy.get().getAudioUrl(audio)
                    .flatMap(new Func1<Audio, Observable<MediaPlayer>>() {

                        @Override
                        public Observable<MediaPlayer> call(Audio audio) {
                            return playObservable(audio);
                        }
                    });
        }
    }

    static MediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener() {
        return new MediaPlayer.OnBufferingUpdateListener() {

            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                if (!Looper.getMainLooper().equals(Looper.myLooper())) {
                    throw new RuntimeException("not on the main thread");
                }
                // TODO FUCK
            }
        };
    }

    final Runnable progressUpdater = new Runnable() {

        @Override
        public void run() {
            handlerLazy.get().postDelayed(this, 16);
            // TODO FUCK
        }
    };

    private Observer<MediaPlayer> onPreparedObserver() {
        return new Observer<MediaPlayer>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable throwable) {
                Timber.e(throwable, "error while preparing");
            }

            @Override
            public void onNext(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
                state = State.PLAYING;
                handlerLazy.get().post(progressUpdater);
            }
        };
    }

    Observable<MediaPlayer> playObservable(final Audio audio) {
        return Observable.create(new Observable.OnSubscribeFunc<MediaPlayer>() {

            @Override
            public Subscription onSubscribe(Observer<? super MediaPlayer> observer) {
                try {
                    // TODO get from URL cache
                    String path = null;
                    Timber.d("setting source %s", path);

                    state = State.PREPARING;
                    mediaPlayer.setDataSource(path);
                    mediaPlayer.prepare();
                    state = State.PAUSED;
                    observer.onNext(mediaPlayer);
                } catch (Throwable throwable) {
                    observer.onError(throwable);
                }
                return Subscriptions.empty();
            }
        });
    }
}
