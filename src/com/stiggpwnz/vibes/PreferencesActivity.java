package com.stiggpwnz.vibes;

import net.bgreco.DirectoryPicker;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.stiggpwnz.vibes.dialogs.LastFMLoginDialog;
import com.stiggpwnz.vibes.dialogs.LastFMLoginDialog.LastFMLoginListener;
import com.stiggpwnz.vibes.dialogs.LastFMUserDialog;
import com.stiggpwnz.vibes.dialogs.LastFMUserDialog.LastFMUserListener;
import com.stiggpwnz.vibes.imageloader.ImageLoader;

public class PreferencesActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener, OnPreferenceClickListener, LastFMLoginListener,
		LastFMUserListener {

	private VibesApplication app;
	private SharedPreferences prefs;
	private Preference picker;
	private Preference lastFm;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		app = (VibesApplication) getApplication();
		Settings settings = app.getSettings();

		picker = findPreference(Settings.DIRECTORY_PICKER);
		picker.setSummary(settings.getDirectoryPath());
		picker.setOnPreferenceClickListener(this);

		lastFm = findPreference(Settings.SESSION);
		if (settings.getSession() != null)
			lastFm.setSummary(settings.getUsername());
		else
			lastFm.setSummary(R.string.sign_in);
		lastFm.setOnPreferenceClickListener(this);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
		else if (key.equals(Settings.FINISHED_NOTIFICATION))
			app.getSettings().updateFinishedNotification();
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference == picker) {
			Intent intent = new Intent(this, DirectoryPicker.class);
			startActivityForResult(intent, DirectoryPicker.PICK_DIRECTORY);
			return true;
		} else if (preference == lastFm) {
			launchLastFM();
			return true;
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
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

	private void launchLastFM() {
		Settings settings = app.getSettings();
		if (settings.getSession() == null)
			new LastFMLoginDialog(this).show();
		else
			new LastFMUserDialog(this, settings.getUsername(), settings.getUserImage()).show();
	}

	@Override
	public ImageLoader getImageLoader() {
		return app.getImageLoader();
	}

	@Override
	public String[] lastFmAuth(String username, String password) {
		return app.getLastFM().auth(username, password);
	}

	@Override
	public void saveLastFM(String[] params) {
		app.getSettings().saveLastFM(params);
		lastFm.setSummary(params[0]);
	}

	@Override
	public void resetLastFM() {
		app.getSettings().resetLastFM();
		lastFm.setSummary(R.string.sign_in);
		new LastFMLoginDialog(this).show();
	}

}
