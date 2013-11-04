package com.stiggpwnz.vibes.test.base;

import android.os.Bundle;

public abstract class HomeAsUpActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
