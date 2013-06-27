package com.stiggpwnz.vibes.fragments;

import android.os.Bundle;

import com.devspark.progressfragment.ProgressSherlockFragment;
import com.stiggpwnz.vibes.Singletons;

public abstract class BaseProgressFragment extends ProgressSherlockFragment implements RetainedFragment {

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
		Singletons.OTTO.register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Singletons.OTTO.unregister(this);
	}

	protected void runOnUiThread(Runnable runnable) {
		if (getSherlockActivity() != null) {
			getSherlockActivity().runOnUiThread(runnable);
		}
	}

	protected void runOnBackgroundThread(Runnable runnable) {
		new Thread(runnable).start();
	}
}
