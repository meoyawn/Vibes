package com.stiggpwnz.vibes.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.neenbedankt.bundles.annotation.Argument;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.adapters.FeedAdapter;
import com.stiggpwnz.vibes.fragments.base.BaseFragment;
import com.stiggpwnz.vibes.qualifiers.IOThreadPool;
import com.stiggpwnz.vibes.qualifiers.MainThread;
import com.stiggpwnz.vibes.util.JacksonSerializer;
import com.stiggpwnz.vibes.vk.VKontakte;
import com.stiggpwnz.vibes.vk.models.Feed;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import butterknife.InjectView;
import dagger.Lazy;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import timber.log.Timber;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

public class FeedFragment extends BaseFragment {
    @Inject               Lazy<VKontakte>         vkontakte;
    @Inject               Lazy<JacksonSerializer> jackson;
    @Inject @IOThreadPool Scheduler               ioThreadPool;
    @Inject @MainThread   Scheduler               mainThread;

    @Argument int ownerId;

    @InjectView(R.id.ptr_layout) @NotNull PullToRefreshLayout pullToRefreshLayout;
    @InjectView(R.id.grid) @NotNull       AbsListView         staggeredGridView;

    Feed         lastResult;
    Subscription subscription;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FeedFragmentBuilder.injectArguments(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.newsfeed, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ActionBarPullToRefresh.from(getActivity())
                .allChildrenArePullable()
                .listener(view1 -> makeRequest())
                .setup(pullToRefreshLayout);

        if (subscription == null) {
            if (lastResult == null && savedInstanceState != null && savedInstanceState.containsKey("feed")) {
                lastResult = (Feed) savedInstanceState.get("feed");
            }
            if (lastResult == null) {
                makeRequest();
            }
        } else {
            pullToRefreshLayout.setRefreshing(true);
        }
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("feed", lastResult);
    }

    void updateView() {
        if (getView() != null && lastResult != null && getActivity() != null) {
            pullToRefreshLayout.setRefreshComplete();
            staggeredGridView.setAdapter(new FeedAdapter(LayoutInflater.from(getActivity()), lastResult.getItems()));
        }
    }

    void makeRequest() {
        pullToRefreshLayout.setRefreshing(true);
        Observable<Feed> feedObservable = ownerId == 0 ?
                vkontakte.get().getNewsFeed(0) :
                vkontakte.get().getWall(ownerId, null, 0);

        subscription = AndroidObservable.fromFragment(this, feedObservable.subscribeOn(ioThreadPool))
                .subscribe(new Observer<Feed>() {
                    @Override public void onCompleted() {}

                    @Override public void onError(Throwable throwable) {
                        Timber.e(throwable, "error getting feed");
                        pullToRefreshLayout.setRefreshComplete();
                    }

                    @Override public void onNext(Feed feed) {
                        lastResult = feed;
                        updateView();
                    }
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
