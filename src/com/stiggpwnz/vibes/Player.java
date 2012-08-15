package com.stiggpwnz.vibes;

import java.util.List;

import org.apache.http.impl.client.AbstractHttpClient;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class Player implements OnCompletionListener, OnPreparedListener, OnSeekCompleteListener, OnBufferingUpdateListener, OnErrorListener, OnInfoListener {

	public enum State {
		STATE_NOT_PREPARED_IDLING, STATE_PLAYING, STATE_PAUSED_IDLING, STATE_PREPARING_FOR_PLAYBACK, STATE_NEXT_FOR_PLAYBACK, STATE_PREPARED_IDLING, STATE_SEEKING, STATE_PREPARING_FOR_IDLE
	}

	public static final String SONG = "song";
	public static final int NOTIFICATION = 49;

	private final Handler handler = new Handler();
	private final Runnable progressUpdater = new Runnable() {

		@Override
		public void run() {
			seekBarUpdater();
		}
	};
	private final Runnable serviceKiller = new Runnable() {

		@Override
		public void run() {
			service.stopSelf();
		}
	};

	private AbstractHttpClient client;
	private Vkontakte vkontakte;
	private LastFM lastfm;

	private MediaPlayer player;
	private OnPlayerActionListener activity;
	private State state;

	public State getState() {
		return state;
	}

	public int currentSong;
	private List<Song> songs;
	public Song current;

	private NewService service;

	public Player(NewService newService) {
		state = State.STATE_NOT_PREPARED_IDLING;

		player = new MediaPlayer();
		player.setOnCompletionListener(this);
		player.setOnPreparedListener(this);
		player.setOnSeekCompleteListener(this);
		player.setOnBufferingUpdateListener(this);
		player.setOnErrorListener(this);
		player.setOnInfoListener(this);

		this.service = newService;
		client = VibesApplication.threadSafeHttpClient();
		((VibesApplication) service.getApplication()).getSettings().setVkontakte(getVkontakte());
	}

	public void release() {
		player.release();
		client.getConnectionManager().shutdown();
		((VibesApplication) service.getApplication()).getSettings().setVkontakte(null);
		service = null;
		getVkontakte().getCache().clear();
		if (lastfm != null)
			lastfm.getCache().clear();
	}

	public boolean isPlaying() {
		try {
			return player.isPlaying();
		} catch (IllegalStateException e) {
			return false;
		}
	}

	public void setLooping(boolean looping) {
		player.setLooping(looping);
	}

	public boolean isLooping() {
		return player.isLooping();
	}

	protected void seekBarUpdater() {
		// TODO Auto-generated method stub

	}

	public void generateShuffleQueue() {
		// TODO Auto-generated method stub

	}

	private void setSongUrl(Song song) {
		try {
			getVkontakte().setSongUrl(song);
		} catch (VkontakteException e) {
			switch (e.getCode()) {
			case VkontakteException.UNKNOWN_ERROR_OCCURED:
				errorStopPlayback();
				break;

			case VkontakteException.USER_AUTHORIZATION_FAILED:
				authFail();
				break;

			case VkontakteException.TOO_MANY_REQUESTS_PER_SECOND:
				setSongUrl(song);
				break;

			default:
				errorStopPlayback();
			}
		} catch (Exception e) {
			errorStopPlayback();
		}
	}

	public Vkontakte getVkontakte() {
		if (vkontakte == null)
			vkontakte = new Vkontakte(getAccessToken(), client, getUserID(), getMaxNews(), getMaxAudio());
		return vkontakte;
	}

	private int getMaxAudio() {
		// TODO Auto-generated method stub
		return 0;
	}

	private int getMaxNews() {
		// TODO Auto-generated method stub
		return 0;
	}

	private int getUserID() {
		// TODO Auto-generated method stub
		return 0;
	}

	private String getAccessToken() {
		// TODO Auto-generated method stub
		return null;
	}

	public LastFM getLastFM() {
		if (lastfm == null) {
			int density = service.getResources().getDisplayMetrics().densityDpi;
			lastfm = new LastFM(client, getSession(), density);
		}
		return lastfm;
	}

	private String getSession() {
		// TODO Auto-generated method stub
		return null;
	}

	private class PreparePlayer extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			Thread.currentThread().setName("Preparing New Track");
			if (isCancelled())
				return null;
			if (state == State.STATE_PREPARED_IDLING) {
				// no need to prepare
				startPlaying();
			} else if (!player.isPlaying()) {
				// telling everyone that we're started
				if (activity != null)
					activity.onBufferingStrated();
				if (isCancelled())
					return null;
				state = State.STATE_PREPARING_FOR_PLAYBACK;
				if (isCancelled())
					return null;
				// fixing current song and synchronizingly setting an url for it, if needed
				int currentSong = Player.this.currentSong;
				if (getCurrentSong().url == null) {
					if (isCancelled())
						return null;
					synchronized (this) {
						setSongUrl(getCurrentSong());
					}
				}
				// finally starting to prepare song
				if (currentSong == Player.this.currentSong) {
					if (isCancelled())
						return null;
					if (getCurrentSong().url == null) {
						errorStopPlayback();
						return null;
					}
					prepare(currentSong);
				}
			}
			return null;
		}

		private void prepare(int currentSong) {
			if (isCancelled())
				return;
			try {
				player.setDataSource(getCurrentSong().url);
				if (isCancelled())
					return;
				Log.d(VibesApplication.VIBES, "preparing " + Player.this.currentSong + "...");
				player.prepare();
			} catch (IllegalStateException e) {
				// reseting and recursively preparing if in wrong state
				if (Player.this.currentSong == currentSong) {
					player.reset();
					prepare(currentSong);
				}
			} catch (Exception e) {

			}
			return;
		}

	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO Auto-generated method stub

	}

	public Song getCurrentSong() {
		if (current != null)
			return current;
		if (songs != null && songs.size() > 0)
			return songs.get(currentSong);
		return null;
	}

	public void startPlaying() {
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
		if (activity != null)
			activity.onPlayerBufferingUpdate(percent);
		// TODO Auto-generated method stub

	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onInfo(MediaPlayer arg0, int what, int extra) {

		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {

		// TODO Auto-generated method stub
		return false;
	}

	public void setListener(OnPlayerActionListener onPlayerActionListener) {
		this.activity = onPlayerActionListener;
	}

	public List<Song> getSongs() {
		return songs;
	}

	public void setSongs(List<Song> songs) {
		this.songs = songs;
	}

	public Song getCurrent() {
		return current;
	}

	public void setCurrent(Song current) {
		this.current = current;
	}

	public Context getContext() {
		return service;
	}

	public void resume() {
		// TODO Auto-generated method stub

	}

	public void pause() {
		// TODO Auto-generated method stub

	}

	public void play() {
		// TODO Auto-generated method stub

	}

	public void next() {
		// TODO Auto-generated method stub

	}

	public void prev() {
		// TODO Auto-generated method stub

	}

	public void seekTo(int progress) {
		// TODO Auto-generated method stub
		
	}

}
