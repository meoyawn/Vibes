package com.stiggpwnz.vibes.fragments;

import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.app.SherlockListFragment;
import com.stiggpwnz.vibes.util.BusProvider;

public abstract class VibesListFragment extends SherlockListFragment implements VibesFragmentInterface {

	private boolean created;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (created) {
			return;
		}
		onFirstCreated(view);
		created = true;
	}

	@Override
	public void onResume() {
		super.onResume();
		BusProvider.register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		BusProvider.unregister(this);
	}

	@Override
	public void runOnUiThread(Runnable runnable) {
		if (getSherlockActivity() != null) {
			getSherlockActivity().runOnUiThread(runnable);
		}
	}

	@Override
	public void runOnBackgroundThread(Runnable runnable) {
		new Thread(runnable).start();
	}
}
