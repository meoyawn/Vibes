package com.stiggpwnz.vibes;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class NewService extends Service {

	private final IBinder binder = new ServiceBinder();

	private NotificationManager notificationManager;
	private Player player;
	private PlayerListener listener;

	public class ServiceBinder extends Binder {

		public NewService getService() {
			return NewService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		player = new Player(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		player.release();
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

	public void onDownloadException(String messsage) {
		// TODO Auto-generated method stub

	}

	public void setPlayerListener(PlayerListener listener) {
		this.listener = listener;
		player.setListener(listener);
	}

}
