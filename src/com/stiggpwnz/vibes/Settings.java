package com.stiggpwnz.vibes;

import com.stiggpwnz.vibes.restapi.LastFM;
import com.stiggpwnz.vibes.restapi.Vkontakte;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {

	public static final String MAX_NEWS = "max news";
	public static final String MAX_AUDIOS = "max audios";
	public static final String REPEAT_PLAYLIST = "repeat";

	private static final String USERNAME = "username";
	private static final String USER_IMAGE = "user image";
	private static final String SESSION = "session";
	private static final String SHUFFLE = "shuffle";
	private static final String PLAYLIST = "playlist";
	private static final String OWNER_ID = "owner id";
	private static final String LAST_SEARCH = "last search";
	private static final String ALBUM_ID = "album id";

	private SharedPreferences prefs;
	private Context context;
	private Vkontakte vkontakte;

	// vkontakte settings
	private String accessToken;
	private long expiringTime;
	private int userID;
	private int maxNews;
	private int maxAudio;

	// last.fm settings
	private String username;
	private String userImage;
	private String session;

	// player settings
	private int playlist = -1;
	private int ownerId = -1;
	private int albumId = -1;
	private String lastSearch;
	private Boolean shuffle;
	private Boolean repeatPlaylist;
	private LastFM lastFM;

	public Settings(Context context) {
		this.context = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public void saveData(String[] params) {
		SharedPreferences.Editor editor = prefs.edit();

		accessToken = params[0];
		editor.putString(Vkontakte.ACCESS_TOKEN, accessToken);

		expiringTime = System.currentTimeMillis() / 1000 + Integer.parseInt(params[1]);
		editor.putLong(Vkontakte.EXPIRES_IN, expiringTime);

		userID = Integer.valueOf(params[2]);
		editor.putInt(Vkontakte.USER_ID, userID);

		editor.commit();

	}

	public void resetData() {
		SharedPreferences.Editor editor = prefs.edit();

		editor.remove(Vkontakte.ACCESS_TOKEN);
		accessToken = null;

		editor.remove(Vkontakte.EXPIRES_IN);
		expiringTime = 0;

		editor.remove(Vkontakte.USER_ID);
		userID = 0;

		editor.commit();
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

		if (lastFM != null)
			lastFM.setSession(session);
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

		if (lastFM != null)
			lastFM.setSession(session);
	}

	public String getAccessToken() {
		if (accessToken == null)
			accessToken = prefs.getString(Vkontakte.ACCESS_TOKEN, null);
		return accessToken;
	}

	public long getExpiringTime() {
		if (expiringTime == 0)
			expiringTime = prefs.getLong(Vkontakte.EXPIRES_IN, 0);
		return expiringTime;
	}

	public int getUserID() {
		if (userID == 0)
			userID = prefs.getInt(Vkontakte.USER_ID, 0);
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

	public int getPlaylist() {
		if (playlist == -1)
			playlist = prefs.getInt(PLAYLIST, NewActivity.NEWSFEED);
		return playlist;
	}

	public void setPlaylist(int number) {
		if (number != playlist) {
			playlist = number;
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt(PLAYLIST, number);
			editor.commit();
		}
	}

	public int getOwnerId() {
		if (ownerId == -1)
			ownerId = prefs.getInt(OWNER_ID, 0);
		return ownerId;
	}

	public void setOwnerId(int number) {
		if (ownerId != number) {
			ownerId = number;
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt(OWNER_ID, number);
			editor.commit();
		}
	}

	public int getAlbumId() {
		if (albumId == -1)
			albumId = prefs.getInt(ALBUM_ID, 0);
		return albumId;
	}

	public void setAlbumId(int album) {
		if (album != this.albumId) {
			this.albumId = album;
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt(ALBUM_ID, album);
			editor.commit();
		}
	}

	public String getLastSearch() {
		if (lastSearch == null)
			lastSearch = prefs.getString(LAST_SEARCH, null);
		return lastSearch;
	}

	public void setLastSearch(String search) {
		if (lastSearch == null || !lastSearch.equals(search)) {
			lastSearch = search;
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(LAST_SEARCH, search);
			editor.commit();
		}
	}

	public int getMaxNews() {
		if (maxNews == 0)
			maxNews = Integer.valueOf(prefs.getString(MAX_NEWS, context.getString(R.string.default_max_news)));
		return maxNews;
	}

	public void updateMaxNews() {
		maxNews = Integer.valueOf(prefs.getString(MAX_NEWS, context.getString(R.string.default_max_news)));
		if (vkontakte != null)
			vkontakte.maxNews = maxNews;
	}

	public void setVkontakte(Vkontakte vkontakte) {
		this.vkontakte = vkontakte;
	}

	public void setLastFM(LastFM lastFM) {
		this.lastFM = lastFM;
	}

	public int getMaxAudio() {
		if (maxAudio == 0)
			maxAudio = Integer.valueOf(prefs.getString(MAX_AUDIOS, context.getString(R.string.default_max_audio)));
		return maxAudio;
	}

	public void updateMaxAudio() {
		maxAudio = Integer.valueOf(prefs.getString(MAX_AUDIOS, context.getString(R.string.default_max_audio)));
		if (vkontakte != null)
			vkontakte.maxAudios = maxAudio;
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

}
