package com.stiggpwnz.vibes.activities.base;

import android.os.Bundle;

import com.actionbarsherlock.view.MenuItem;

public abstract class HomeAsUpActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	};
}
