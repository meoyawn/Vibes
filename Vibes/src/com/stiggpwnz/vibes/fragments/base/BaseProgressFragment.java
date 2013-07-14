package com.stiggpwnz.vibes.fragments.base;

import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import butterknife.Views;

import com.devspark.progressfragment.SherlockProgressFragment;
import com.devspark.robototextview.widget.RobotoButton;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.util.BusProvider;

public abstract class BaseProgressFragment extends SherlockProgressFragment implements FragmentInterface {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.progress_fragment, container, false);
	}

	private final OnClickListener onRetryClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			onRetryClick();
		}
	};

	protected void onError() {
		setContentEmpty(true);
		setContentShown(true);
	}

	protected abstract void onRetryClick();

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		RobotoButton retryButton = Views.findById(view, R.id.retry_button);
		retryButton.setOnClickListener(onRetryClick);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		onCreateView(savedInstanceState);
		Views.inject(this, getView());
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

	protected final Runnable showRetry = new Runnable() {

		@Override
		public void run() {
			setContentEmpty(true);
			setContentShown(true);
		}
	};

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
