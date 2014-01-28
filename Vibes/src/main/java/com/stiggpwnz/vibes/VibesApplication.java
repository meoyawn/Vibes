package com.stiggpwnz.vibes;

import android.app.Application;
import android.content.Context;

import com.stiggpwnz.vibes.util.CrashReportingTree;

import java.util.ArrayList;

import dagger.ObjectGraph;
import timber.log.Timber;

import static timber.log.Timber.DebugTree;

public class VibesApplication extends Application {

    public static VibesApplication from(Context context) {
        return (VibesApplication) context.getApplicationContext();
    }

    ObjectGraph objectGraph;

    @Override
    public void onCreate() {
        super.onCreate();
        objectGraph = ObjectGraph.create(getModules().toArray());
        Timber.plant(BuildConfig.DEBUG ? new DebugTree() : new CrashReportingTree());
    }

    protected ArrayList<Object> getModules() {
        ArrayList<Object> modules = new ArrayList<>(1);
        modules.add(new VibesModule(this));
        return modules;
    }

    public ObjectGraph getObjectGraph() { return objectGraph; }
}
