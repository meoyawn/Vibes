package com.stiggpwnz.vibes;

public interface OnPlayerActionListener {

	public void onBufferingStrated();

	public void onBufferingEnded();

	public void onPlayerProgressUpdate();

	public void onPlayerBufferingUpdate(int percent);

	public void onPlayerStopped();

	public void onSongChanged();

	public void onAuthProblem();

	public void onInternetProblem();

}
