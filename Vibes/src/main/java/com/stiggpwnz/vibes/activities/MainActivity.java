package com.stiggpwnz.vibes.activities;

import android.content.Intent;
import android.os.Bundle;

import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.activities.base.HomeAsUpActivity;
import com.stiggpwnz.vibes.fragments.NavigationFragment;
import com.stiggpwnz.vibes.fragments.NewsFeedFragment;
import com.stiggpwnz.vibes.util.Persistence;

import javax.inject.Inject;

import dagger.Lazy;

public class MainActivity extends HomeAsUpActivity {

    @Inject Lazy<Persistence> persistenceLazy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (persistenceLazy.get().getCookie() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.main_root);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.left_drawer, new NavigationFragment())
                    .add(R.id.content_frame, new NewsFeedFragment())
                    .commit();
        }
    }
}
