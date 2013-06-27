package com.stiggpwnz.vibes.activities;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class BaseFragmentActivity extends SherlockFragmentActivity {

	protected static final int FRAME = 843684698;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FrameLayout frameLayout = new FrameLayout(this);
		frameLayout.setId(FRAME);
		setContentView(frameLayout);
	}
}
