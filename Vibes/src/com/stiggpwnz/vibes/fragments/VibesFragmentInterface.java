package com.stiggpwnz.vibes.fragments;

import android.view.View;

public interface VibesFragmentInterface {

	public void onFirstCreated(View view);

	public void runOnUiThread(Runnable runnable);

	public void runOnBackgroundThread(Runnable runnable);
}
