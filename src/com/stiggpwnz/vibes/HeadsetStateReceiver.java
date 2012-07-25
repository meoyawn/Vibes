package com.stiggpwnz.vibes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class HeadsetStateReceiver extends BroadcastReceiver {

	public static final String STATE = "state";

	@Override
	public void onReceive(Context context, Intent intent) {
		int headset = intent.getIntExtra(STATE, 2);
		Log.d(VibesApplication.VIBES, "recieving headset " + headset);
		context.startService(new Intent(context, PlayerService.class).putExtra(STATE, headset));
	}

}
