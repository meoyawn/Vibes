package com.stiggpwnz.vibes.adapters;

import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(suppressConstructorProperties = true)
public abstract class InflaterAdapter extends BaseAdapter {
    protected final LayoutInflater inflater;

    @Override
    public long getItemId(int position) { return position; }
}
