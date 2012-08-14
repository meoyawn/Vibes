package com.stiggpwnz.vibes;

import java.util.List;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
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

	public static final String SONG = "song";

	public static final int NOTIFICATION = 49;

	private final Runnable progressUpdater = new Runnable() {

		@Override
		public void run() {
			seekBarUpdater();
		}
	};

	private AbstractHttpClient client;
	private Vkontakte vkontakte;
	private LastFM lastfm;

	private MediaPlayer player;
	private PlayerListener activity;
	private State state;

	private int currentSong;
	private List<Song> songs;
	private Song current;

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
		client = threadSafeHttpClient();
	}

	public void release() {
		player.release();
		client.getConnectionManager().shutdown();
		service = null;
	}

	private AbstractHttpClient threadSafeHttpClient() {
		HttpParams params = new BasicHttpParams();
		int connectionTimeout = 3000;
		HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
		int socketTimeout = 5000;
		HttpConnectionParams.setSoTimeout(params, socketTimeout);

		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

		ClientConnectionManager manager = new ThreadSafeClientConnManager(params, registry);
		AbstractHttpClient client = new DefaultHttpClient(manager, params);
		return client;
	}

	protected void seekBarUpdater() {
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

	public void setListener(PlayerListener playerListener) {
		this.activity = playerListener;
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

	public List<Integer> getDownloadQueue() {
		// TODO Auto-generated method stub
		return null;
	}

	public void onDownloadException(String messsage) {
		// TODO Auto-generated method stub

	}

}
