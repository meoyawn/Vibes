package com.stiggpwnz.vibes.fragments.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.stiggpwnz.vibes.util.Injector;

import butterknife.ButterKnife;

public class BaseFragment extends Fragment {

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
    public void onDestroyView() {
        ButterKnife.reset(this);
        super.onDestroyView();
    }
}
