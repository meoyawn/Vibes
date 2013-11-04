package com.stiggpwnz.vibes.fragments.base;

import android.os.Bundle;

import com.devspark.progressfragment.ProgressFragment;
import com.squareup.otto.Bus;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.Vibes;

import javax.inject.Inject;

import butterknife.OnClick;
import butterknife.Views;
import dagger.Lazy;
import rx.Observer;

import static icepick.bundle.Bundles.restoreInstanceState;
import static icepick.bundle.Bundles.saveInstanceState;

public abstract class BaseProgressFragment extends ProgressFragment {

    @Inject Lazy<Bus> busLazy;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Vibes.from(getActivity()).inject(this);
        restoreInstanceState(this, savedInstanceState);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onCreateView(savedInstanceState);
        Views.inject(this, getView());
        onViewCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveInstanceState(this, outState);
    }

    protected abstract void onCreateView(Bundle savedInstanceState);

    protected abstract void onViewCreated(Bundle savedInstanceState);

    @OnClick(R.id.retry_button)
    void retry() {
        onRetryButtonClick();
    }

    protected abstract void onRetryButtonClick();


    @Override
    public void onResume() {
        super.onResume();
        busLazy.get().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        busLazy.get().unregister(this);
    }

    @Override
    public void onDestroyView() {
        Views.reset(this);
        super.onDestroyView();
    }

    protected abstract class SafeObserver<T> implements Observer<T> {

        public abstract void safeOnNext(T t);

        public abstract void safeOnError(Throwable throwable);

        @Override
        public void onNext(T args) {
            if (getView() != null) {
                setContentEmpty(false);
                setContentShown(true);
                safeOnNext(args);
            }
        }

        @Override
        public void onError(Throwable e) {
            if (getView() != null) {
                setContentEmpty(true);
                setContentShown(true);
                safeOnError(e);
            }
        }

        @Override
        public void onCompleted() {

        }
    }
}
