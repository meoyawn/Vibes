package com.stiggpwnz.vibes;

import android.app.Activity;
import android.app.Application;
import android.app.Service;

import dagger.ObjectGraph;

public class Vibes extends Application {

    ObjectGraph objectGraph;

    public static Vibes from(Activity activity) {
        return (Vibes) activity.getApplication();
    }

    static Vibes from(Service service) {
        return (Vibes) service.getApplication();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        objectGraph = ObjectGraph.create(new DependenciesModule(this));
    }

    public <T> T inject(T object) {
        return objectGraph.inject(object);
    }
}
