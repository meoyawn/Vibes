package com.stiggpwnz.vibes.fragments.base;

import rx.Observer;

/**
 * Created by adel on 11/17/13
 */
public abstract class FragmentObserver<T> implements Observer<T> {

    final BaseProgressFragment progressFragment;

    public FragmentObserver(BaseProgressFragment progressFragment) {
        this.progressFragment = progressFragment;
    }

    public abstract void safeOnNext(T t);

    public abstract void safeOnError(Throwable throwable);

    @Override
    public void onNext(T args) {
        if (progressFragment.getView() != null) {
            progressFragment.setContentEmpty(false);
            progressFragment.setContentShown(true);
            safeOnNext(args);
        }
    }

    @Override
    public void onError(Throwable e) {
        if (progressFragment.getView() != null) {
            progressFragment.setContentEmpty(true);
            progressFragment.setContentShown(true);
            safeOnError(e);
        }
    }

    @Override
    public void onCompleted() {

    }
}
