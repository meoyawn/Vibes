package com.stiggpwnz.vibes;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;

public class Player implements OnCompletionListener, OnPreparedListener, OnSeekCompleteListener, OnBufferingUpdateListener, OnErrorListener, OnInfoListener {

	private MediaPlayer player;
	private ServiceActionListener serviceActionListener;

	public Player(ServiceActionListener serviceActionListener) {
		player = new MediaPlayer();
		player.setOnCompletionListener(this);
		player.setOnPreparedListener(this);
		player.setOnSeekCompleteListener(this);
		player.setOnBufferingUpdateListener(this);
		player.setOnErrorListener(this);
		player.setOnInfoListener(this);
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		if (serviceActionListener != null)
			serviceActionListener.onPlayerPrepared();
		// TODO Auto-generated method stub

	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		if (serviceActionListener != null)
			serviceActionListener.onPlayerBufferingUpdate(percent);
		// TODO Auto-generated method stub

	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		if (serviceActionListener != null)
			serviceActionListener.onPlayerPrepared();
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onInfo(MediaPlayer arg0, int what, int extra) {
		if (serviceActionListener != null)
			serviceActionListener.onPlayerPrepared();
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		if (serviceActionListener != null)
			serviceActionListener.onPlayerPrepared();
		// TODO Auto-generated method stub
		return false;
	}

	public void setServiceActionListener(ServiceActionListener serviceActionListener) {
		this.serviceActionListener = serviceActionListener;
	}

}
