package com.stiggpwnz.vibes.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.etsy.android.grid.StaggeredGridView;
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
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.util.functions.Action1;
import rx.util.functions.Func1;
import timber.log.Timber;

public class FeedFragment extends BaseFragment {

    @Inject Lazy<VKontakte>         vkontakte;
    @Inject Lazy<JacksonSerializer> jackson;

    @InjectView(R.id.grid) StaggeredGridView gridView;

    Feed         lastResult;
    Subscription subscription;

    public FeedFragment() {
        // restore state
    }

    public FeedFragment(int ownerId) {
        Bundle args = new Bundle();
        args.putInt("owner_id", ownerId);
        setArguments(args);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (lastResult == null) {
            if (savedInstanceState == null) {
                makeRequest();
            } else {
                Observable.from(savedInstanceState)
                        .map(new Func1<Bundle, Feed>() {

                            @Override
                            public Feed call(Bundle bundle) {
                                return jackson.get().deserialize(bundle.getString("feed"), Feed.class);
                            }
                        })
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Feed>() {

                            @Override
                            public void call(Feed feed) {
                                lastResult = feed;
                                updateView();
                            }
                        });
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Observable.just(outState)
                .subscribeOn(Schedulers.threadPoolForComputation())
                .subscribe(new Action1<Bundle>() {

                    @Override
                    public void call(Bundle bundle) {
                        bundle.putString("feed", jackson.get().serialize(lastResult));
                    }
                });
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

    void updateView() {
        if (getView() != null && lastResult != null) {
            gridView.setAdapter(new FeedAdapter(getActivity(), lastResult));
            // TODO loading finished
        }
    }

    void makeRequest() {
        if (getView() != null) {
            // TODO loading started
        }
        int ownerId = getArguments().getInt("owner_id");
        Observable<Feed> feedObservable = ownerId == 0 ?
                vkontakte.get().getNewsFeed(0) :
                vkontakte.get().getWall(ownerId, null, 0);
        subscription = feedObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Feed>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Timber.e(throwable, "error getting feed");
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
