package com.stiggpwnz.vibes.fragments;

import java.io.IOException;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import butterknife.InjectView;

import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.Validator.ValidationListener;
import com.mobsandgeeks.saripaar.annotation.Password;
import com.mobsandgeeks.saripaar.annotation.Required;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.activities.MainActivity;
import com.stiggpwnz.vibes.fragments.base.RetainedProgressFragment;
import com.stiggpwnz.vibes.util.Cookies;
import com.stiggpwnz.vibes.util.Persistance;
import com.stiggpwnz.vkauth.VKAuthenticator;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class LoginFragment extends RetainedProgressFragment {

	public static final int CLIENT_ID = 3027476;
	public static final int SCOPE = 2 + 8 + 8192;

	@InjectView(R.id.email) @Required(order = 1) EditText emailView;
	@InjectView(R.id.password) @Password(order = 2) EditText passwordView;
	@InjectView(R.id.sign_in_button) Button signIn;

	private final Validator validator = new Validator(this);
	private final VKAuthenticator vkAuth = new VKAuthenticator(CLIENT_ID, SCOPE, Cookies.get());

	@Override
	protected void onCreateView(Bundle savedInstanceState) {
		setContentView(R.layout.login);
	}

	@Override
	protected void onViewCreated(Bundle savedInstanceState) {
		passwordView.setOnEditorActionListener(onEnter);
		signIn.setOnClickListener(onSignInClick);
	}

	private final OnEditorActionListener onEnter = new OnEditorActionListener() {

		@Override
		public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
			if (id == R.id.login || id == EditorInfo.IME_NULL) {
				validator.validate();
				return true;
			}
			return false;
		}
	};

	private final OnClickListener onSignInClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			validator.validate();
		}
	};

	@Override
	public void onFirstCreated(View view) {
		validator.setValidationListener(validationListener);
	}

	private final Runnable showContent = new Runnable() {

		@Override
		public void run() {
			setContentShown(true);
		}
	};

	private final ValidationListener validationListener = new ValidationListener() {

		@Override
		public void preValidation() {

		}

		@Override
		public void onValidationSuccess() {
			auth();
		}

		@Override
		public void onValidationFailed(View arg0, Rule<?> arg1) {
			if (arg0 instanceof EditText) {
				EditText editText = (EditText) arg0;
				editText.requestFocus();
				editText.setError(arg1.getFailureMessage());
			}
		}

		@Override
		public void onValidationCancelled() {

		}
	};

	private final Runnable auth = new Runnable() {

		@Override
		public void run() {
			try {
				final Map<String, String> result = vkAuth.auth(emailView.getText().toString(), passwordView.getText().toString());
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						parseResultOnMainThread(result);
					}
				});
			} catch (final IOException e) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						showErrorOnMainThread(e);
					}
				});
			} finally {
				runOnUiThread(showContent);
			}
		}
	};

	private void auth() {
		if (!isContentShown()) {
			return;
		}

		setContentShown(false);
		runOnBackgroundThread(auth);
	}

	private void parseResultOnMainThread(final Map<String, String> result) {
		if (result.containsKey(VKAuthenticator.ACCESS_TOKEN)) {
			Persistance.saveEmailPassword(emailView.getText().toString(), passwordView.getText().toString());
			Persistance.saveVK(result);

			startActivity(new Intent(getSherlockActivity(), MainActivity.class));
			getSherlockActivity().finish();
		} else if (result.containsKey(VKAuthenticator.ERROR)) {
			Crouton.makeText(getSherlockActivity(), result.get(VKAuthenticator.ERROR), Style.ALERT).show();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Cookies.save(vkAuth.getCookieManager().getStore());
	}

	private void showErrorOnMainThread(final IOException e) {
		// TODO error logic
		Crouton.makeText(getSherlockActivity(), e.getMessage(), Style.ALERT).show();
	}

}
