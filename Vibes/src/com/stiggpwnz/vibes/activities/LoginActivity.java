package com.stiggpwnz.vibes.activities;

import android.support.v4.app.Fragment;
import android.view.View;
import android.webkit.WebView;

import com.stiggpwnz.vibes.activities.base.FrameActivity;
import com.stiggpwnz.vibes.fragments.LoginFragment;

public class LoginActivity extends FrameActivity {

	@Override
	public void onFirstCreated(View view) {
		getSupportFragmentManager().beginTransaction().add(FRAME, new LoginFragment()).commit();
	}

	@Override
	public void onBackPressed() {
		Fragment fragmentById = getSupportFragmentManager().findFragmentById(FRAME);
		if (fragmentById != null && fragmentById instanceof LoginFragment) {
			LoginFragment loginFragment = (LoginFragment) fragmentById;
			WebView webView = loginFragment.getWebView();
			if (webView != null && webView.canGoBack()) {
				webView.goBack();
				return;
			}
		}
		super.onBackPressed();
	}
}
