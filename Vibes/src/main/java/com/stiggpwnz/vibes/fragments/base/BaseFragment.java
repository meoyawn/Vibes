package com.stiggpwnz.vibes.fragments.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.squareup.otto.Bus;
import com.stiggpwnz.vibes.VibesApplication;

import javax.inject.Inject;

import butterknife.Views;
import dagger.Lazy;
import icepick.bundle.Bundles;

public class BaseFragment extends Fragment {

    @Inject Lazy<Bus> bus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VibesApplication.from(getActivity()).inject(this);
        Bundles.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Views.inject(this, view);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundles.saveInstanceState(this, outState);
    }

    @Override
    public void onDestroyView() {
        Views.reset(this);
        super.onDestroyView();
    }
}
