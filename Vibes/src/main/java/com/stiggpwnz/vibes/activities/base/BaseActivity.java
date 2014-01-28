package com.stiggpwnz.vibes.activities.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.stiggpwnz.vibes.util.Dagger;

import butterknife.ButterKnife;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public abstract class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dagger.inject(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
        CalligraphyConfig.initDefault("fonts/Roboto-Regular.ttf");
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
