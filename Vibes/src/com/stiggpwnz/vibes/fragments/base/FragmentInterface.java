package com.stiggpwnz.vibes.fragments.base;

public interface FragmentInterface {

	public void runOnUiThread(Runnable runnable);

	public void runOnBackgroundThread(Runnable runnable);
}
