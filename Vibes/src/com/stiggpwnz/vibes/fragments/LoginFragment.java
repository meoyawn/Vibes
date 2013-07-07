package com.stiggpwnz.vibes.fragments;

import java.util.Map;

import android.os.Bundle;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import butterknife.InjectView;

import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.fragments.base.RetainedProgressFragment;
import com.stiggpwnz.vibes.util.Persistance;
import com.stiggpwnz.vibes.vk.VKAuthenticator;

public class LoginFragment extends RetainedProgressFragment {

	@InjectView(R.id.webview) WebView webView;

	@Override
	protected void onCreateView(Bundle savedInstanceState) {
		setContentView(R.layout.webview);
	}

	private final WebViewClient webViewClient = new WebViewClient() {

		@Override
		public void onPageFinished(WebView view, String url) {

			if (url.startsWith(VKAuthenticator.REDIRECT_URL)) {
				CookieSyncManager.getInstance().sync();
				Map<String, String> result = VKAuthenticator.parseRedirectUrl(url);
				Persistance.saveVK(result);
			}
		};
	};

	@Override
	protected void onViewCreated(Bundle savedInstanceState) {
		webView.setWebViewClient(webViewClient);
	}

	public WebView getWebView() {
		return webView;
	}

	@Override
	public void onFirstCreated(View view) {
		webView.loadUrl(VKAuthenticator.authUrl());
	}

}
