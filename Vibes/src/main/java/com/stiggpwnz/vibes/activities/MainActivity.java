package com.stiggpwnz.vibes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.CookieManager;

import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.activities.base.HomeAsUpActivity;
import com.stiggpwnz.vibes.fragments.FeedFragment;
import com.stiggpwnz.vibes.fragments.NavigationFragment;

import javax.inject.Inject;

public class MainActivity extends HomeAsUpActivity {

    @Inject CookieManager cookieManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (cookieManager.getCookie("vk.com") == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.main_root);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.left_drawer, new NavigationFragment())
                    .add(R.id.content_frame, FeedFragment.newInstance(0))
                    .commit();
        }
    }
}
