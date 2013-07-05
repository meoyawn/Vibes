package com.stiggpwnz.vibes.fragments.base;

import android.os.Bundle;
import android.os.Looper;
import butterknife.Views;

import com.devspark.progressfragment.ProgressSherlockFragment;
import com.stiggpwnz.vibes.util.BusProvider;

public abstract class BaseProgressFragment extends ProgressSherlockFragment implements FragmentInterface {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		onCreateView(savedInstanceState);
		Views.inject(this, getView());
		setContentShownNoAnimation(true);
		onViewCreated(savedInstanceState);
	}

	protected abstract void onCreateView(Bundle savedInstanceState);

	protected abstract void onViewCreated(Bundle savedInstanceState);

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
	public void onDestroyView() {
		super.onDestroyView();
		Views.reset(this);
	}

	@Override
	public void runOnUiThread(Runnable runnable) {
		if (runnable == null || getSherlockActivity() == null) {
			return;
		}

		getSherlockActivity().runOnUiThread(runnable);
	}

	@Override
	public void runOnBackgroundThread(Runnable runnable) {
		if (runnable == null) {
			return;
		}

		if (Looper.myLooper() == Looper.getMainLooper()) {
			new Thread(runnable).start();
		} else {
			runnable.run();
		}
	}

}
