package com.stiggpwnz.vibes;

public interface OnPlayerActionListener {

	public void onBufferingStrated();

	public void onBufferingEnded();

	public void onProgressChanged(int progress);

	public void onBufferingUpdate(int percent);

	public void onCompletion();

	public void onAuthProblem();

	public void onInternetProblem();

}
