package com.stiggpwnz.vibes.fragments.base;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;

import com.roadtrippers.RoadTrippersApp;
import com.squareup.otto.Bus;
import com.stiggpwnz.vibes.Vibes;

import javax.inject.Inject;

import butterknife.Views;
import dagger.Lazy;

public class BaseDialogFragment extends DialogFragment {

    @Inject Lazy<Bus> bus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Vibes.from(getActivity()).inject(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Views.inject(this, view);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
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
        Views.reset(this);
        super.onDestroyView();
    }
}
