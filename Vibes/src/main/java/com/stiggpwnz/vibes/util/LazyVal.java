package com.stiggpwnz.vibes.util;

/**
 * Created by adel on 1/28/14
 */
public abstract class LazyVal<T> {

    private T mInstance;

    protected abstract T create();

    public final T get() {
        synchronized (this) {
            if (mInstance == null) {
                mInstance = create();
            }
            return mInstance;
        }
    }
}