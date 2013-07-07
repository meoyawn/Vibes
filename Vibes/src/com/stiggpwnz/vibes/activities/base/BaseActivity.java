package com.stiggpwnz.vibes.activities.base;

import android.os.Bundle;
import butterknife.Views;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.stiggpwnz.vibes.util.BusProvider;

public abstract class BaseActivity extends SherlockFragmentActivity implements RetainedActivityInterface {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		onCreateView(savedInstanceState);
		Views.inject(this);
		onViewCreated(savedInstanceState);
		if (savedInstanceState == null) {
			onFirstCreated(null);
		} else {
			onRecreated(savedInstanceState);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		BusProvider.register(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		BusProvider.unregister(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Views.reset(this);
	}
}
