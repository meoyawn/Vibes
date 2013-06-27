package com.stiggpwnz.vibes.fragments;

import android.os.Bundle;

import com.devspark.progressfragment.ProgressSherlockFragment;
import com.stiggpwnz.vibes.util.BusProvider;

public abstract class VibesProgressFragment extends ProgressSherlockFragment implements VibesFragmentInterface {

	private boolean created;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (created) {
			return;
		}
		onFirstCreated(getView());
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
