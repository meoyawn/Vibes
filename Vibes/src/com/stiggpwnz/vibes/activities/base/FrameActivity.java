package com.stiggpwnz.vibes.activities.base;

import android.os.Bundle;
import android.widget.FrameLayout;

public abstract class FrameActivity extends BaseActivity {

	protected static final int FRAME = 843684698;

	@Override
	public void onCreateView(Bundle savedInstanceState) {
		FrameLayout frameLayout = new FrameLayout(this);
		frameLayout.setId(FRAME);
		setContentView(frameLayout);
	}

	@Override
	public void onViewCreated(Bundle savedInstanceState) {

	}

	@Override
	public void onRecreated(Bundle savedInstanceState) {

	}
}
