package com.stiggpwnz.vibes;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.http.client.methods.HttpPost;
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
import android.os.AsyncTask.Status;
import android.os.Handler;
import android.util.Log;

public class Player implements OnCompletionListener, OnPreparedListener, OnSeekCompleteListener, OnBufferingUpdateListener, OnErrorListener, OnInfoListener {

	public enum State {
		STATE_NOT_PREPARED_IDLING, STATE_PLAYING, STATE_PAUSED_IDLING, STATE_PREPARING_FOR_PLAYBACK, STATE_SEEKING_FOR_PLAYBACK, STATE_SEEKING_FOR_IDLE, STATE_PREPARING_FOR_IDLE
	}

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
	private OnPlayerActionListener activity;
	private State state;

	private boolean scrobbled;
	private boolean scrobbling;
	private boolean timeStamped;

	private int shufflePosition;
	private int songDuration;

	private long timeStamp;

	private Random random;
	private List<Integer> shuffleQueue;
	private PreparePlayer preparePlayer;

	public int currentSong;
	private List<Song> songs;
	public Song current;

	private NewService service;
	private Settings settings;

	private Handler handler;

	public Player(NewService newService, Handler handler) {
		this.handler = handler;
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
		settings = ((VibesApplication) service.getApplication()).getSettings();
		settings.setVkontakte(getVkontakte());
		settings.setLastFM(getLastFM());

		shuffleQueue = new LinkedList<Integer>();
		random = new Random();
		if (settings.getShuffle()) {
			generateShuffleQueue(0);
		}
	}

	public void release() {
		player.release();
		client.getConnectionManager().shutdown();
		settings.setVkontakte(null);
		settings.setLastFM(null);
		service = null;
		settings = null;
		getVkontakte().getCache().clear();
		getLastFM().getCache().clear();
		handler.removeCallbacks(progressUpdater);
		handler = null;
	}

	private class Generator extends Thread {

		int seed;

		public Generator(int seed, String name) {
			super(name);
			this.seed = seed;
		}

		@Override
		public synchronized void run() {
			shuffleQueue.clear();
			shuffleQueue.add(seed);
			shufflePosition = 0;
			int n = songs.size();
			for (int i = 1; i < n; i++) {
				boolean flag;
				int m;
				do {
					flag = false;
					m = random.nextInt(n);
					for (int pos : shuffleQueue) {
						if (pos == m) {
							flag = true;
							break;
						}
					}
				} while (flag);
				shuffleQueue.add(m);
			}
		}
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

	private void seekBarUpdater() {
		if (isPlaying()) {
			int position = player.getCurrentPosition();
			if (activity != null)
				activity.onProgressChanged(position);

			if (settings.getSession() != null && !scrobbled && !scrobbling && songDuration > 30000 && (position >= (songDuration / 2) || (position / 60000) >= 4))
				new Thread("Scrobbling") {

					@Override
					public void run() {
						scrobbling = true;
						getLastFM().scrobble(getCurrentSong(), timeStamp);
						scrobbling = false;
						scrobbled = true;
						getLastFM().updateNowPlaying(getCurrentSong());
					}
				}.start();

			handler.postDelayed(progressUpdater, 500);
		} else
			handler.removeCallbacks(progressUpdater);
	}

	private void generateShuffleQueue(int seed) {
		Log.d(VibesApplication.VIBES, "generating queue");
		new Generator(seed, "Generating Random Queue").start();
	}

	public void generateShuffleQueue() {
		generateShuffleQueue(currentSong);
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
			vkontakte = new Vkontakte(settings.getAccessToken(), client, settings.getUserID(), settings.getMaxNews(), settings.getMaxAudio());
		return vkontakte;
	}

	public LastFM getLastFM() {
		if (lastfm == null) {
			int density = service.getResources().getDisplayMetrics().densityDpi;
			lastfm = new LastFM(client, settings.getSession(), density);
		}
		return lastfm;
	}

