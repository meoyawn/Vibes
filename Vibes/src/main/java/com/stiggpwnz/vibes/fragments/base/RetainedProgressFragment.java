package com.stiggpwnz.vibes.fragments.base;

import android.os.Bundle;

public abstract class RetainedProgressFragment extends BaseProgressFragment implements RetainedInterface {

	private boolean isCreated;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (isCreated) {
			onReCreated(getContentView());
		} else {
			onFirstCreated(getContentView());
			isCreated = true;
		}
	}

}
