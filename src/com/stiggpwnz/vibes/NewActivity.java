package com.stiggpwnz.vibes;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.stiggpwnz.vibes.NewService.LocalBinder;

public class NewActivity extends Activity implements ServiceActionListener {

	private final ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			playerService = binder.getService();
			bound = true;
			playerService.setServiceActionListener(NewActivity.this);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			playerService.setServiceActionListener(null);
			playerService = null;
			bound = false;
		}
	};

	private NewService playerService;
	private boolean bound = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	protected void onResume() {
		super.onResume();

		Intent intent = new Intent(this, NewService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (bound) {
			unbindService(mConnection);
			bound = false;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// TODO Auto-generated method stub
	}

	@Override
	public void onPlayerProgressUpdate(int progress) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerBufferingUpdate(int percent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerPrepared() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerStopped() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerSeekComplete() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerInfo(int what, int extra) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerError(int what, int extra) {
		// TODO Auto-generated method stub

	}

}
