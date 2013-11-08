package com.stiggpwnz.vibes.activities.base;

import android.os.Bundle;

public abstract class HomeAsUpActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
