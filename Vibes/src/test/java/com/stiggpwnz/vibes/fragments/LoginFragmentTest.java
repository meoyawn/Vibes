package com.stiggpwnz.vibes.fragments;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.webkit.WebView;

import com.stiggpwnz.vibes.test.RobolectricGradleTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by adelnizamutdinov on 05/12/2013
 */
@RunWith(RobolectricGradleTestRunner.class)
public class LoginFragmentTest {

    LoginFragment loginFragment;

    @Before
    public void setUp() {
        loginFragment = new LoginFragment();
        Robolectric.buildActivity(FragmentActivity.class)
                .create()
                .start()
                .resume()
                .get()
                .getSupportFragmentManager()
                .beginTransaction()
                .add(loginFragment, null)
                .commit();
    }

    @Test
    public void testInjection() {
        assertNotNull(loginFragment.cookieManagerLazy);
    }

    @Test
    public void testOnActivityCreated() {
        View view = loginFragment.getContentView();
        assertTrue(view instanceof WebView);
    }
}
