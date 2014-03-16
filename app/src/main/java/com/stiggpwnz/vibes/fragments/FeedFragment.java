package com.stiggpwnz.vibes.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.neenbedankt.bundles.annotation.Argument;
import com.stiggpwnz.vibes.Dagger;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.adapters.FeedAdapter;
import com.stiggpwnz.vibes.util.Images;
import com.stiggpwnz.vibes.vk.VKontakte;
import com.stiggpwnz.vibes.vk.models.Feed;
import com.stiggpwnz.vibes.vk.models.Unit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.InjectView;
import dagger.Lazy;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

public class FeedFragment extends BaseFragment {
    @Inject Lazy<VKontakte>        vkontakte;
    @Inject PublishSubject<Unit>   unitClicks;
    @Inject PublishSubject<Bitmap> loadedBitmaps;

    @Argument int ownerId;

    @InjectView(R.id.ptr_layout) PullToRefreshLayout pullToRefreshLayout;
    @InjectView(R.id.grid)       AbsListView         gridView;

    @Nullable Subscription subscription;
    @NotNull  Subscription unitClickSubscription;
    @NotNull  Subscription bitmaps;

    @Nullable Feed lastResult;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FeedFragmentBuilder.injectArguments(this);
        Dagger.inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.newsfeed, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ActionBarPullToRefresh.from(getActivity())
                .listener(view1 -> makeRequest())
                .setup(pullToRefreshLayout);

        init(savedInstanceState);

        unitClickSubscription = AndroidObservable.fromFragment(this, unitClicks)
                .subscribe(unit -> {
                    getFragmentManager().beginTransaction()
                            .replace(R.id.content_frame, FeedFragmentBuilder.newFeedFragment(unit.getId()))
                            .addToBackStack(null)
                            .commit();
                });

        bitmaps = AndroidObservable.fromFragment(this, loadedBitmaps
                .debounce(500, TimeUnit.MILLISECONDS)
                .flatMap(bitmap -> Observable.create((Subscriber<? super Bitmap> subscriber) -> {
                    subscriber.onNext(Images.blur(getActivity(), bitmap));
                })
                        .map(blurred -> Images.transition(gridView.getBackground(), blurred, getResources()))
                        .subscribeOn(Schedulers.computation())))
                .subscribe(transition -> {
                    gridView.setBackgroundDrawable(transition);
                    transition.startTransition(500);
                });
    }

    private void init(@Nullable Bundle savedInstanceState) {
        if (subscription == null) {
            if (lastResult == null && savedInstanceState != null && savedInstanceState.containsKey("feed")) {
                lastResult = (Feed) savedInstanceState.get("feed");
            }
            if (lastResult == null) {
                makeRequest();
            } else {
                updateView();
            }
        } else {
            pullToRefreshLayout.setRefreshing(true);
        }
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("feed", lastResult);
    }

    @Override public void onDestroyView() {
        unitClickSubscription.unsubscribe();
        bitmaps.unsubscribe();
        super.onDestroyView();
    }

    void updateView() {
        if (getView() != null && lastResult != null && getActivity() != null) {
            pullToRefreshLayout.setRefreshComplete();
            gridView.setAdapter(new FeedAdapter(getLayoutInflater(null), lastResult.getItems()));
        }
    }

    Observable<Feed> createNewFeedObservable() {
        return (ownerId == 0 ?
                vkontakte.get().getNewsFeed(0) :
                vkontakte.get().getWall(ownerId, null, 0))
                .subscribeOn(Schedulers.io());
    }

    void makeRequest() {
        pullToRefreshLayout.setRefreshing(true);
        subscription = AndroidObservable.fromFragment(this, createNewFeedObservable())
                .doOnEach(notification -> subscription = null)
                .subscribe(feed -> {
                    lastResult = feed;
                    updateView();
                }, e -> {
                    Timber.e(e, "error getting feed");
                    pullToRefreshLayout.setRefreshComplete();
                });
    }

    @Override public void onDestroy() {
        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
        super.onDestroy();
    }
}
