package com.stiggpwnz.vibes.activities.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.stiggpwnz.vibes.util.Injector;

import butterknife.ButterKnife;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public abstract class BaseActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }

    @Override
    public void setContentView(int layoutResId) {
        super.setContentView(layoutResId);
        ButterKnife.inject(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Crouton.cancelAllCroutons();
    }
}
