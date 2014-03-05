package com.stiggpwnz.vibes.util;

/**
 * Created by adel on 1/31/14
 */
public interface Injector {
    <T> T get(Class<? extends T> type);

    <T> void inject(T instance);
}