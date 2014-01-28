package com.stiggpwnz.vibes.vk;

import com.stiggpwnz.vibes.vk.models.Audio;
import com.stiggpwnz.vibes.vk.models.Feed;
import com.stiggpwnz.vibes.vk.models.Result;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.MultipleAssignmentSubscription;
import rx.util.functions.Func2;

import static rx.Observable.OnSubscribeFunc;

/**
 * Created by adel on 11/8/13
 */
@Singleton
public class VKontakte {

    static class RetryAuth<T> implements OnSubscribeFunc<T> {

        private final Observable<? extends Result<T>> source;
        private final CompositeSubscription subscription = new CompositeSubscription();
        private final Lazy<VKAuth> vkAuthLazy;

        public RetryAuth(Observable<? extends Result<T>> source, Lazy<VKAuth> vkAuthLazy) {
            this.source = source;
            this.vkAuthLazy = vkAuthLazy;
        }

        @Override
        public Subscription onSubscribe(Observer<? super T> observer) {
            MultipleAssignmentSubscription rescursiveSubscription = new MultipleAssignmentSubscription();
            subscription.add(Schedulers.io().schedule(rescursiveSubscription, attemptSubscription(observer)));
            subscription.add(rescursiveSubscription);
            return subscription;
        }

        private Func2<Scheduler, MultipleAssignmentSubscription, Subscription> attemptSubscription(final Observer<? super T> observer) {
            return new Func2<Scheduler, MultipleAssignmentSubscription, Subscription>() {
                @Override
                public Subscription call(final Scheduler scheduler, final MultipleAssignmentSubscription rescursiveSubscription) {
                    return source.subscribe(new Observer<Result<T>>() {
                        @Override
                        public void onCompleted() {
                            observer.onCompleted();
                        }

                        @Override
                        public void onError(Throwable e) {
                            observer.onError(e);
                        }

                        @Override
                        public void onNext(Result<T> v) {
                            if (v.isError()) {
                                if (v.error.isAuthError()) {
                                    vkAuthLazy.get().resetAuth();
                                    rescursiveSubscription.setSubscription(scheduler.schedule(rescursiveSubscription, attemptSubscription(observer)));
                                } else {
                                    observer.onError(v.error);
                                }
                            } else {
                                observer.onNext(v.response);
                            }
                        }
                    });
                }
            };
        }
    }

    Lazy<VKApi>  vkApiLazy;
    Lazy<VKAuth> vkAuthLazy;

    @Inject
    public VKontakte(Lazy<VKAuth> vkAuthLazy, Lazy<VKApi> vkApiLazy) {
        this.vkAuthLazy = vkAuthLazy;
        this.vkApiLazy = vkApiLazy;
    }

    public Observable<Audio[]> getAudios() {
        return retryAuth(vkApiLazy.get().getAudios());
    }

    public Observable<Audio> getAudioUrl(Audio audio) {
        return retryAuth(vkApiLazy.get().getAudioURL(audio.ownerIdAidParam()));
    }

    public Observable<Audio[]> searchAudios(String query) {
        return retryAuth(vkApiLazy.get().searchAudios(query))
                .map(Audio.removeFirstItem());
    }

    public Observable<Feed> getNewsFeed(int offset) {
        return retryAuth(vkApiLazy.get().getNewsFeed(offset))
                .map(Feed.filterAudios());
    }

    public Observable<Feed> getWall(int ownerId, String filter, int offset) {
        return retryAuth(vkApiLazy.get().getWall(ownerId, filter, offset))
                .map(Feed.removeFirstItem())
                .map(Feed.filterAudios());
    }

    <T> Observable<T> retryAuth(Observable<? extends Result<T>> observable) {
        return Observable.create(new RetryAuth<>(observable, vkAuthLazy));
    }
}
