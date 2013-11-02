package com.stiggpwnz.vibes;

import android.app.Application;
import android.app.Service;
import android.content.Context;

import com.stiggpwnz.vibes.util.CrashReportingTree;

import dagger.ObjectGraph;
import timber.log.Timber;

public class Vibes extends Application {

    public static Vibes from(Context context) {
        return (Vibes) context.getApplicationContext();
    }

    ObjectGraph objectGraph;

    @Override
    public void onCreate() {
        super.onCreate();

        // Dependency tree
        objectGraph = ObjectGraph.create(new DependenciesModule(this));
        Timber.plant(BuildConfig.DEBUG ? new Timber.DebugTree() : new CrashReportingTree());
    }

    public <T> T inject(T object) {
        return objectGraph.inject(object);
    }
}
