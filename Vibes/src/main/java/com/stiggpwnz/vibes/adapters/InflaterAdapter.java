package com.stiggpwnz.vibes.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;

public abstract class InflaterAdapter extends BaseAdapter {

    protected final LayoutInflater inflater;

    public InflaterAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public static void setVisibility(View view, int visibility) {
        if (view.getVisibility() != visibility) {
            view.setVisibility(visibility);
        }
    }
}
