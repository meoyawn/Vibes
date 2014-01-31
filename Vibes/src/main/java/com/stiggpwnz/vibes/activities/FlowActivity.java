package com.stiggpwnz.vibes.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.HttpResponseCache;
import com.squareup.okhttp.OkHttpClient;
import com.stiggpwnz.vibes.App;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.util.DiskUtils;
import com.stiggpwnz.vibes.util.JacksonParcer;
import com.stiggpwnz.vibes.util.JacksonSerializer;
import com.stiggpwnz.vibes.util.Persistence;
import com.stiggpwnz.vibes.util.ScopedContext;
import com.stiggpwnz.vibes.views.ContainerView;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import butterknife.ButterKnife;
import butterknife.InjectView;
import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import de.devland.esperandro.Esperandro;
import flow.Backstack;
import flow.Flow;
import flow.HasParent;
import flow.Layouts;
import flow.Parcer;
import timber.log.Timber;

/**
 * Created by adel on 1/31/14
 */
public class FlowActivity extends Activity implements Flow.Listener {

    static final String BUNDLE_BACKSTACK = "bundle backstack";

    @Inject CookieManager  cookieManager;
    @Inject Parcer<Object> parcer;

    Flow        flow;
    ObjectGraph activityGraph;

    @InjectView(R.id.container) ContainerView containerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activityGraph = ObjectGraph.create(new ActivityModule());
        activityGraph.inject(this);

        flow = new Flow(getInitialBackstack(savedInstanceState), this);
        go(flow.getBackstack(), Flow.Direction.FORWARD);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.inject(this);
    }

    private Backstack getInitialBackstack(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            return Backstack.from(savedInstanceState.getParcelable(BUNDLE_BACKSTACK), parcer);
        } else {
            if (cookieManager.getCookie("vk.com") == null) {
                return Backstack.single(new App.LoginScreen());
            } else {
                return Backstack.single(new App.NavigationScreen());
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_BACKSTACK, flow.getBackstack().getParcelable(parcer));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            return flow.goUp();
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onBackPressed() {
        if (!flow.goBack()) {
            finish();
        }
    }

    @Override
    public void go(Backstack backstack, Flow.Direction direction) {
        Object screen = backstack.current().getScreen();
        containerView.displayView(getView(screen), direction);

        setTitle(screen.getClass().getSimpleName());

        ActionBar actionBar = getActionBar();
        boolean hasUp = screen instanceof HasParent;
        actionBar.setDisplayHomeAsUpEnabled(hasUp);
        actionBar.setHomeButtonEnabled(hasUp);

        invalidateOptionsMenu();
    }

    View getView(Object screen) {
        ObjectGraph graph = activityGraph.plus(screen);
        Context scopedContext = new ScopedContext(this, graph);
        return Layouts.createView(scopedContext, screen);
    }

    @Module(injects = FlowActivity.class, library = true)
    public class ActivityModule {

        @Provides
        @App
        Flow provideAppFlow() { return flow; }

        @Provides
        @Singleton
        ObjectMapper provideObjectMapper() { return new ObjectMapper(); }

        @Provides
        @Singleton
        Parcer<Object> provideParcer(ObjectMapper objectMapper) {
            return new JacksonParcer<>(objectMapper);
        }

        @Provides
        @Singleton
        Persistence providePersistence(JacksonSerializer jacksonSerializer) {
            Esperandro.setSerializer(jacksonSerializer);
            return Esperandro.getPreferences(Persistence.class, FlowActivity.this);
        }

        @Provides
        @Singleton
        OkHttpClient provideHttpClient() {
            OkHttpClient okHttpClient = new OkHttpClient();
            try {
                int maxSize = 20 * 1024 * 1024; // 20 MB
                okHttpClient.setResponseCache(new HttpResponseCache(DiskUtils.cacheDirNamed(FlowActivity.this, "http"), maxSize));
            } catch (IOException e) {
                Timber.e(e, "Failed to create a cache folder for OkHttpClient");
            }
            return okHttpClient;
        }

        @Provides
        @Singleton
        CookieSyncManager provideCookieSyncManager() {
            return CookieSyncManager.createInstance(FlowActivity.this);
        }

        @Provides
        CookieManager provideCookieManager(@SuppressWarnings("UnusedParameters") CookieSyncManager cookieSyncManager) {
            return CookieManager.getInstance();
        }
    }
}
