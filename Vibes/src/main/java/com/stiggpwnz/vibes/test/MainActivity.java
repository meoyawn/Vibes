package com.stiggpwnz.vibes.test;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.test.base.HomeAsUpActivity;
import com.stiggpwnz.vibes.fragments.FeedFragment;
import com.stiggpwnz.vibes.fragments.NavigationFragment;

public class MainActivity extends HomeAsUpActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CookieSyncManager.createInstance(this);
        if (CookieManager.getInstance().getCookie("vk.com") == null) {
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
