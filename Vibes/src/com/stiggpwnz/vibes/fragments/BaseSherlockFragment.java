package com.stiggpwnz.vibes.fragments;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragment;
import com.stiggpwnz.vibes.Singletons;

public class BaseSherlockFragment extends SherlockFragment {

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
}
