package com.stiggpwnz.vibes.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.stiggpwnz.vibes.LoginActivity;
import com.stiggpwnz.vibes.PlayerActivity;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.restapi.VKontakte;
import com.stiggpwnz.vibes.util.MyWebView;

public class LoginFragment extends SherlockDialogFragment {

	public static interface Listener {

		public void saveData(String[] params);
	}

	private static final String URL = "url";

	private Listener listener;

	public LoginFragment() {

	}

	public static LoginFragment newInstance(String url) {
		LoginFragment dialog = new LoginFragment();
		Bundle args = new Bundle();
		args.putString(URL, url);
		dialog.setArguments(args);
		return dialog;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		listener = (Listener) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.browser, container);
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getDialog().setTitle(R.string.auth);

		boolean reset = getSherlockActivity().getIntent().getBooleanExtra(LoginActivity.RESET, false);
		String url = getArguments().getString(URL);

		MyWebView webView = (MyWebView) view.findViewById(R.id.MyWebView1);
		final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBrowser);

		CookieSyncManager.createInstance(webView.getContext());
		CookieManager cm = CookieManager.getInstance();
		if (reset) {
			cm.removeAllCookie();
		}

		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				progressBar.setVisibility(View.VISIBLE);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				progressBar.setVisibility(View.GONE);
				Uri uri = Uri.parse(url);
				if (uri != null && uri.toString().startsWith(VKontakte.CALLBACK_URL)) {
					if (uri.toString().contains(VKontakte.ACCESS_TOKEN)) {
						String[] params = new String[3];
						for (String param : uri.getFragment().split("&")) {
							String pair[] = param.split("=");
							Log.d("meridian", pair[0] + " = " + pair[1]);
							if (pair[0].equals(VKontakte.ACCESS_TOKEN))
								params[0] = pair[1];
							else if (pair[0].equals(VKontakte.EXPIRES_IN))
								params[1] = pair[1];
							else if (pair[0].equals(VKontakte.USER_ID))
								params[2] = pair[1];
						}
						listener.saveData(params);
						startActivity(new Intent(getSherlockActivity(), PlayerActivity.class));
						getSherlockActivity().finish();
					}
				}
			}
		});

		webView.setWebChromeClient(new WebChromeClient() {

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				progressBar.setProgress(newProgress);
			}

		});

		webView.loadUrl(url);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

}
