package com.stiggpwnz.vibes.fragments.base;

import android.os.Bundle;

/**
 * Created by stiggpwnz on 9/10/13
 */
public abstract class RetainedMapFragment extends BaseMapFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
}