	public void play() {
		scrobbled = false;
		timeStamped = false;
		if (getCurrentSong() != null) {
			if (state == State.STATE_PAUSED_IDLING || state == State.STATE_PLAYING) {
				state = State.STATE_SEEKING_FOR_PLAYBACK;
				handler.removeCallbacks(progressUpdater);
				player.seekTo(0);
			} else {
				synchronized (this) {
					List<HttpPost> requests = getVkontakte().getAudioUrlRequests();
					if (preparePlayer != null && preparePlayer.getStatus() == Status.RUNNING) {
						Log.e(VibesApplication.VIBES, "cancelling audio url loader: " + requests.size() + " items in queue");
						for (HttpPost request : requests) {
							request.abort();
						}
						preparePlayer.cancel(true);
					}
					requests.clear();
				}
				preparePlayer = new PreparePlayer();
				preparePlayer.execute();
			}
		}
	}

	private class PreparePlayer extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			Thread.currentThread().setName("Preparing New Track");
			if (isCancelled())
				return null;
			if (!isPlaying()) {
				// telling everyone that we're started
				if (activity != null)
					activity.onBufferingStrated();
				if (isCancelled())
					return null;
				state = State.STATE_PREPARING_FOR_PLAYBACK;
				if (isCancelled())
					return null;
				// remembering current song and synchronizingly setting an url for it, if needed
				int currentSong = Player.this.currentSong;
				if (getCurrentSong().url == null) {
					if (isCancelled())
						return null;
					synchronized (this) {
						setSongUrl(getCurrentSong());
					}
				}
				// finally starting to prepare song if still on this position
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
		Log.d(VibesApplication.VIBES, "prepared " + currentSong + " and state = " + state);
		songDuration = player.getDuration();
		if (activity != null)
			activity.onBufferingEnded();
		if (state == State.STATE_PREPARING_FOR_PLAYBACK) {
			if (activity == null)
				service.makeNotification();

			startPlaying();
			if (settings.getSession() != null)
				new Thread("Updating Now Playing") {
					@Override
					public void run() {
						getLastFM().updateNowPlaying(getCurrentSong());
					}
				}.start();
		} else if (state == State.STATE_PREPARING_FOR_IDLE) {
			state = State.STATE_PAUSED_IDLING;
		}

	}

	public Song getCurrentSong() {
		if (current != null)
			return current;
		if (songs != null && songs.size() > 0)
			return songs.get(currentSong);
		return null;
	}

	private void startPlaying() {
		player.start();
		state = State.STATE_PLAYING;
		if (!timeStamped) {
			timeStamp = System.currentTimeMillis() / 1000;
			timeStamped = true;
		}
		seekBarUpdater();
	}

	private void authFail() {
		if (activity != null)
			activity.onAuthProblem();
	}

	private void errorStopPlayback() {
		stop();
		if (activity != null)
			activity.onInternetProblem();
	}

