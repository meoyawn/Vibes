package com.stiggpwnz.vibes.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.etsy.android.grid.StaggeredGridView;
import com.neenbedankt.bundles.annotation.Argument;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.adapters.FeedAdapter;
import com.stiggpwnz.vibes.fragments.base.BaseFragment;
import com.stiggpwnz.vibes.util.JacksonSerializer;
import com.stiggpwnz.vibes.vk.VKontakte;
import com.stiggpwnz.vibes.vk.models.Feed;

import javax.inject.Inject;

import butterknife.InjectView;
import dagger.Lazy;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class FeedFragment extends BaseFragment {

    @Inject Lazy<VKontakte>         vkontakte;
    @Inject Lazy<JacksonSerializer> jackson;

    @Argument int ownerId;

    @InjectView(R.id.grid) StaggeredGridView gridView;

    Feed         lastResult;
    Subscription subscription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FeedFragmentBuilder.injectArguments(this);

        if (lastResult == null) {
            if (savedInstanceState == null) {
                makeRequest();
            } else {
                lastResult = (Feed) savedInstanceState.getSerializable("feed");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.newsfeed, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("feed", lastResult);
    }

    void updateView() {
        if (getView() != null && lastResult != null) {
            gridView.setAdapter(new FeedAdapter(getActivity(), lastResult.items));
            // TODO loading finished
        }
    }

    void makeRequest() {
        if (getView() != null) {
            // TODO loading started
        }
        Observable<Feed> feedObservable = ownerId == 0 ?
                vkontakte.get().getNewsFeed(0) :
                vkontakte.get().getWall(ownerId, null, 0);

        subscription = AndroidObservable.fromFragment(this, feedObservable.subscribeOn(Schedulers.io()))
                .subscribe(new Observer<Feed>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Timber.e(throwable, "error getting feed");
                        // TODO show retry
                    }

                    @Override
                    public void onNext(Feed feed) {
                        lastResult = feed;
                        updateView();
                    }
                });
    }

    @Override
    public void onDestroy() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
        super.onDestroy();
    }
}
