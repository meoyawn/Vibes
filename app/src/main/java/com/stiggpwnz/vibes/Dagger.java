package com.stiggpwnz.vibes;

import android.content.Context;
import android.view.View;

import dagger.ObjectGraph;

/**
 * Created by adel on 16/03/14
 */
public class Dagger {
    public static ObjectGraph getObjectGraph(Context context) {
        return ((Injector) context).getObjectGraph();
    }

    public static void inject(Context context, Object object) {
        getObjectGraph(context).inject(object);
    }

    public static void inject(Context context) { inject(context, context); }

    public static void inject(View view) { inject(view.getContext(), view); }

    public static void inject(android.app.Fragment fragment) {
        inject(fragment.getActivity(), fragment);
    }

    public static void inject(android.support.v4.app.Fragment fragment) {
        inject(fragment.getActivity(), fragment);
    }
}
