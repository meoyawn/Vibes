package com.stiggpwnz.vibes;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.stiggpwnz.vibes.restapi.VKontakte;

public class Settings {

	public static interface OnActionListener {

		public void onLastFmSessionChanged(String session);

		public void onVkontakteAccessTokenChanged(int userId, String accessToken);

		public void onVkontakteMaxAudiosChanged(int maxAudios);

		public void onVkontakteMaxNewsChanged(int maxNews);
	}

	public static final String MAX_NEWS = "max news";
	public static final String MAX_AUDIOS = "max audios";
	public static final String REPEAT_PLAYLIST = "repeat";
	public static final String DIRECTORY_PICKER = "directory picker";
	public static final String FINISHED_NOTIFICATION = "finish download notification";

	private static final String USERNAME = "username";
	private static final String USER_IMAGE = "user image";
	private static final String SESSION = "session";
	private static final String SHUFFLE = "shuffle";

	private SharedPreferences prefs;
	private OnActionListener listener;
	private Context context;

	// vkontakte settings
	private String accessToken;
	private long expiringTime;
	private int userID;
	private int maxNews;
	private int maxAudios;

	// last.fm settings
	private String username;
	private String userImage;
	private String session;

	// player settings
	private Boolean shuffle;
	private Boolean repeatPlaylist;
	private String directoryPath;
	private Boolean finishedNotification;

	public Settings(Context context, OnActionListener listener) {
		this.context = context;
		this.listener = listener;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public void saveVKontakte(String[] params) {
		SharedPreferences.Editor editor = prefs.edit();

		accessToken = params[0];
		editor.putString(VKontakte.ACCESS_TOKEN, accessToken);

		expiringTime = System.currentTimeMillis() / 1000 + Integer.parseInt(params[1]);
		editor.putLong(VKontakte.EXPIRES_IN, expiringTime);

		userID = Integer.valueOf(params[2]);
		editor.putInt(VKontakte.USER_ID, userID);

		editor.commit();

		listener.onVkontakteAccessTokenChanged(userID, accessToken);
	}

	public void resetVKontakte() {
		SharedPreferences.Editor editor = prefs.edit();

		editor.remove(VKontakte.ACCESS_TOKEN);
		accessToken = null;

		editor.remove(VKontakte.EXPIRES_IN);
		expiringTime = 0;

		editor.remove(VKontakte.USER_ID);
		userID = 0;

		editor.commit();

		listener.onVkontakteAccessTokenChanged(userID, accessToken);
	}

	public void saveLastFM(String[] params) {
		SharedPreferences.Editor editor = prefs.edit();

		username = params[0];
		editor.putString(USERNAME, username);

		session = params[1];
		editor.putString(SESSION, session);

		userImage = params[2];
		editor.putString(USER_IMAGE, userImage);

		editor.commit();

		listener.onLastFmSessionChanged(session);
	}

	public void resetLastFM() {
		SharedPreferences.Editor editor = prefs.edit();

		editor.remove(USERNAME);
		username = null;

		editor.remove(SESSION);
		session = null;

		editor.remove(USER_IMAGE);
		userImage = null;

		editor.commit();

		listener.onLastFmSessionChanged(session);
	}

	public String getAccessToken() {
		if (accessToken == null)
			accessToken = prefs.getString(VKontakte.ACCESS_TOKEN, null);
		return accessToken;
	}

	public long getExpiringTime() {
		if (expiringTime == 0)
			expiringTime = prefs.getLong(VKontakte.EXPIRES_IN, 0);
		return expiringTime;
	}

	public int getUserID() {
		if (userID == 0)
			userID = prefs.getInt(VKontakte.USER_ID, 0);
		return userID;
	}

	public String getUsername() {
		if (username == null)
			username = prefs.getString(USERNAME, null);
		return username;
	}

	public String getUserImage() {
		if (userImage == null)
			userImage = prefs.getString(USER_IMAGE, null);
		return userImage;
	}

	public String getSession() {
		if (session == null)
			session = prefs.getString(SESSION, null);
		return session;
	}

	public int getMaxNews() {
		if (maxNews == 0)
			maxNews = Integer.valueOf(prefs.getString(MAX_NEWS, context.getString(R.string.default_max_news)));
		return maxNews;
	}

	public void updateMaxNews() {
		maxNews = Integer.valueOf(prefs.getString(MAX_NEWS, context.getString(R.string.default_max_news)));
		listener.onVkontakteMaxNewsChanged(maxNews);
	}

	public int getMaxAudio() {
		if (maxAudios == 0)
			maxAudios = Integer.valueOf(prefs.getString(MAX_AUDIOS, context.getString(R.string.default_max_audio)));
		return maxAudios;
	}

	public void updateMaxAudio() {
		maxAudios = Integer.valueOf(prefs.getString(MAX_AUDIOS, context.getString(R.string.default_max_audio)));
		listener.onVkontakteMaxAudiosChanged(maxAudios);
	}

	public boolean getRepeatPlaylist() {
		if (repeatPlaylist == null)
			repeatPlaylist = prefs.getBoolean(REPEAT_PLAYLIST, false);
		return repeatPlaylist;
	}

	public void updateRepeatPlaylist() {
		repeatPlaylist = prefs.getBoolean(REPEAT_PLAYLIST, false);
	}

	public boolean getShuffle() {
		if (shuffle == null)
			shuffle = prefs.getBoolean(SHUFFLE, false);
		return shuffle;
	}

	public void setShuffle(boolean shuffle) {
		if (this.shuffle == null || this.shuffle.booleanValue() != shuffle) {
			this.shuffle = shuffle;
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean(SHUFFLE, shuffle);
			editor.commit();
		}
	}

	public String getDirectoryPath() {
		if (directoryPath == null) {
			String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music";
			directoryPath = prefs.getString(DIRECTORY_PICKER, path);
		}
		return directoryPath;
	}

	public void setDirectoryPath(String path) {
		if (directoryPath == null || !directoryPath.equals(path)) {
			directoryPath = path;
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(DIRECTORY_PICKER, path);
			editor.commit();
		}
	}

	public Boolean getFinishedNotification() {
		if (finishedNotification == null)
			finishedNotification = prefs.getBoolean(FINISHED_NOTIFICATION, false);
		return finishedNotification;
	}

	public void updateFinishedNotification() {
		finishedNotification = prefs.getBoolean(FINISHED_NOTIFICATION, false);
	}

}
