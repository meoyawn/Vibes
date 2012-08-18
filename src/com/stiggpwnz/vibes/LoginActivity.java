package com.stiggpwnz.vibes;

import com.stiggpwnz.vibes.restapi.Vkontakte;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class LoginActivity extends Activity {

	public static final String RESET = "RESET";
	
	private static final String SCOPE = "friends,audio,wall,groups"; // GIVEN RIGHTS
	private static final String AUTH_URL = "http://api.vk.com/oauth/authorize";
	private static final String CALLBACK_URL = "http://api.vk.com/blank.html";
	private static final int DIALOG_BROWSER = 47;

	private VibesApplication app;
	private boolean reset;
	private String url;
	private WebView webView;
	private Animation shake;
	private View btnLogin;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (VibesApplication) getApplication();
		reset = getIntent().getBooleanExtra(RESET, false);
		if (app.getSettings().getExpiringTime() > System.currentTimeMillis() / 1000) {
			startActivity(new Intent(getApplicationContext(), NewActivity.class));
			finish();
		} else
			initUI();
	}

	private void initUI() {
		setContentView(R.layout.login);
		btnLogin = findViewById(R.id.btnLogin);
		shake = AnimationUtils.loadAnimation(this, R.anim.shake);
		btnLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				btnLogin.startAnimation(shake);
				showDialog(DIALOG_BROWSER);
			}

		});
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		webView.loadUrl(url);
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_BROWSER:
			Dialog dialog = new Dialog(this);
			dialog.setContentView(R.layout.browser);
			dialog.setTitle(R.string.auth);
			webView = (WebView) dialog.findViewById(R.id.webView1);
			final ProgressBar progressBar = (ProgressBar) dialog.findViewById(R.id.progressBrowser);

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
					progressBar.setProgress(100);
					Uri uri = Uri.parse(url);
					if (uri != null && uri.toString().startsWith(CALLBACK_URL)) {
						if (uri.toString().contains(Vkontakte.ACCESS_TOKEN)) {
							String[] params = new String[3];
							for (String param : uri.getFragment().split("&")) {
								String pair[] = param.split("=");
								Log.d("meridian", pair[0] + " = " + pair[1]);
								if (pair[0].equals(Vkontakte.ACCESS_TOKEN))
									params[0] = pair[1];
								else if (pair[0].equals(Vkontakte.EXPIRES_IN))
									params[1] = pair[1];
								else if (pair[0].equals(Vkontakte.USER_ID))
									params[2] = pair[1];
							}
							app.getSettings().saveData(params);
							startActivity(new Intent(getApplicationContext(), NewActivity.class));
							finish();
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

			url = queryString();
			Log.d("meridian", url);
			return dialog;
		default:
			return super.onCreateDialog(id);

		}
	}

	private static String queryString() {
		return AUTH_URL + "?client_id=" + Vkontakte.CLIENT_ID + "&scope=" + SCOPE + "&redirect_uri=" + CALLBACK_URL + "&display=touch&response_type=token";
	}

}