package com.stiggpwnz.vibes.util;

import android.content.Context;
import android.view.View;

import com.stiggpwnz.vibes.VibesApplication;

/**
 * Created by adel on 11/17/13
 */
public class Injector {

    private static VibesApplication from(Context context) {
        return (VibesApplication) context.getApplicationContext();
    }

    public static void inject(Context context) {
        from(context).getObjectGraph().inject(context);
    }

    public static void inject(android.support.v4.app.Fragment fragment) {
        from(fragment.getActivity()).getObjectGraph().inject(fragment);
    }

    public static void inject(android.app.Fragment fragment) {
        from(fragment.getActivity()).getObjectGraph().inject(fragment);
    }

    public static void inject(View view) {
        from(view.getContext()).getObjectGraph().inject(view);
    }

    public static void inject(Context context, Object object) {
        from(context).getObjectGraph().inject(object);
    }
}
