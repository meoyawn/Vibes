package com.stiggpwnz.vibes;

public interface ServiceActionListener {
	
	public void onPlayerProgressUpdate();

	public void onPlayerBufferingUpdate(int percent);

	public void onPlayerPrepared();

	public void onPlayerStopped();

	public void onPlayerSeekComplete();
	
	public void onBufferingStrated();
	
	public void onBufferingEnded();
	
	public void onSongChanged();

}
