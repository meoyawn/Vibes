package com.stiggpwnz.vibes.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.stiggpwnz.vibes.dagger.Dagger;

import butterknife.ButterKnife;

public class BaseFragment extends Fragment {
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dagger.inject(this);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
    }

    @Override public void onDestroyView() {
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    public boolean onBackPressed() { return false; }
}