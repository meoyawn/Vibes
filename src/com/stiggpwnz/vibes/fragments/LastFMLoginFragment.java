package com.stiggpwnz.vibes.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.stiggpwnz.vibes.R;

public class LastFMLoginFragment extends SherlockDialogFragment implements OnClickListener {

	public static interface Listener {

		public String[] lastFmAuth(String username, String password);

		public void saveLastFM(String[] params);
	}

	private EditText editUsername;
	private EditText editPassword;

	private Listener listener;

	public LastFMLoginFragment() {

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		listener = (Listener) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.last_auth, container);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		editUsername = (EditText) view.findViewById(R.id.editUsername);
		editPassword = (EditText) view.findViewById(R.id.editPassword);
		view.findViewById(R.id.btnSingIn).setOnClickListener(this);
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		getDialog().setCanceledOnTouchOutside(true);

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// show keyboard
				editUsername.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
				editUsername.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
			}
		}, 50);

		editPassword.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// login
				if (editUsername.getText().length() > 0 && editPassword.getText().length() > 0)
					new LastFmSignIn().execute();

				// hide keyboard
				InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

				return true;
			}
		});
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	@Override
	public void onClick(View v) {
		if (editUsername.getText().length() > 0 && editPassword.getText().length() > 0)
			new LastFmSignIn().execute();
	}

	private class LastFmSignIn extends AsyncTask<Void, Void, String[]> {

		private ProgressDialog authDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			authDialog = new ProgressDialog(getSherlockActivity());
			authDialog.setMessage(getText(R.string.auth));
			authDialog.setIndeterminate(true);
			authDialog.setCancelable(false);
			authDialog.show();
		}

		@Override
		protected String[] doInBackground(Void... params) {
			Thread.currentThread().setName("Signing in to LastFM");
			String username = editUsername.getText().toString();
			String password = editPassword.getText().toString();
			if (listener != null)
				return listener.lastFmAuth(username, password);
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {
			super.onPostExecute(result);
			authDialog.dismiss();
			if (listener != null) {
				if (result != null) {
					listener.saveLastFM(result);
					dismissAllowingStateLoss();
					Toast.makeText(getSherlockActivity(), R.string.last_success, Toast.LENGTH_SHORT).show();
				} else
					Toast.makeText(getSherlockActivity(), R.string.authProblem, Toast.LENGTH_LONG).show();
			}
		}

	}
}
