package com.stiggpwnz.vibes.activities;

import com.stiggpwnz.vibes.fragments.LoginFragment;

import android.os.Bundle;

public class LoginActivity extends BaseFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(FRAME, new LoginFragment()).commit();
		}
	}
}
