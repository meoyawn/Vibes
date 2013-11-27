package com.stiggpwnz.vibes.fragments.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.squareup.otto.Bus;
import com.stiggpwnz.vibes.util.Injector;

import javax.inject.Inject;

import butterknife.ButterKnife;
import dagger.Lazy;

public class BaseFragment extends Fragment {

    @Inject Lazy<Bus> bus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
    }

    @Override
    public void onResume() {
        super.onResume();
        bus.get().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        bus.get().unregister(this);
    }

    @Override
    public void onDestroyView() {
        ButterKnife.reset(this);
        super.onDestroyView();
    }
}
