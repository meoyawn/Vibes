package com.stiggpwnz.vibes;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.tester.android.view.TestMenuItem;
import org.robolectric.util.ActivityController;

import static org.junit.Assert.assertTrue;

/**
 * Created by adelnizamutdinov on 03/03/2014
 */
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {
    ActivityController<MainActivity> activityController;

    @Before public void setUp() throws Exception {
        activityController = Robolectric.buildActivity(MainActivity.class);
    }

    @Test public void testOnOptionsItemSelected() throws Exception {
        MainActivity mainActivity = activityController.create().get();
        FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, new Fragment())
                .addToBackStack(null)
                .commit();
        int oldStack = fragmentManager.getBackStackEntryCount();

        assertTrue(mainActivity.onOptionsItemSelected(new TestMenuItem(android.R.id.home)));
        assertTrue(fragmentManager.getBackStackEntryCount() < oldStack);
    }
}
