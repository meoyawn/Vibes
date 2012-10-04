package com.stiggpwnz.vibes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.stiggpwnz.vibes.fragments.WebViewFragment;
import com.stiggpwnz.vibes.restapi.VKontakte;

public class LoginActivity extends SherlockFragmentActivity implements OnClickListener, WebViewFragment.Listener {

	public static final String RESET = "reset";
	private static final String WEBVIEW = "webview";

	private VibesApplication app;
	private View playButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (VibesApplication) getApplication();
		long currentTimeInSeconds = System.currentTimeMillis() / 1000;
		if (app.getSettings().getExpiringTime() > currentTimeInSeconds) {
			startActivity(new Intent(this, PlayerActivity.class));
			finish();
		} else {
			setContentView(R.layout.login);
			playButton = findViewById(R.id.btnLogin);
			findViewById(android.R.id.content).setOnClickListener(this);

			if (getIntent().getBooleanExtra(RESET, false)) {
				CookieSyncManager.createInstance(this);
				CookieManager.getInstance().removeAllCookie();
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case android.R.id.content:
			playButton.startAnimation(app.getShakeAnimation());
			WebViewFragment.newInstance(VKontakte.getAuthUrl()).show(getSupportFragmentManager(), WEBVIEW);
		}
	}

	@Override
	public void saveData(String[] params) {
		app.getSettings().saveVKontakte(params);
	}
}
