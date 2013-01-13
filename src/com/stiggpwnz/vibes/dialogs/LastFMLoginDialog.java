package com.stiggpwnz.vibes.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.Misc;
import com.stiggpwnz.vibes.VibesApplication;

public class LastFMLoginDialog extends Dialog implements OnClickListener {

	private VibesApplication app;

	private EditText editUsername;
	private EditText editPassword;

	// workaround to prevent multiple authorizations at a time
	private LastFmSignIn signIn;

	public LastFMLoginDialog(Context context) {
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);
		app = (VibesApplication) getOwnerActivity().getApplication();
		setContentView(R.layout.last_auth);

		editUsername = (EditText) findViewById(R.id.editUsername);
		editPassword = (EditText) findViewById(R.id.editPassword);

		findViewById(R.id.btnSingIn).setOnClickListener(this);
		findViewById(R.id.textForgotUsername).setOnClickListener(this);
		findViewById(R.id.textForgotPassword).setOnClickListener(this);
		findViewById(R.id.textRegister).setOnClickListener(this);

		setCanceledOnTouchOutside(true);
		Misc.showKeyboardFor(editUsername);

		editPassword.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (signIn == null) {

					// login
					if (editUsername.getText().length() > 0 && editPassword.getText().length() > 0) {
						signIn = new LastFmSignIn();
						signIn.execute();
					}

					Misc.hideKeyboard(v);
					return true;
				}
				return false;
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnSingIn:
			if (editUsername.getText().length() > 0 && editPassword.getText().length() > 0)
				new LastFmSignIn().execute();
			break;

		case R.id.textForgotUsername:
			getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.last.fm/settings/lostusername")));
			break;

		case R.id.textForgotPassword:
			getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.last.fm/settings/lostpassword")));
			break;

		case R.id.textRegister:
			getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.last.fm/join")));
			break;
		}

	}

	private class LastFmSignIn extends AsyncTask<Void, Void, String[]> {

		private ProgressDialog authDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			authDialog = new ProgressDialog(getContext());
			authDialog.setMessage(getContext().getString(R.string.auth));
			authDialog.setIndeterminate(true);
			authDialog.setCancelable(false);
			authDialog.show();
		}

		@Override
		protected String[] doInBackground(Void... params) {
			Thread.currentThread().setName("Signing in to LastFM");

			String username = editUsername.getText().toString();
			String password = editPassword.getText().toString();

			return app.getLastFM().auth(username, password);
		}

		@Override
		protected void onPostExecute(String[] result) {
			super.onPostExecute(result);
			authDialog.dismiss();
			if (result != null) {
				app.getSettings().saveLastFM(result);
				dismiss();
				Toast.makeText(getContext(), R.string.last_success, Toast.LENGTH_SHORT).show();
			} else
				Toast.makeText(getContext(), R.string.authProblem, Toast.LENGTH_LONG).show();
		}
	}
}
