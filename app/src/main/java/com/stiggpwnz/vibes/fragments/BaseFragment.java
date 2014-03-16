package com.stiggpwnz.vibes.fragments;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import org.jetbrains.annotations.NotNull;

import butterknife.ButterKnife;

public abstract class BaseFragment extends Fragment {
    protected void configure(@NotNull ActionBar actionBar) { }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getActionBar() != null) {
            configure(getActivity().getActionBar());
        }
    }

    @Override public void onDestroyView() {
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    public boolean onBackPressed() { return false; }
}
