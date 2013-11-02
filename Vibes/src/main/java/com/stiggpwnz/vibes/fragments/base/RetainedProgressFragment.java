package com.stiggpwnz.vibes.fragments.base;

import android.os.Bundle;

public abstract class RetainedProgressFragment extends BaseProgressFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
}
