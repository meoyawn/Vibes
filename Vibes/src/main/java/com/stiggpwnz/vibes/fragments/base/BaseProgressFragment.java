package com.stiggpwnz.vibes.fragments.base;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import butterknife.Views;

import com.devspark.progressfragment.SherlockProgressFragment;
import com.devspark.robototextview.widget.RobotoButton;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.util.BusProvider;
import com.stiggpwnz.vibes.util.Log;
import com.stiggpwnz.vibes.util.Persistance;
import com.stiggpwnz.vibes.vk.models.Result;

public abstract class BaseProgressFragment extends SherlockProgressFragment implements FragmentInterface {

	private final OnClickListener onRetryClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			onRetryClick();
		}
	};

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

	public abstract class VKCallback<T extends Result> implements Callback<T> {

		private final Runnable runnable;
		private boolean isCancelled;

		public VKCallback(Runnable runnable) {
			this.runnable = runnable;
		}

		protected abstract void onSuccess(T object, Response response);

		protected void onFailure(RetrofitError error) {

		}

		@Override
		public void failure(RetrofitError arg0) {
			Log.e(arg0);
			if (getSherlockActivity() == null || isCancelled) {
				return;
			}

			setContentEmpty(true);
			onFailure(arg0);
			setContentShown(true);
		}

		@Override
		public void success(T arg0, Response arg1) {
			if (getSherlockActivity() == null || isCancelled) {
				return;
			}

			if (arg0.isResponse()) {
				setContentEmpty(false);
				onSuccess(arg0, arg1);
				setContentShown(true);
			} else if (arg0.isAuthError()) {
				Persistance.resetAuth();
				runnable.run();
			} else {
				failure(null);
			}
		}

		public void cancel() {
			isCancelled = true;
		}
	}
}
