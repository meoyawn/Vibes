package com.stiggpwnz.vibes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.stiggpwnz.vibes.fragments.WebViewFragment;
import com.stiggpwnz.vibes.restapi.VKontakte;

public class LoginActivity extends SherlockFragmentActivity implements OnClickListener, WebViewFragment.Listener {

	public static final String RESET = "RESET";

	private Animation shake;
	private VibesApplication app;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (VibesApplication) getApplication();
		if (app.getSettings().getExpiringTime() > System.currentTimeMillis() / 1000) {
			startActivity(new Intent(this, PlayerActivity.class));
			finish();
		} else {
			setContentView(R.layout.login);
			shake = AnimationUtils.loadAnimation(this, R.anim.shake);
			findViewById(R.id.btnLogin).setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLogin:
			v.startAnimation(shake);
			WebViewFragment.newInstance(VKontakte.loginUrl()).show(getSupportFragmentManager(), "webview");
		}
	}

	@Override
	public void saveData(String[] params) {
		app.getSettings().saveData(params);
	}
}