package com.stiggpwnz.vibes.activities.base;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.squareup.otto.Bus;
import com.stiggpwnz.vibes.util.Injector;

import javax.inject.Inject;

import butterknife.ButterKnife;
import dagger.Lazy;
import de.keyboardsurfer.android.widget.crouton.Crouton;

public abstract class BaseActivity extends FragmentActivity {

    @Inject Lazy<Bus> busLazy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
    }

    @Override
    public void setContentView(int layoutResId) {
        super.setContentView(layoutResId);
        ButterKnife.inject(this);
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
    protected void onDestroy() {
        super.onDestroy();
        Crouton.cancelAllCroutons();
    }
}
