package com.stiggpwnz.vibes.fragments;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stiggpwnz.vibes.R;

import org.jetbrains.annotations.NotNull;

import butterknife.InjectView;

/**
 * Created by adelnizamutdinov on 03/03/2014
 */
public class MainFragment extends BaseFragment {
    @InjectView(R.id.drawer_layout) DrawerLayout drawerLayout;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override protected void configure(@NotNull ActionBar actionBar) {
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.app_name);
    }

    @Override protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getChildFragmentManager().findFragmentById(R.id.left_drawer) == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.left_drawer, new NavigationFragment())
                    .replace(R.id.content_frame, FeedFragmentBuilder.newFeedFragment(0))
                    .commit();
        }
    }

    @Override public boolean onBackPressed() {
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStack();
            return true;
        }
        return super.onBackPressed();
    }
}
