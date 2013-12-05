package com.stiggpwnz.vibes.fragments.base;

import android.os.Bundle;
import android.view.View;

import com.devspark.progressfragment.ProgressFragment;
import com.squareup.otto.Bus;
import com.stiggpwnz.vibes.util.Injector;

import javax.inject.Inject;

import butterknife.ButterKnife;
import dagger.Lazy;

public abstract class BaseProgressFragment extends ProgressFragment {

    @Inject Lazy<Bus> busLazy;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
    }

    @Override
    public void setContentView(int layoutResId) {
        super.setContentView(layoutResId);
        ButterKnife.inject(this, getView());
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        ButterKnife.inject(this, getView());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        busLazy.get().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        busLazy.get().unregister(this);
    }

    @Override
    public void onDestroyView() {
        ButterKnife.reset(this);
        super.onDestroyView();
    }
}
