package com.stiggpwnz.vibes.fragments;

import java.io.IOException;
import java.util.Map;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.Validator.ValidationListener;
import com.mobsandgeeks.saripaar.annotation.Password;
import com.mobsandgeeks.saripaar.annotation.Required;
import com.stiggpwnz.vibes.Persistance;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.activities.MainActivity;
import com.stiggpwnz.vkauth.VKAuthenticator;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class LoginFragment extends BaseProgressFragment {

	public static final int CLIENT_ID = 3027476;
	public static final int SCOPE = 2 + 8 + 8192;

	private VKAuthenticator vkAuth;
	private final Validator validator = new Validator(this);

	@Required(order = 1)
	private EditText emailView;

	@Password(order = 2)
	private EditText passwordView;

	@Override
	public void onFirstCreated(View view) {
		vkAuth = new VKAuthenticator(CLIENT_ID, SCOPE, Persistance.Cookies.get(getSherlockActivity()));

		setContentView(R.layout.login_fragment);

		validator.setValidationListener(validationListener);

		emailView = (EditText) view.findViewById(R.id.email);

		passwordView = (EditText) view.findViewById(R.id.password);
		passwordView.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					validator.validate();
					return true;
				}
				return false;
			}
		});

		view.findViewById(R.id.sign_in_button).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				validator.validate();
			}
		});

		setContentShownNoAnimation(true);
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

	private void auth() {
		if (!isContentShown()) {
			return;
		}

		setContentShown(false);
		new Thread() {

			@Override
			public void run() {
				try {
					final Map<String, String> result = vkAuth.auth(emailView.getText().toString(), passwordView.getText().toString());
					if (getSherlockActivity() != null) {
						getSherlockActivity().runOnUiThread(new Runnable() {

							@Override
							public void run() {
								parseResultOnMainThread(result);
							}
						});
					}
				} catch (final IOException e) {
					if (getSherlockActivity() != null) {
						getSherlockActivity().runOnUiThread(new Runnable() {

							@Override
							public void run() {
								showErrorOnMainThread(e);
							}
						});
					}
				} finally {
					if (getSherlockActivity() != null) {
						getSherlockActivity().runOnUiThread(showContent);
					}
				}
			};
		}.start();
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
		Persistance.Cookies.save(getSherlockActivity(), vkAuth.getCookieManager().getStore());
		Persistance.Cookies.release();
	}

	private void showErrorOnMainThread(final IOException e) {
		// TODO error logic
		Crouton.makeText(getSherlockActivity(), e.getMessage(), Style.ALERT).show();
	}
}
