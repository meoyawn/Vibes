package com.stiggpwnz.vibes.util;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Views;

import com.roadtrippers.R;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;

public class RetryCrouton {

	public static interface Listener {
		public void onRetry();
	}

	@InjectView(R.id.textErrorMessage) TextView errorMessage;

	private final Listener listener;
	private final Crouton crouton;

	private View customView;

	public RetryCrouton(Activity context, ViewGroup container, Listener listener) {
		customView = LayoutInflater.from(context).inflate(R.layout.retry, container, false);
		Views.inject(this, customView);

		crouton = Crouton.make(context, customView, container);
		crouton.setConfiguration(new Configuration.Builder().setDuration(Configuration.DURATION_INFINITE).build());

		this.listener = listener;
	}

	public View getCustomView() {
		return customView;
	}

	public RetryCrouton setErrorMessage(CharSequence errorMessage) {
		this.errorMessage.setText(errorMessage);
		return this;
	}

	public void show() {
		crouton.show();
	}

	@OnClick(R.id.buttonRetry)
	public void retry() {
		listener.onRetry();
	}

	public void remove() {
		crouton.cancel();
	}
}
