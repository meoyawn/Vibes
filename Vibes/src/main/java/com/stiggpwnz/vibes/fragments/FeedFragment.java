package com.stiggpwnz.vibes.fragments;

import android.os.Bundle;

import com.android.ex.widget.StaggeredGridView;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.adapters.FeedAdapter;
import com.stiggpwnz.vibes.fragments.base.RetainedProgressFragment;
import com.stiggpwnz.vibes.util.JacksonSerializer;
import com.stiggpwnz.vibes.vk.VKontakte;
import com.stiggpwnz.vibes.vk.models.Feed;

import javax.inject.Inject;

import butterknife.InjectView;
import dagger.Lazy;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.concurrency.AndroidSchedulers;
import rx.concurrency.Schedulers;
import rx.util.functions.Action1;
import rx.util.functions.Func1;
import timber.log.Timber;

public class FeedFragment extends RetainedProgressFragment {

    @Inject Lazy<VKontakte>         vkontakte;
    @Inject Lazy<JacksonSerializer> jackson;

    @InjectView(R.id.grid) StaggeredGridView gridView;

    Feed         lastResult;
    Subscription subscription;

    public FeedFragment() {

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
                Observable.just(savedInstanceState)
                        .map(new Func1<Bundle, Feed>() {

                            @Override
                            public Feed call(Bundle bundle) {
                                return jackson.get().deserialize(bundle.getString("feed"), Feed.class);
                            }
                        })
                        .subscribeOn(Schedulers.threadPoolForComputation())
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setContentView(R.layout.newsfeed);
        gridView.setItemMargin(getResources().getDimensionPixelSize(R.dimen.padding_list));
        gridView.setColumnCount(getResources().getInteger(R.integer.num_columns));
        updateView();
    }

    void updateView() {
        if (getView() != null && lastResult != null) {
            gridView.setAdapter(new FeedAdapter(getActivity(), lastResult));
            setContentShown(true);
        }
    }

    void makeRequest() {
        if (getView() != null) {
            setContentShown(false);
        }
        int ownerId = getArguments().getInt("owner_id");
        Observable<Feed> feedObservable = ownerId == 0 ?
                vkontakte.get().getNewsFeed(0) :
                vkontakte.get().getWall(ownerId, null, 0);
        subscription = feedObservable.subscribeOn(Schedulers.threadPoolForIO())
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
    protected void onRetryButtonClick() {
        makeRequest();
    }

    @Override
    public void onDestroy() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
        super.onDestroy();
    }
}
