package com.stiggpwnz.vibes;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
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
	private OnPlayerActionListener listener;
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
		this.listener = listener;
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
		// TODO Auto-generated method stub

	}

	public void startWaiter() {
		// TODO Auto-generated method stub

	}

	public void cancelNotification() {
		// TODO Auto-generated method stub

	}

	public void stopWaiter() {
		// TODO Auto-generated method stub

	}

}
