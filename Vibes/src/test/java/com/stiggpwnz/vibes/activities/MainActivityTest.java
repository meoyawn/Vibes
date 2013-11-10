package com.stiggpwnz.vibes.activities;

import android.webkit.CookieManager;

import com.stiggpwnz.vibes.test.RobolectricGradleTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static com.stiggpwnz.vibes.TestVibesApplication.injectMocks;
import static org.junit.Assert.assertEquals;
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
        injectMocks(this);
    }

    @Test
    public void testInjection() throws Exception {
        assertEquals(cookieManager,
                buildActivity(MainActivity.class).create().get().cookieManager);
    }

    @Test
    public void testLoggedOut() throws Exception {
        when(cookieManager.getCookie("vk.com")).thenReturn(null);
        assertTrue(buildActivity(MainActivity.class).create().get().isFinishing());
    }
}
