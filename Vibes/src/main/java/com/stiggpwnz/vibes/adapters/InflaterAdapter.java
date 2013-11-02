package com.stiggpwnz.vibes.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

public abstract class InflaterAdapter extends BaseAdapter {

    protected final LayoutInflater inflater;

    public InflaterAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }
}
