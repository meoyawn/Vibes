package com.stiggpwnz.vibes.fragments.base;

import android.os.Bundle;
import android.view.View;

public abstract class RetainedFragment extends BaseFragment implements RetainedInterface {

	private boolean created;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (created) {
			return;
		}
		onFirstCreated(view);
		created = true;
	}
}
