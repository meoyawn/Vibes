package com.stiggpwnz.vibes;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class NewService extends Service {

	private final IBinder binder = new ServiceBinder();

	private Player player;
	private ServiceActionListener serviceActionListener;

	public class ServiceBinder extends Binder {

		public NewService getService() {
			return NewService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		player = new Player();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		player.release();
		player = null;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	public Player getPlayer() {
		return player;
	}

	public void setServiceActionListener(ServiceActionListener serviceActionListener) {
		this.serviceActionListener = serviceActionListener;
		player.setServiceActionListener(serviceActionListener);
	}

}
