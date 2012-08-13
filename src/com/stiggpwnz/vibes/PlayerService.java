package com.stiggpwnz.vibes;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.http.client.methods.HttpPost;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class PlayerService extends Service implements OnCompletionListener, OnPreparedListener, OnSeekCompleteListener, OnBufferingUpdateListener, OnErrorListener, OnInfoListener {

	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_BUFFERING_STARTED = 3;
	public static final int MSG_BUFFERING_STOPPED = 4;
	public static final int MSG_SET_CURRENT_SONG = 5;
	public static final int MSG_PLAY = 6;
	public static final int MSG_PAUSE = 7;
	public static final int MSG_RESUME = 8;
	public static final int MSG_NEXT = 9;
	public static final int MSG_PREV = 10;
	public static final int MSG_SET_PROGRESS = 12;
	public static final int MSG_SEEK_TO = 13;
	public static final int MSG_SET_CURRENT_SONG_SERVICE = 14;
	public static final int MSG_ON_RESUME = 15;
	public static final int MSG_ON_RESUME_SERVICE = 17;
	public static final int MSG_ON_PAUSE_SERVICE = 16;
	public static final int MSG_SET_SHUFFLE = 18;
	public static final int MSG_SET_REPEAT = 19;
	public static final int MSG_UNBIND_SERVICE = 20;
	public static final int MSG_UPDATE_BUFFER = 21;
	public static final int MSG_AUTH_PROBLEM = 22;
	public static final int MSG_INTERNET_PROBLEM = 23;
	public static final int MSG_DOWNLOAD = 24;

	public static final int STATE_NOT_PREPARED_IDLING = 0;
	public static final int STATE_PLAYING = 3;
	public static final int STATE_PAUSED_IDLING = 4;
	public static final int STATE_PREPARING_FOR_PLAYBACK = 6;
	public static final int STATE_NEXT_FOR_PLAYBACK = 1;
	public static final int STATE_PREPARED_IDLING = 2;
	public static final int STATE_SEEKING = 5;
	public static final int STATE_PREPARING_FOR_IDLE = 7;

	private static final long IDLE_TIME = 3 * 60 * 1000;

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
			sendCommand(MSG_UNBIND_SERVICE, 0, 0);
			state = STATE_NOT_PREPARED_IDLING;
		}
	};

	private final Messenger mMessenger = new Messenger(new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				messenger = msg.replyTo;
				break;
			case MSG_UNREGISTER_CLIENT:
				messenger = null;
				break;
			case MSG_PLAY:
				play();
				break;
			case MSG_PAUSE:
				pause();
				break;
			case MSG_RESUME:
				resume();
				break;
			case MSG_NEXT:
				next();
				break;
			case MSG_PREV:
				prev();
				break;
			case MSG_SEEK_TO:
				seekTo(msg.arg1);
				break;
			case MSG_SET_CURRENT_SONG_SERVICE:
				app.current = null;
				scrobbled = false;
				timeStamped = false;
				if (msg.arg1 != app.currentSong) {
					Log.d(VibesApplication.VIBES, "reseting " + app.currentSong);
					new SetSongFromPlaylist().execute(msg.arg1);
				} else {
					if (state == STATE_NOT_PREPARED_IDLING) {
						play();
					} else {
						if (state != STATE_PREPARING_FOR_PLAYBACK && state != STATE_PREPARING_FOR_IDLE) {
							sendCommand(MSG_BUFFERING_STARTED, 0, 0);
							player.seekTo(0);
							startPlaying();
						}
					}
				}
				break;
			case MSG_ON_PAUSE_SERVICE:
				activityIsAlive = false;
				if (player.isPlaying()) {
					makeNotification();
				} else {
					if (state == STATE_NOT_PREPARED_IDLING || state == STATE_PAUSED_IDLING || state == STATE_PREPARED_IDLING || state == STATE_PREPARING_FOR_IDLE)
						startWaiter();
				}
				break;
			case MSG_ON_RESUME_SERVICE:
				sendCommand(MSG_ON_RESUME, state, songDuration);
				if (state == STATE_PAUSED_IDLING)
					sendCommand(MSG_SET_PROGRESS, player.getCurrentPosition(), 0);
				else if (state != STATE_NOT_PREPARED_IDLING && player.isPlaying())
					seekBarUpdater();
				activityIsAlive = true;
				cancelNotification();
				stopWaiter();

				break;
			case MSG_SET_REPEAT:
				if (msg.arg1 == 1)
					player.setLooping(true);
				else
					player.setLooping(false);
				break;
			case MSG_SET_SHUFFLE:
				if (msg.arg1 == 1)
					generateShuffleQueue(app.currentSong == -1 ? 0 : app.currentSong);
				break;
			case MSG_DOWNLOAD:
				downloadSong();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	});
	public static boolean isRunning;

	private boolean scrobbled;
	private boolean scrobbling;
	private boolean activityIsAlive = true;
	private boolean wasPlaying;
	private boolean alreadySet;
	private boolean timeStamped;

	private int state;
	private int shufflePosition;
	private int songDuration;

	private long timeStamp;

	private MediaPlayer player;
	private VibesApplication app;

	private Messenger messenger;
	private Random random;
	private List<Integer> shuffleQueue;
	private WifiManager.WifiLock wifiLock;
	private PreparePlayer preparePlayer;
	private ResetPlayer resetPlayer;
	private List<Integer> downloadQueue;

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		new Thread("Creating Service") {

			@Override
			public void run() {
				app = (VibesApplication) getApplication();
				shuffleQueue = new LinkedList<Integer>();
				random = new Random();
				if (app.getShuffle()) {
					generateShuffleQueue(0);
				}

				wifiLock = ((WifiManager) getSystemService(WIFI_SERVICE)).createWifiLock(VibesApplication.SONG);
				wifiLock.acquire();

				player = new MediaPlayer();
				player.setOnCompletionListener(PlayerService.this);
				player.setOnPreparedListener(PlayerService.this);
				player.setOnSeekCompleteListener(PlayerService.this);
				player.setOnBufferingUpdateListener(PlayerService.this);
				player.setOnErrorListener(PlayerService.this);
				player.setOnInfoListener(PlayerService.this);

				isRunning = true;
				Log.d(VibesApplication.VIBES, "Service created");
			}
		}.start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopWaiter();
		wifiLock.release();
		if (player != null) {
			player.release();
			handler.removeCallbacks(progressUpdater);
		}
		cancelNotification();
		isRunning = false;
		Log.d(VibesApplication.VIBES, "Service destroyed");
	}

	private class PreparePlayer extends AsyncTask<Void, Void, Void> {

		private void getUrl() {
			try {
				app.getVkontakte().setSongUrl(app.getCurrentSong());
			} catch (IOException e) {
				errorStopPlayback();
			} catch (VkontakteException e) {
				switch (e.getCode()) {
				case VkontakteException.UNKNOWN_ERROR_OCCURED:
					errorStopPlayback();
					break;

				case VkontakteException.USER_AUTHORIZATION_FAILED:
					authFail();
					break;

				case VkontakteException.TOO_MANY_REQUESTS_PER_SECOND:
					getUrl();
					break;

				default:
					errorStopPlayback();
				}

			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			Thread.currentThread().setName("Preparing New Track");
			if (isCancelled())
				return null;
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
						getUrl();
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
					try {
						Log.d(VibesApplication.VIBES, app.getCurrentSong().url);
						player.setDataSource(app.getCurrentSong().url);
						if (isCancelled())
							return null;
						Log.d(VibesApplication.VIBES, "preparing " + app.currentSong + "...");
						player.prepareAsync();
						return null;
					} catch (IllegalArgumentException e) {
						cancel(true);
					} catch (IllegalStateException e) {
						return resetAndPrepare();
					} catch (IOException e) {
						errorStopPlayback();
					}
				}
			}
			return null;
		}

		private Void resetAndPrepare() {
			if (isCancelled())
				return null;
			player.reset();
			if (isCancelled())
				return null;
			try {
				player.setDataSource(app.getCurrentSong().url);
				if (isCancelled())
					return null;
				Log.d(VibesApplication.VIBES, "preparing " + app.currentSong + "...");
				player.prepareAsync();
			} catch (IllegalArgumentException e) {
				return resetAndPrepare();
			} catch (IllegalStateException e) {
				return resetAndPrepare();
			} catch (IOException e) {

			}
			return null;
		}

	}

	private class ResetPlayer extends AsyncTask<Void, Void, Void> {

		int song;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			song = app.currentSong;
		}

		@Override
		protected Void doInBackground(Void... params) {
			Thread.currentThread().setName("Reseting Player");
			if (!alreadySet && activityIsAlive) {
				if (isCancelled())
					return null;
				sendCommand(MSG_SET_CURRENT_SONG, app.currentSong, 0);
				alreadySet = true;
			}
			if (isCancelled())
				return null;
			player.reset();
			if (isCancelled())
				return null;
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			scrobbled = false;
			timeStamped = false;
			if (song == app.currentSong) {
				if (wasPlaying || state == STATE_PREPARING_FOR_PLAYBACK) {
					state = STATE_NEXT_FOR_PLAYBACK;
					play();
				} else {
					state = STATE_NOT_PREPARED_IDLING;
				}
			} else
				Log.d(VibesApplication.VIBES, "another song got called");
		}

	}

	private class SetSongFromPlaylist extends AsyncTask<Integer, Void, Void> {

		private int song;

		@Override
		protected Void doInBackground(Integer... params) {
			Thread.currentThread().setName("Setting Song From Playlist");
			app.currentSong = params[0];
			if (app.getShuffle())
				generateShuffleQueue(app.currentSong);
			song = params[0];
			sendCommand(MSG_SET_CURRENT_SONG, app.currentSong, 1);
			alreadySet = true;
			player.reset();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (app.currentSong == song) {
				state = STATE_PREPARING_FOR_PLAYBACK;
				play();
			} else
				Log.d(VibesApplication.VIBES, "another song got called");
		}

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
			int n = app.songs.size();
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

	private void play() {
		if (app.getCurrentSong() != null) {
			synchronized (this) {
				List<HttpPost> requests = app.getVkontakte().getAudioUrlRequests();
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

	private void authFail() {
		stop();
		if (activityIsAlive) {
			sendCommand(MSG_AUTH_PROBLEM, 0, 0);
		}
	}

	private void errorStopPlayback() {
		stop();
		if (activityIsAlive) {
			sendCommand(MSG_INTERNET_PROBLEM, 0, 0);
		}
	}

	private void stop() {
		wasPlaying = false;
		player.reset();
		state = STATE_NOT_PREPARED_IDLING;
		cancelNotification();
		if (!activityIsAlive)
			startWaiter();
	}

	private void startPlaying() {
		state = STATE_PLAYING;
		player.start();
		if (!timeStamped) {
			timeStamp = System.currentTimeMillis() / 1000;
			timeStamped = true;
		}
		seekBarUpdater();
		wasPlaying = true;
	}

	private void pause() {
		if (player.isPlaying()) {
			player.pause();
			state = STATE_PAUSED_IDLING;
			wasPlaying = false;
			handler.removeCallbacks(progressUpdater);
			if (!activityIsAlive) {
				cancelNotification();
				startWaiter();
			}
		} else if (state == STATE_PREPARING_FOR_PLAYBACK) {
			wasPlaying = false;
			state = STATE_PREPARING_FOR_IDLE;
		}
	}

	private void resume() {
		if (!activityIsAlive) {
			stopWaiter();
		}
		if (state == STATE_NOT_PREPARED_IDLING)
			play();
		else if (!player.isPlaying() && state != STATE_PREPARING_FOR_IDLE) {
			startPlaying();
			wasPlaying = true;
			if (!activityIsAlive) {
				makeNotification();
			}
		} else if (state == STATE_PREPARING_FOR_IDLE) {
			state = STATE_PREPARING_FOR_PLAYBACK;
		}
	}

	private void next() {
		if (app.songs.size() > 0) {
			Log.d(VibesApplication.VIBES, "Invoking next() and current = " + app.currentSong);
			app.current = null;
			alreadySet = false;
			if (app.getShuffle()) {
				if (!app.getRepeatPlaylist()) {
					if (shufflePosition == app.songs.size() - 1) {
						Log.d(VibesApplication.VIBES, "last song and state = " + state);
						Log.d(VibesApplication.VIBES, player.isPlaying() ? "player is playing" : "player is not playing");
						if (state != STATE_PLAYING && state != STATE_PREPARING_FOR_PLAYBACK) {
							stopPlayback();
						}
					} else {
						shufflePosition++;
						app.currentSong = shuffleQueue.get(shufflePosition);
						resetAndPlay();
					}
				} else {
					if (++shufflePosition >= app.songs.size()) {
						shufflePosition = 0;
					}
					app.currentSong = shuffleQueue.get(shufflePosition);
					resetAndPlay();
				}
			} else {
				if (!app.getRepeatPlaylist()) {
					if (app.currentSong == app.songs.size() - 1) {
						Log.d(VibesApplication.VIBES, "last song and state = " + state);
						Log.d(VibesApplication.VIBES, player.isPlaying() ? "player is playing" : "player is not playing");
						if (state != STATE_PLAYING && state != STATE_PREPARING_FOR_PLAYBACK) {
							stopPlayback();
						}
					} else {
						app.currentSong++;
						resetAndPlay();
					}
				} else {
					if (++app.currentSong >= app.songs.size()) {
						app.currentSong = 0;
					}
					resetAndPlay();
				}
			}
		}
	}

	private void prev() {
		if (app.songs.size() > 0) {

			Log.d(VibesApplication.VIBES, "Invoking prev() and current = " + app.currentSong);
			app.current = null;
			alreadySet = false;
			if (app.getShuffle()) {
				if (--shufflePosition < 0) {
					shufflePosition = app.songs.size() - 1;
				}
				app.currentSong = shuffleQueue.get(shufflePosition);
			} else {
				if (--app.currentSong < 0) {
					app.currentSong = app.songs.size() - 1;
				}
			}
			resetAndPlay();
		}
	}

	private void seekTo(final int position) {
		if (wasPlaying && state != STATE_PREPARING_FOR_PLAYBACK && state != STATE_PREPARING_FOR_IDLE) {
			handler.removeCallbacks(progressUpdater);
			Log.d(VibesApplication.VIBES, "seeking...");
			state = STATE_SEEKING;
			player.seekTo(position);
		} else if (state == STATE_PAUSED_IDLING) {
			state = STATE_SEEKING;
			player.seekTo(position);
		}
	}

	private void resetAndPlay() {
		if (resetPlayer != null && resetPlayer.getStatus() == Status.RUNNING)
			resetPlayer.cancel(true);
		resetPlayer = new ResetPlayer();
		resetPlayer.execute();
	}

	private void generateShuffleQueue(int seed) {
		Log.d(VibesApplication.VIBES, "generating queue");
		new Generator(seed, "Generating Random Queue").start();
	}

	private void stopPlayback() {
		Log.d(VibesApplication.VIBES, "onCompleted: stopped");
		if (!activityIsAlive) {
			stop();
		} else {
			sendCommand(MSG_ON_RESUME, state, songDuration);
		}
	}

	private void sendCommand(int msg, int data1, int data2) {
		if (messenger != null) {
			try {
				messenger.send(Message.obtain(null, msg, data1, data2));
			} catch (RemoteException e) {
				messenger = null;
			}
		}
	}

	public VibesApplication getApp() {
		return app;
	}

	private void downloadSong() {
		try {
			new Downloader(this).download(app.getCurrentSong());
		} catch (IOException e) {
			onDownloadException(e.getLocalizedMessage());
		}
	}

	public void onDownloadException(String message) {
		if (activityIsAlive && message != null) {
			Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.TOP, 50, 0);
			toast.show();
		}
	}

	// usually, subclasses of AsyncTask are declared inside the activity class.
	// that way, you can easily modify the UI thread from here

	private void seekBarUpdater() {
		if (player.isPlaying() && (state != STATE_SEEKING || state != STATE_NEXT_FOR_PLAYBACK || state != STATE_PREPARING_FOR_PLAYBACK)) {
			int position = player.getCurrentPosition();
			if (activityIsAlive)
				sendCommand(MSG_SET_PROGRESS, position, 0);

			if (app.getSession() != null && !scrobbled && songDuration > 30000 && !scrobbling && (position >= songDuration / 2 || position / 60000 >= 4)) {
				new Thread("Scrobbling") {

					@Override
					public void run() {
						scrobbling = true;
						app.getLastFM().scrobble(app.getCurrentSong(), timeStamp);
						scrobbled = true;
						scrobbling = false;
					}

				}.start();
			}

			handler.postDelayed(progressUpdater, 500);
		} else
			handler.removeCallbacks(progressUpdater);
	}

	private void startWaiter() {
		stopWaiter();
		Log.d(VibesApplication.VIBES, "starting waiter");
		handler.postDelayed(serviceKiller, IDLE_TIME);
	}

	private void stopWaiter() {
		Log.d(VibesApplication.VIBES, "stopping waiter");
		handler.removeCallbacks(serviceKiller);
	}

	private void makeNotification() {
		final Notification notification = new Notification(R.drawable.icon, String.format("%s - %s", app.getCurrentSong().performer, app.getCurrentSong().title), System
				.currentTimeMillis());
		CharSequence contentTitle = app.getCurrentSong().title;
		CharSequence contentText = app.getCurrentSong().performer;
		Intent notifyIntent = new Intent(PlayerService.this, PlayerActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent intent = PendingIntent.getActivity(PlayerService.this, 0, notifyIntent, 0);
		notification.setLatestEventInfo(app, contentTitle, contentText, intent);
		notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
		app.getNotificationManager().notify(VibesApplication.SONG, VibesApplication.NOTIFICATION, notification);

	}

	private void cancelNotification() {
		app.getNotificationManager().cancel(VibesApplication.SONG, VibesApplication.NOTIFICATION);

	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.d(VibesApplication.VIBES, "onCompleted");
		state = STATE_PAUSED_IDLING;
		scrobbled = false;
		timeStamped = false;
		timeStamp = System.currentTimeMillis() / 1000;
		if (!player.isLooping()) {
			if (app.songs != null && app.songs.size() > 0) {
				if (state != STATE_PREPARING_FOR_PLAYBACK && state != STATE_PREPARING_FOR_IDLE && state != STATE_NEXT_FOR_PLAYBACK) {
					Log.d(VibesApplication.VIBES, "onCompleted: playing next song and state = " + state);
					next();
				}
			} else
				stopPlayback();
		}
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.d(VibesApplication.VIBES, "prepared " + app.currentSong + " and state = " + state);
		songDuration = player.getDuration();
		if (activityIsAlive)
			sendCommand(MSG_BUFFERING_STOPPED, songDuration, 0);
		if (state == STATE_PREPARING_FOR_PLAYBACK) {
			if (!activityIsAlive) {
				makeNotification();
			}
			startPlaying();
			if (app.getSession() != null)
				new Thread("Updating Now Playing") {
					@Override
					public void run() {
						app.getLastFM().updateNowPlaying(app.getCurrentSong());
					}
				}.start();
		} else if (state == STATE_PREPARING_FOR_IDLE) {
			state = STATE_PREPARED_IDLING;
		}

	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		sendCommand(MSG_BUFFERING_STOPPED, 0, 0);
		Log.d(VibesApplication.VIBES, "seeking complete");
		if (wasPlaying) {
			state = STATE_PLAYING;
			seekBarUpdater();
		} else
			state = STATE_PAUSED_IDLING;
	}

	@Override
	public void onBufferingUpdate(MediaPlayer arg0, int percent) {
		if (activityIsAlive)
			sendCommand(MSG_UPDATE_BUFFER, percent * songDuration / 100, 0);
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
			new ResetPlayer().execute();
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
		if (what == 1 && extra == 26) {
			// just an info, do nothing
			return true;
		} else if (what == 1 && extra == 44) {
			// just an info, do nothing
			return true;
		} else if (what == 701 && extra == 0) {
			if (activityIsAlive)
				sendCommand(MSG_BUFFERING_STARTED, 0, 0);
			return true;
		} else if (what == 702 && extra == 0) {
			if (activityIsAlive)
				sendCommand(MSG_BUFFERING_STOPPED, 0, 0);
			return true;
		}
		return false;
	}

	public List<Integer> getDownloadQueue() {
		if (downloadQueue == null)
			downloadQueue = new LinkedList<Integer>();
		return downloadQueue;
	}

}
