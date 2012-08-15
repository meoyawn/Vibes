package com.stiggpwnz.vibes;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

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
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class PlayerService extends Service implements OnCompletionListener, OnPreparedListener, OnSeekCompleteListener, OnBufferingUpdateListener, OnErrorListener, OnInfoListener {





	









	








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
