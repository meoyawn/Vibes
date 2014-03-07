package com.stiggpwnz.vibes;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.webkit.CookieManager;

import com.stiggpwnz.vibes.dagger.Dagger;
import com.stiggpwnz.vibes.dagger.UiScopeDaggerModule;
import com.github.mttkay.memento.Memento;
import com.github.mttkay.memento.MementoCallbacks;
import com.github.mttkay.memento.Retain;
import com.stiggpwnz.vibes.fragments.LoginFragment;
import com.stiggpwnz.vibes.fragments.MainFragment;

import javax.inject.Inject;

import dagger.ObjectGraph;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import lombok.Getter;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends FragmentActivity implements MementoCallbacks {
    @Retain @Getter ObjectGraph   objectGraph;
    @Inject         CookieManager cookieManager;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Memento.retain(this);
        Dagger.inject(this);
        if (savedInstanceState == null) {
            Fragment fragment = cookieManager.getCookie("vk.com") == null ?
                    new LoginFragment() :
                    new MainFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, fragment)
                    .commit();
        }
    }

    @Override public void onLaunch() {
        objectGraph = Dagger.getAppScope(this).plus(new UiScopeDaggerModule());
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

//    @Override public void onStart() {
//        super.onStart();
//        EasyTracker.getInstance(this).activityStart(this);
//    }
//
//    @Override public void onStop() {
//        super.onStop();
//        EasyTracker.getInstance(this).activityStop(this);
//    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
        CalligraphyConfig.initDefault("fonts/Roboto-Regular.ttf");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Crouton.cancelAllCroutons();
    }
}