	private void stop() {
		player.reset();
		state = State.STATE_NOT_PREPARED_IDLING;
		service.cancelNotification();
		if (activity == null)
			service.startWaiter();
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		if (activity != null)
			activity.onBufferingUpdate(percent);
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		if (activity != null)
			activity.onBufferingEnded();
		Log.d(VibesApplication.VIBES, "seeking complete");
		if (state == State.STATE_SEEKING_FOR_PLAYBACK) {
			state = State.STATE_PLAYING;
			seekBarUpdater();
		} else if (state == State.STATE_SEEKING_FOR_IDLE)
			state = State.STATE_PAUSED_IDLING;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.e(VibesApplication.VIBES, String.format("catching error: what = %d, extra = %d", what, extra));
		if (what == 1 && extra == -2147483648) {
			Log.e(VibesApplication.VIBES, "reseting while preparing");
			return true;
		} else if (what == 1 && extra == -1002) {
			errorStopPlayback();
			return true;
		} else if (what == 1 && extra == -1004) {
			resetAndPlay();
			return true;
		} else if (what == -38 && extra == 0) {
			// SEEKING WHILE PREPARING
			return true;
		} else if (what == 1 && extra == -11) {
			errorStopPlayback();
			return true;
		} else if (what == 1 && extra == -1) {
			errorStopPlayback();
			return true;
		}
		return false;
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		Log.i(VibesApplication.VIBES, String.format("catching INFO: what = %d, extra = %d", what, extra));
		if (what == 701 && extra == 0) {
			if (activity != null)
				activity.onBufferingStrated();
			return true;
		} else if (what == 702 && extra == 0) {
			if (activity != null)
				activity.onBufferingEnded();
			return true;
		}
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
		if (activity == null) {
			service.stopWaiter();
		}
		if (state == State.STATE_NOT_PREPARED_IDLING)
			play();
		else if (!isPlaying() && state != State.STATE_PREPARING_FOR_IDLE) {
			startPlaying();
			if (activity == null) {
				service.makeNotification();
			}
		} else if (state == State.STATE_PREPARING_FOR_IDLE) {
			state = State.STATE_PREPARING_FOR_PLAYBACK;
		}
	}

	public void pause() {
		if (isPlaying()) {
			player.pause();
			state = State.STATE_PAUSED_IDLING;
			handler.removeCallbacks(progressUpdater);
			if (activity == null) {
				service.cancelNotification();
				service.startWaiter();
			}
		} else if (state == State.STATE_PREPARING_FOR_PLAYBACK) {
			state = State.STATE_PREPARING_FOR_IDLE;
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		scrobbled = false;
		timeStamped = false;
		timeStamp = System.currentTimeMillis() / 1000;
		if (songs != null && songs.size() > 0) {
			Log.d(VibesApplication.VIBES, "onCompleted: playing next song and state = " + state);
			if (!settings.getRepeatPlaylist()) {
				if (settings.getShuffle()) {
					if (shufflePosition == songs.size() - 1)
						stop();
					else
						next();
				} else {
					if (currentSong == songs.size() - 1)
						stop();
					else
						next();
				}
			} else {
				next();
			}
		} else
			stop();

		if (activity != null)
			activity.onCompletion();
	}

	public void next() {
		if (songs.size() > 0) {
			Log.d(VibesApplication.VIBES, "Invoking next() and current = " + currentSong);
			current = null;
			if (settings.getShuffle()) {
				if (++shufflePosition >= songs.size())
					shufflePosition = 0;
				currentSong = shuffleQueue.get(shufflePosition);
			} else {
				if (++currentSong >= songs.size())
					currentSong = 0;
			}
			resetAndPlay();
		}
	}

	private void resetAndPlay() {
		player.reset();
		play();
	}

	public void prev() {
		if (songs.size() > 0) {
			Log.d(VibesApplication.VIBES, "Invoking prev() and current = " + currentSong);
			current = null;
			if (settings.getShuffle()) {
				if (--shufflePosition < 0)
					shufflePosition = songs.size() - 1;
				currentSong = shuffleQueue.get(shufflePosition);
			} else {
				if (--currentSong < 0)
					currentSong = songs.size() - 1;
			}
			resetAndPlay();
		}
	}

	public void seekTo(int position) {
		Log.d(VibesApplication.VIBES, "seeking...");
		if (isPlaying()) {
			handler.removeCallbacks(progressUpdater);
			state = State.STATE_SEEKING_FOR_PLAYBACK;
			player.seekTo(position);
		} else if (state == State.STATE_PAUSED_IDLING) {
			state = State.STATE_SEEKING_FOR_IDLE;
			player.seekTo(position);
		}
	}

	public int getSongDuration() {
		try {
			return player.getDuration();
		} catch (Exception e) {
			return 0;
		}
	}

	public State getState() {
		return state;
	}

}
