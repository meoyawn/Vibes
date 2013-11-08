package com.stiggpwnz.vibes.activities.base;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.squareup.otto.Bus;
import com.stiggpwnz.vibes.VibesApplication;

import javax.inject.Inject;

import butterknife.Views;
import dagger.Lazy;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import icepick.bundle.Bundles;

public abstract class BaseActivity extends FragmentActivity {

    @Inject Lazy<Bus> busLazy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VibesApplication.from(this).inject(this);
        Bundles.restoreInstanceState(this, savedInstanceState);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void setContentView(int layoutResId) {
        super.setContentView(layoutResId);
        Views.inject(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundles.saveInstanceState(this, outState);
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
