package com.stiggpwnz.vibes.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(suppressConstructorProperties = true)
public abstract class InflaterAdapter extends BaseAdapter {
    protected final LayoutInflater inflater;

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static void setVisibility(View view, int visibility) {
        if (view.getVisibility() != visibility) {
            view.setVisibility(visibility);
        }
    }
}
