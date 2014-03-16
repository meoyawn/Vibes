package com.stiggpwnz.vibes;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.webkit.CookieManager;

import com.github.mttkay.memento.Memento;
import com.github.mttkay.memento.MementoCallbacks;
import com.github.mttkay.memento.Retain;
import com.stiggpwnz.vibes.fragments.BaseFragment;
import com.stiggpwnz.vibes.fragments.LoginFragment;
import com.stiggpwnz.vibes.fragments.MainFragment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import lombok.Getter;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends FragmentActivity implements Injector, MementoCallbacks {
    static {
        CalligraphyConfig.initDefault("fonts/Roboto-Regular.ttf");
    }

    @Inject           CookieManager cookieManager;
    @Nullable @Getter ObjectGraph   objectGraph;
    @Retain @NotNull  ObjectGraph   retainedObjectGraph;

    @Module(addsTo = AppDaggerModule.class,
            overrides = true)
    static class ContextModule {
        final Context context;

        ContextModule(Context context) { this.context = context; }

        @Provides Context provideContext() { return context; }
    }


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Memento.retain(this);
        objectGraph = retainedObjectGraph.plus(new ContextModule(this));
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
        retainedObjectGraph = Dagger.getObjectGraph(getApplication()).plus(new ActivityDaggerModule());
    }

    @Override public void onBackPressed() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof BaseFragment) {
                if (((BaseFragment) fragment).onBackPressed()) {
                    return;
                }
            }
        }
        super.onBackPressed();
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
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
    }

    @Override
    protected void onDestroy() {
        Crouton.cancelAllCroutons();
        super.onDestroy();
    }
}