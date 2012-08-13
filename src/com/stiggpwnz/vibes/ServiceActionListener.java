package com.stiggpwnz.vibes;

public interface ServiceActionListener {
	
	public void onPlayerProgressUpdate(int progress);

	public void onPlayerBufferingUpdate(int percent);

	public void onPlayerPrepared();

	public void onPlayerStopped();

	public void onPlayerSeekComplete();

	public void onPlayerInfo(int what, int extra);

	public void onPlayerError(int what, int extra);

}
