package com.stiggpwnz.vibes.fragments;

import android.os.Bundle;

import com.android.ex.widget.StaggeredGridView;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.adapters.NewsFeedAdapter;
import com.stiggpwnz.vibes.fragments.base.RetainedProgressFragment;
import com.stiggpwnz.vibes.vk.VKApi;
import com.stiggpwnz.vibes.vk.models.NewsFeed.Result;

import javax.inject.Inject;

import butterknife.InjectView;
import dagger.Lazy;
import icepick.annotation.Icicle;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.concurrency.AndroidSchedulers;
import rx.concurrency.Schedulers;
import rx.subscriptions.Subscriptions;

public class NewsFeedFragment extends RetainedProgressFragment {

    @Inject Lazy<VKApi> vkApiLazy;

    @InjectView(R.id.grid) StaggeredGridView gridView;

    Result result;

    @Icicle int firstVisiblePosition;

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
            postResult(result);
            gridView.setFirstPosition(firstVisiblePosition);
            setContentShownNoAnimation(true);
        }
    }

    private void makeRequest() {
        setContentShown(false);
        Observable.create(new Observable.OnSubscribeFunc<Result>() {

            @Override
            public Subscription onSubscribe(Observer<? super Result> t1) {
                try {
                    t1.onNext(vkApiLazy.get().getNewsFeed(0));
                } catch (Throwable throwable) {
                    t1.onError(throwable);
                }
                return Subscriptions.empty();
            }
        }).subscribeOn(Schedulers.threadPoolForIO()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Result>() {

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Result args) {
                setContentEmpty(false);
                setContentShown(true);
                postResult(args);
            }
        });
    }

    int getFirstVisiblePosition() {
        return gridView.getFirstPosition();
    }

    @Override
    protected void onRetryButtonClick() {
        makeRequest();
    }

    @Override
    public void onDestroyView() {
        firstVisiblePosition = getFirstVisiblePosition();
        super.onDestroyView();
    }

    protected void postResult(Result result) {
        this.result = result;
        gridView.setAdapter(new NewsFeedAdapter(getActivity(), result.response));
    }
}
