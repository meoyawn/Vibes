package com.stiggpwnz.vibes;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class NewService extends Service {

	private final IBinder mBinder = new LocalBinder();

	private Player player;
	private ServiceActionListener serviceActionListener;

	public class LocalBinder extends Binder {

		NewService getService() {
			return NewService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public Player getPlayer() {
		return player;
	}

	public void setServiceActionListener(ServiceActionListener serviceActionListener) {
		this.serviceActionListener = serviceActionListener;
		player.setServiceActionListener(serviceActionListener);
	}

}
