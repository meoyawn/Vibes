package com.stiggpwnz.vibes.fragments.base;

import android.os.Bundle;
import android.view.View;

import com.devspark.progressfragment.ProgressFragment;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.util.Injector;

import butterknife.ButterKnife;
import butterknife.OnClick;

public abstract class BaseProgressFragment extends ProgressFragment {

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

    @OnClick(R.id.retry_button)
    void retry() {
        onRetryButtonClick();
    }

    protected abstract void onRetryButtonClick();

    @Override
    public void onDestroyView() {
        ButterKnife.reset(this);
        super.onDestroyView();
    }
}
