package com.stiggpwnz.vibes.fragments;

import android.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stiggpwnz.vibes.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.ANDROID.assertThat;

/**
 * Created by adelnizamutdinov on 05/03/2014
 */
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class MainFragmentTest {
    FragmentActivity activity;
    MainFragment     mainFragment;

    @Before public void setUp() throws Exception {
        activity = Robolectric.buildActivity(FragmentActivity.class)
                .create()
                .get();
        mainFragment = new MainFragment();
        activity.getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, mainFragment)
                .commit();
    }

    @Test public void testOnCreateView() throws Exception {
        LayoutInflater inflater = LayoutInflater.from(activity);
        ViewGroup viewGroup = (ViewGroup) activity.findViewById(android.R.id.content);
        View view = mainFragment.onCreateView(inflater, viewGroup, null);
        assertThat(activity.getActionBar())
                .hasTitle(activity.getString(R.string.app_name))
                .hasDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE |
                        ActionBar.DISPLAY_SHOW_HOME |
                        ActionBar.DISPLAY_USE_LOGO);
    }

    @Test public void testOnViewCreated() {
        assertThat(mainFragment.getFragmentManager().findFragmentById(R.id.left_drawer))
                .isInstanceOf(DrawerLayout.class);
        assertThat(mainFragment.getFragmentManager().findFragmentById(R.id.content_frame))
                .isInstanceOf(FeedFragment.class);
    }
}
