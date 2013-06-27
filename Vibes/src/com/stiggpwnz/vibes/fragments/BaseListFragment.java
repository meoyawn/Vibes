package com.stiggpwnz.vibes.fragments;

import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.app.SherlockListFragment;
import com.stiggpwnz.vibes.Singletons;

public abstract class BaseListFragment extends SherlockListFragment {

	private boolean created;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		Singletons.OTTO.register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Singletons.OTTO.unregister(this);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (created) {
			return;
		}
		onCreate(view);
		created = true;
	}

	protected abstract void onCreate(View view);
}
