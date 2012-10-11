package com.stiggpwnz.vibes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class HeadsetStateReciever extends BroadcastReceiver {

	public static final String STATE = "state";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!isInitialStickyBroadcast() && PlayerService.isRunning) {
			int state = intent.getIntExtra(STATE, 0);
			context.startService(new Intent(context, PlayerService.class).putExtra(STATE, state));
		}
	}

}
