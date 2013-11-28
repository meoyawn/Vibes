package com.stiggpwnz.vibes.activities;

import android.webkit.CookieManager;

import com.stiggpwnz.vibes.TestVibesApplication;
import com.stiggpwnz.vibes.fragments.LoginFragment;
import com.stiggpwnz.vibes.test.RobolectricGradleTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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

    @Before
    public void setUp() {
        TestVibesApplication.get().getObjectGraph().inject(this);
    }

    @Test
    public void testLoggedOut() throws Exception {
        when(cookieManager.getCookie("vk.com")).thenReturn(null);

        MainActivity mainActivity = buildActivity(MainActivity.class).create().get();
        assertTrue(mainActivity.getSupportFragmentManager().findFragmentById(android.R.id.content) instanceof LoginFragment);
    }
}
