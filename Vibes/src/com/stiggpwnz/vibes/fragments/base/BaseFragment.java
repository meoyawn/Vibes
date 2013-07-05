package com.stiggpwnz.vibes.fragments.base;

import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import butterknife.Views;

import com.actionbarsherlock.app.SherlockFragment;
import com.stiggpwnz.vibes.util.BusProvider;

public class BaseFragment extends SherlockFragment implements FragmentInterface {

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Views.inject(this, view);
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
	public void onDestroyView() {
		super.onDestroyView();
		Views.reset(this);
	}

	@Override
	public void runOnUiThread(Runnable runnable) {
		if (getSherlockActivity() != null) {
			getSherlockActivity().runOnUiThread(runnable);
		}
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
