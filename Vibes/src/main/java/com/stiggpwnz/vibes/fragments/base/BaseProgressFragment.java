package com.stiggpwnz.vibes.fragments.base;

import android.os.Bundle;

import com.devspark.progressfragment.ProgressFragment;
import com.squareup.otto.Bus;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.util.Injector;

import javax.inject.Inject;

import butterknife.OnClick;
import butterknife.Views;
import dagger.Lazy;

public abstract class BaseProgressFragment extends ProgressFragment {

    @Inject Lazy<Bus> busLazy;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setContentView(getLayoutResId());
        Views.inject(this, getView());
    }

    protected abstract int getLayoutResId();

    @OnClick(R.id.retry_button)
    void retry() {
        onRetryButtonClick();
    }

    protected abstract void onRetryButtonClick();

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
        Views.reset(this);
        super.onDestroyView();
    }
}
