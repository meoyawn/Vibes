package com.stiggpwnz.vibes.util;

import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Created by adel on 1/31/14
 */
public final class Utils {

    private Utils() {}

    public static void waitForMeasure(final View view, final OnMeasuredCallback callback) {
        int width = view.getWidth();
        int height = view.getHeight();

        if (width > 0 && height > 0) {
            callback.onMeasured(view, width, height);
            return;
        }

        final ViewTreeObserver observer = view.getViewTreeObserver();
        if (observer != null) {
            observer.addOnPreDrawListener(() -> {
                if (observer.isAlive()) {
                    observer.removeOnPreDrawListener(this);
                }

                callback.onMeasured(view, view.getWidth(), view.getHeight());

                return true;
            });
        }
    }

    public interface OnMeasuredCallback {
        void onMeasured(View view, int width, int height);
    }
}