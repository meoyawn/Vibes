package com.stiggpwnz.vibes.util;

import timber.log.Timber;

/**
 * A tree which logs important information for crash reporting.
 */
public class CrashReportingTree extends Timber.HollowTree {

    @Override
    public void i(String message, Object... args) {
        // TODO e.g., Crashlytics.log(String.format(message, args));
    }

    @Override
    public void i(Throwable t, String message, Object... args) {
        i(message, args); // Just add to the log.
    }

    @Override
    public void e(String message, Object... args) {
        i("ERROR: " + message, args); // Just add to the log.
    }

    @Override
    public void e(Throwable t, String message, Object... args) {
        e(message, args);
        // TODO e.g., Crashlytics.logException(t);
    }
}
