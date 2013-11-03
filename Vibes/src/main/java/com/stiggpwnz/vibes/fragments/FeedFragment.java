package com.stiggpwnz.vibes.fragments;

import android.os.Bundle;

import com.android.ex.widget.StaggeredGridView;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.adapters.FeedAdapter;
import com.stiggpwnz.vibes.fragments.base.RetainedProgressFragment;
import com.stiggpwnz.vibes.vk.VKontakte;
import com.stiggpwnz.vibes.vk.models.Feed;

import javax.inject.Inject;

import butterknife.InjectView;
import dagger.Lazy;
import rx.Observable;
import rx.Observer;
import rx.android.concurrency.AndroidSchedulers;
import rx.concurrency.Schedulers;
import timber.log.Timber;

public class FeedFragment extends RetainedProgressFragment {

    @Inject Lazy<VKontakte> vKontakteLazy;

    @InjectView(R.id.grid) StaggeredGridView gridView;

    Feed result;

    public static FeedFragment newInstance(int ownerId) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        args.putInt("owner_id", ownerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.newsfeed);
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState) {
        gridView.setItemMargin(getResources().getDimensionPixelSize(R.dimen.padding_list));
        gridView.setColumnCount(getResources().getInteger(R.integer.num_columns));

        if (result == null) {
            makeRequest();
        } else {
            if (gridView.getAdapter() == null) {
                postResult(result);
                setContentShownNoAnimation(true);
            }
        }
    }

    void makeRequest() {
        setContentShown(false);
        int ownerId = getArguments().getInt("owner_id");
        Observable<Feed> feedObservable = ownerId == 0 ?
                vKontakteLazy.get().getNewsFeed(0) :
                vKontakteLazy.get().getWall(ownerId, null, 0);
        feedObservable.subscribeOn(Schedulers.threadPoolForIO())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Feed>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "Error getting newsfeed");
                        setContentEmpty(true);
                        setContentShown(true);
                    }

                    @Override
                    public void onNext(Feed args) {
                        setContentEmpty(false);
                        setContentShown(true);
                        postResult(args);
                    }
                });
    }

    @Override
    protected void onRetryButtonClick() {
        makeRequest();
    }

    protected void postResult(Feed result) {
        if (this.result == null) {
            this.result = result;
            Feed.filter(result);
            gridView.setAdapter(new FeedAdapter(getActivity(), result));
        } else {
            this.result.append(result);
            FeedAdapter adapter = (FeedAdapter) gridView.getAdapter();
            adapter.notifyDataSetChanged();
        }
    }
}
