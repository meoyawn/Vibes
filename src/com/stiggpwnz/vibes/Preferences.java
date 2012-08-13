package com.stiggpwnz.vibes;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {

	public static final String ACCESS_TOKEN = "access_token";
	public static final String EXPIRES_IN = "expires_in";
	public static final String USER_ID = "user_id";
	public static final String MAX_NEWS = "max news";
	public static final String MAX_AUDIO = "max audio";
	public static final String REPEAT_PLAYLIST = "repeat";

	private static final String USERNAME = "username";
	private static final String USER_IMAGE = "user_image";
	private static final String SESSION = "session";
	private static final String SHUFFLE = "shuffle";
	private static final String PLAYLIST = "playlist";
	private static final String OWNER = "owner";
	private static final String LAST_SEARCH = "last search";
	private static final String ALBUM = "album";

	private SharedPreferences prefs;

	private String accessToken;
	private long expiringTime;
	private int userID;

	private String username;
	private String userImage;
	private String session;

	private int playlist = -1;
	private int owner = -1;
	private int album = -1;
	private String lastSearch;
	private Boolean shuffle;
	private Boolean repeatPlaylist;

	private int maxNews;
	private int maxAudio;

	public Preferences(Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public void saveData(String[] params) {
		SharedPreferences.Editor editor = prefs.edit();

		accessToken = params[0];
		editor.putString(ACCESS_TOKEN, accessToken);

		expiringTime = System.currentTimeMillis() / 1000 + Integer.parseInt(params[1]);
		editor.putLong(EXPIRES_IN, expiringTime);

		userID = Integer.valueOf(params[2]);
		editor.putInt(USER_ID, userID);

		editor.commit();

	}

	public void resetData() {
		SharedPreferences.Editor editor = prefs.edit();

		editor.remove(ACCESS_TOKEN);
		accessToken = null;

		editor.remove(EXPIRES_IN);
		expiringTime = 0;

		editor.remove(USER_ID);
		userID = 0;

		editor.commit();
	}

}
