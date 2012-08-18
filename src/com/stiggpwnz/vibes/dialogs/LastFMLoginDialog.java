package com.stiggpwnz.vibes.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.stiggpwnz.vibes.PlayerActivity;
import com.stiggpwnz.vibes.R;

public class LastFMLoginDialog extends Dialog implements OnClickListener {

	private EditText editUsername;
	private EditText editPassword;

	public LastFMLoginDialog(Context context) {
		super(context);
		setContentView(R.layout.last_auth);
		setTitle(R.string.auth);
		setCanceledOnTouchOutside(true);
		editUsername = (EditText) findViewById(R.id.editUsername);
		editPassword = (EditText) findViewById(R.id.editPassword);
		Button btnSignIn = (Button) findViewById(R.id.btnSingIn);
		btnSignIn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (editUsername.getText().length() > 0 && editPassword.getText().length() > 0)
			new LastFmSignIn().execute();
	}

	private class LastFmSignIn extends AsyncTask<Void, Void, String[]> {

		private PlayerActivity playerActivity;
		private ProgressDialog authDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			playerActivity = (PlayerActivity) getOwnerActivity();
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
			return playerActivity.getApp().getLastFM().auth(username, password);
		}

		@Override
		protected void onPostExecute(String[] result) {
			super.onPostExecute(result);
			authDialog.dismiss();
			if (result != null) {
				playerActivity.getApp().getSettings().saveLastFM(result);
				dismiss();
				Toast.makeText(getContext(), R.string.last_success, Toast.LENGTH_SHORT).show();
			} else
				Toast.makeText(getContext(), R.string.authProblem, Toast.LENGTH_LONG).show();
		}

	}

}
