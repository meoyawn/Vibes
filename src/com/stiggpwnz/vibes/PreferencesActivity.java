package com.stiggpwnz.vibes;

import net.bgreco.DirectoryPicker;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener, OnPreferenceClickListener {

	private VibesApplication app;
	private SharedPreferences prefs;
	private Preference picker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		app = (VibesApplication) getApplication();

		picker = findPreference(Settings.DIRECTORY_PICKER);
		picker.setSummary(app.getSettings().getDirectoryPath());
		picker.setOnPreferenceClickListener(this);
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
		picker.setEnabled(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED));
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

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference == picker) {
			Intent intent = new Intent(this, DirectoryPicker.class);
			intent.putExtra(DirectoryPicker.ONLY_DIRS, false);
			startActivityForResult(intent, DirectoryPicker.PICK_DIRECTORY);
			return true;
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == DirectoryPicker.PICK_DIRECTORY && resultCode == RESULT_OK) {
			Bundle extras = data.getExtras();
			String path = (String) extras.get(DirectoryPicker.CHOSEN_DIRECTORY);
			app.getSettings().setDirectoryPath(path);
			picker.setSummary(path);
		}
	}
}
