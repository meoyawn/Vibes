package com.stiggpwnz.vibes;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private VibesApplication app;
	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		app = (VibesApplication) getApplication();
	}

	@Override
	protected void onPause() {
		super.onPause();
		prefs.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(Settings.MAX_NEWS))
			app.getSettings().updateMaxNews();
		else if (key.equals(Settings.REPEAT_PLAYLIST))
			app.getSettings().updateRepeatPlaylist();
		else if (key.equals(Settings.MAX_AUDIOS))
			app.getSettings().updateMaxAudio();
	}
}
