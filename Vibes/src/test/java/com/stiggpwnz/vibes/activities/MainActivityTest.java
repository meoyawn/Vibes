package com.stiggpwnz.vibes.activities;

import android.webkit.CookieManager;

import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.fragments.FeedFragment;
import com.stiggpwnz.vibes.fragments.LoginFragment;
import com.stiggpwnz.vibes.test.RobolectricGradleTestRunner;
import com.stiggpwnz.vibes.util.Injector;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.util.ActivityController;

import javax.inject.Inject;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.buildActivity;

/**
 * Created by adel on 11/8/13
 */
@RunWith(RobolectricGradleTestRunner.class)
public class MainActivityTest {

    @Inject CookieManager cookieManager;

    ActivityController<MainActivity> mainActivityActivityController;

    @Before
    public void setUp() {
        Injector.inject(Robolectric.application, this);
        mainActivityActivityController = buildActivity(MainActivity.class);
    }

    @Test
    public void testLoggedOut() throws Exception {
        when(cookieManager.getCookie("vk.com")).thenReturn(null);
        MainActivity mainActivity = mainActivityActivityController.create().start().resume().get();
        assertTrue(mainActivity.getSupportFragmentManager().findFragmentById(android.R.id.content) instanceof LoginFragment);
    }

    @Test
    public void testLoggedIn() {
        when(cookieManager.getCookie("vk.com")).thenReturn("COOOOOOKIE");
        MainActivity mainActivity = mainActivityActivityController.create().start().resume().get();
        assertTrue(mainActivity.getSupportFragmentManager().findFragmentById(R.id.content_frame) instanceof FeedFragment);
    }
}
