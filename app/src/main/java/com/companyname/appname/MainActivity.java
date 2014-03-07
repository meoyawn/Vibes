package com.companyname.appname;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.CookieManager;

import com.companyname.appname.dagger.Dagger;
import com.companyname.appname.dagger.UiScopeDaggerModule;
import com.companyname.appname.fragments.MainFragment;
import com.github.mttkay.memento.Memento;
import com.github.mttkay.memento.MementoCallbacks;
import com.github.mttkay.memento.Retain;
import com.google.analytics.tracking.android.EasyTracker;
import com.stiggpwnz.vibes.fragments.LoginFragment;

import javax.inject.Inject;

import dagger.ObjectGraph;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import lombok.Getter;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends Activity implements MementoCallbacks {
    @Inject         CookieManager cookieManager;
    @Retain @Getter ObjectGraph   objectGraph;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Memento.retain(this);
        if (savedInstanceState == null) {
            Fragment fragment = cookieManager.getCookie("vk.com") == null ?
                    new LoginFragment() :
                    new MainFragment();
            getFragmentManager().beginTransaction()
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

    @Override public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }

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