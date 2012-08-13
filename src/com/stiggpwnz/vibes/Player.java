package com.stiggpwnz.vibes;

import java.io.IOException;
import java.util.List;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.AsyncTask;
import android.util.Log;

public class Player implements OnCompletionListener, OnPreparedListener, OnSeekCompleteListener, OnBufferingUpdateListener, OnErrorListener, OnInfoListener {

	private enum State {
		STATE_NOT_PREPARED_IDLING, STATE_PLAYING, STATE_PAUSED_IDLING, STATE_PREPARING_FOR_PLAYBACK, STATE_NEXT_FOR_PLAYBACK, STATE_PREPARED_IDLING, STATE_SEEKING, STATE_PREPARING_FOR_IDLE
	}

	private final Runnable progressUpdater = new Runnable() {

		@Override
		public void run() {
			seekBarUpdater();
		}
	};

	private MediaPlayer player;
	private ServiceActionListener serviceActionListener;
	private State state;
	
	public int currentSong;
	public List<Song> songs;
	public Song current;

	public Player() {
		player = new MediaPlayer();
		player.setOnCompletionListener(this);
		player.setOnPreparedListener(this);
		player.setOnSeekCompleteListener(this);
		player.setOnBufferingUpdateListener(this);
		player.setOnErrorListener(this);
		player.setOnInfoListener(this);

		state = State.STATE_NOT_PREPARED_IDLING;
	}

	protected void seekBarUpdater() {
		// TODO Auto-generated method stub

	}

	public void release() {
		player.release();
	}

	private void setUrl() {
		try {
			app.getVkontakte().setSongUrl(app.getCurrentSong());
		} catch (VkontakteException e) {
			switch (e.getCode()) {
			case VkontakteException.UNKNOWN_ERROR_OCCURED:
				errorStopPlayback();
				break;

			case VkontakteException.USER_AUTHORIZATION_FAILED:
				authFail();
				break;

			case VkontakteException.TOO_MANY_REQUESTS_PER_SECOND:
				setUrl();
				break;

			default:
				errorStopPlayback();
			}
		} catch (Exception e) {
			errorStopPlayback();
		}
	}

	private class PreparePlayer extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			Thread.currentThread().setName("Preparing New Track");
			if (state == STATE_PREPARED_IDLING) {
				if (isCancelled())
					return null;
				startPlaying();
			} else if (!player.isPlaying()) {
				if (isCancelled())
					return null;
				state = STATE_PREPARING_FOR_PLAYBACK;
				if (activityIsAlive) {
					if (isCancelled())
						return null;
					sendCommand(MSG_BUFFERING_STARTED, 0, 0);
					if (!isCancelled() && !alreadySet) {
						if (isCancelled())
							return null;
						sendCommand(MSG_SET_CURRENT_SONG, app.currentSong, 0);
						alreadySet = true;
					}
				}
				if (isCancelled())
					return null;
				int currentSong = app.currentSong;
				synchronized (this) {
					if (app.getCurrentSong().url == null) {
						Log.d(VibesApplication.VIBES, "entering geturl");
						if (isCancelled())
							return null;
						setUrl();
						Log.d(VibesApplication.VIBES, "leaving geturl");
					}
				}
				if (currentSong == app.currentSong) {
					if (isCancelled())
						return null;
					if (app.getCurrentSong().url == null) {
						errorStopPlayback();
						return null;
					}
					prepare();
				}
			}
			return null;
		}

		private Void prepare() {
			if (isCancelled())
				return null;
			try {
				player.setDataSource(app.getCurrentSong().url);
				if (isCancelled())
					return null;
				Log.d(VibesApplication.VIBES, "preparing " + app.currentSong + "...");
				player.prepareAsync();
			} catch (IllegalArgumentException e) {
				return null;
			} catch (IllegalStateException e) {
				player.reset();
				return prepare();
			} catch (IOException e) {

			}
			return null;
		}

	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		if (serviceActionListener != null)
			serviceActionListener.onPlayerPrepared();
		// TODO Auto-generated method stub

	}

	public void authFail() {
		// TODO Auto-generated method stub

	}

	public void errorStopPlayback() {
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
