package com.stiggpwnz.vibes.fragments;

import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import butterknife.InjectView;

import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.activities.MainActivity;
import com.stiggpwnz.vibes.fragments.base.RetainedFragment;
import com.stiggpwnz.vibes.util.Persistance;
import com.stiggpwnz.vibes.vk.VKontakte;

public class LoginFragment extends RetainedFragment {

	@InjectView(R.id.webview) WebView webView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.webview, container, false);
	}

	private final WebViewClient webViewClient = new WebViewClient() {

		@Override
		public void onPageFinished(WebView view, String url) {

			if (url.startsWith(VKontakte.REDIRECT_URL)) {
				CookieSyncManager.getInstance().sync();
				Map<String, String> result = VKontakte.parseRedirectUrl(url);
				Persistance.saveVK(result);
				startActivity(new Intent(getSherlockActivity(), MainActivity.class));
				getSherlockActivity().finish();
			}
		};
	};

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		webView.setWebViewClient(webViewClient);
	};

	public WebView getWebView() {
		return webView;
	}

	@Override
	public void onFirstCreated(View view) {
		webView.loadUrl(VKontakte.authUrl());
	}

	@Override
	public void onReCreated(View view) {

	}
}
