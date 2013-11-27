package com.stiggpwnz.vibes.fragments;

import android.os.Bundle;

import com.android.ex.widget.StaggeredGridView;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.adapters.FeedAdapter;
import com.stiggpwnz.vibes.fragments.base.FragmentObserver;
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
import timber.log.Timber;

public class FeedFragment extends RetainedProgressFragment {

    @Inject Lazy<VKontakte>         vKontakteLazy;
    @Inject Lazy<JacksonSerializer> jacksonSerializerLazy;

    @InjectView(R.id.grid) StaggeredGridView gridView;

    Feed         lastResult;
    Subscription subscription;

    public static FeedFragment newInstance(int ownerId) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        args.putInt("owner_id", ownerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (lastResult == null) {
            if (savedInstanceState == null) {
                makeRequest();
            } else {
                lastResult = jacksonSerializerLazy.get().deserialize(savedInstanceState.getString("feed"), Feed.class);
            }
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.newsfeed;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        gridView.setItemMargin(getResources().getDimensionPixelSize(R.dimen.padding_list));
        gridView.setColumnCount(getResources().getInteger(R.integer.num_columns));
        updateView();
    }

    void updateView() {
        if (lastResult != null) {
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
                vKontakteLazy.get().getNewsFeed(0) :
                vKontakteLazy.get().getWall(ownerId, null, 0);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("feed", jacksonSerializerLazy.get().serialize(lastResult));
    }

    @Override
    public void onDestroy() {
        if (subscription != null) {
            Timber.d("unsubscribing");
            subscription.unsubscribe();
        }
        super.onDestroy();
    }
}
