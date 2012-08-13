package com.stiggpwnz.vibes;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.stiggpwnz.vibes.NewService.ServiceBinder;

public class NewActivity extends Activity implements ServiceActionListener {

	private final ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			service = ((ServiceBinder) binder).getService();
			service.setServiceActionListener(NewActivity.this);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			service.setServiceActionListener(null);
			service = null;
		}
	};

	private NewService service;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	protected void onResume() {
		super.onResume();

		Intent intent = new Intent(this, NewService.class);
		bindService(intent, connection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onPause() {
		super.onPause();

		unbindService(connection);
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

}
