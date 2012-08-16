package com.stiggpwnz.vibes;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class NewService extends Service {

	private static final String SONG = "song";
	private static final int NOTIFICATION = 49;
	private static final long IDLE_TIME = 3 * 60 * 1000;

	private final IBinder binder = new ServiceBinder();
	private final Handler handler = new Handler();
	private final Runnable serviceKiller = new Runnable() {

		@Override
		public void run() {
			stopSelf();
		}
	};

	private NotificationManager notificationManager;
	private Player player;
	private VibesApplication app;
	private List<Integer> downloadQueue;
	private WifiManager.WifiLock wifiLock;

	public class ServiceBinder extends Binder {

		public NewService getService() {
			return NewService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		app = (VibesApplication) getApplication();
		app.setServiceRunning(true);
		player = new Player(this, handler);
		wifiLock = ((WifiManager) getSystemService(WIFI_SERVICE)).createWifiLock(SONG);
		wifiLock.acquire();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		cancelNotification();
		wifiLock.release();
		player.release();
		app.setServiceRunning(false);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	public Player getPlayer() {
		return player;
	}

	public NotificationManager getNotificationManager() {
		if (notificationManager == null)
			notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		return notificationManager;
	}

	public void setPlayerListener(OnPlayerActionListener listener) {
		player.setListener(listener);
	}

	public List<Integer> getDownloadQueue() {
		if (downloadQueue == null)
			downloadQueue = new LinkedList<Integer>();
		return downloadQueue;
	}

	public void download(int position) {
		try {
			new Downloader(this).download(player.getSongs().get(position));
		} catch (IOException e) {
			onDownloadException(e.getLocalizedMessage());
		}
	}

	public void onDownloadException(String message) {
		if (message != null) {
			Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.TOP, 50, 0);
			toast.show();
		}
	}

	public void makeNotification() {
		final Notification notification = new Notification(R.drawable.icon, String.format("%s - %s", player.getCurrentSong().performer, player.getCurrentSong().title), System
				.currentTimeMillis());
		CharSequence contentTitle = player.getCurrentSong().title;
		CharSequence contentText = player.getCurrentSong().performer;
		Intent notifyIntent = new Intent(this, NewActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent intent = PendingIntent.getActivity(this, 0, notifyIntent, 0);
		notification.setLatestEventInfo(app, contentTitle, contentText, intent);
		notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
		getNotificationManager().notify(SONG, NOTIFICATION, notification);
	}

	public void cancelNotification() {
		getNotificationManager().cancel(SONG, NOTIFICATION);
	}

	public void startWaiter() {
		stopWaiter();
		Log.d(VibesApplication.VIBES, "starting waiter");
		handler.postDelayed(serviceKiller, IDLE_TIME);
	}

	public void stopWaiter() {
		Log.d(VibesApplication.VIBES, "stopping waiter");
		handler.removeCallbacks(serviceKiller);
	}

}
