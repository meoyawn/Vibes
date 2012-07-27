package com.stiggpwnz.vibes;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Application;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.Log;

public class VibesApplication extends Application {

	public static final String VIBES = "meridian";

	public static final int NOTIFICATION = 49;
	public static final String SONG = "song";

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

	private static final int UPDATE_PLAYLIST_TIMEOUT_SECONDS = 4;

	public int currentSong;
	public List<Song> songs;
	public Song current;

	private DefaultHttpClient client;
	private SharedPreferences prefs;
	private Typeface typeface;

	private Vkontakte vkontakte;
	private LastFM lastfm;
	private NotificationManager notificationManager;

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

	public long lastUpdate;

	@Override
	public void onCreate() {
		super.onCreate();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		client = new DefaultHttpClient();
		ClientConnectionManager mgr = client.getConnectionManager();
		HttpParams params = client.getParams();
		client = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);

		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used.
		int timeoutConnection = 3000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT)
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 5000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		client.setParams(httpParameters);

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

	}

	/*
	 * VKONTAKTE STUFF
	 */

	public void saveData(String[] params) {
		SharedPreferences.Editor editor = prefs.edit();

		accessToken = params[0];
		editor.putString(ACCESS_TOKEN, accessToken);

		expiringTime = System.currentTimeMillis() / 1000 + Integer.parseInt(params[1]);
		editor.putLong(EXPIRES_IN, expiringTime);

		userID = Integer.valueOf(params[2]);
		editor.putInt(USER_ID, userID);

		editor.commit();

		if (getVkontakte().getAccesToken() == null)
			getVkontakte().setAccesToken(accessToken);
	}

	public void resetData() {
		songs = null;

		SharedPreferences.Editor editor = prefs.edit();

		editor.remove(ACCESS_TOKEN);
		accessToken = null;

		editor.remove(EXPIRES_IN);
		expiringTime = 0;

		editor.remove(USER_ID);
		userID = 0;

		editor.commit();
		vkontakte = null;
	}

	/*
	 * LAST FM STUFF
	 */

	public void saveLastFM(String[] params) {
		SharedPreferences.Editor editor = prefs.edit();

		username = params[0];
		editor.putString(USERNAME, username);

		session = params[1];
		editor.putString(SESSION, session);

		userImage = params[2];
		editor.putString(USER_IMAGE, userImage);

		editor.commit();
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
		lastfm = null;
	}

	/*
	 * PLAYLISTS STUFF
	 */

	public boolean getSongs(String search) throws ClientProtocolException, IOException, VkontakteException {
		switch (getPlaylist()) {

		case PlayerActivity.SEARCH:
			if (search == null)
				search = getLastSearch();
			if (search == null)
				Log.d(VIBES, "starting null search");
			Log.d(VIBES, "starting search from application");
			songs = getVkontakte().search(search, 0);
			if (songs == null)
				return false;
			else {
				Log.d(VIBES, "setting last search");
				setLastSearch(search);
				return true;
			}

		case PlayerActivity.MY_AUDIOS:
			songs = getVkontakte().getAudios(getOwnerId(), getAlbumId(), 0, false);
			if (songs == null)
				return false;
			else
				return true;

		case PlayerActivity.WALL:
			songs = getVkontakte().getWallAudios(getOwnerId(), 0, false, false);
			if (songs == null)
				return false;
			else
				return true;

		case PlayerActivity.NEWSFEED:
			songs = getVkontakte().getNewsFeedAudios(false);
			if (songs == null)
				return false;
			else
				return true;
		}
		return false;
	}

	public int updateSongs() throws ClientProtocolException, IOException, VkontakteException {

		final Vkontakte vkontakte = getVkontakte();
		switch (getPlaylist()) {
		case PlayerActivity.MY_AUDIOS:
			songs = vkontakte.getAudios(getOwnerId(), getAlbumId(), 0, true);
			break;

		case PlayerActivity.WALL:
			songs = vkontakte.getWallAudios(getOwnerId(), 0, false, true);
			break;

		case PlayerActivity.NEWSFEED:
			long prevUpdate = vkontakte.getLastUpdate();
			long thisUpdate = System.currentTimeMillis() / 1000;
			if (thisUpdate - prevUpdate > UPDATE_PLAYLIST_TIMEOUT_SECONDS) {
				Log.d(VibesApplication.VIBES, "updating songs from:" + prevUpdate);
				List<Song> temp = vkontakte.getNewsFeedAudios(true);
				if (temp != null) {
					songs.addAll(0, temp);
					vkontakte.getCache().put(vkontakte.getNewsFeedUri(), songs);
					return temp.size();
				}
			}
		}
		return 0;
	}

	public List<Album> getAlbums(int id) throws ClientProtocolException, IOException, VkontakteException {
		return getVkontakte().getAlbums(id, 0);
	}

	/*
	 * FRIENDS STUFF
	 */

	public List<Unit> getFriends() throws ClientProtocolException, IOException, VkontakteException {
		return getVkontakte().getFriends(false);
	}

	public List<Unit> getGroups() throws ClientProtocolException, IOException, VkontakteException {
		return getVkontakte().getGroups();
	}

	/*
	 * SYSTEM STUFF
	 */

	public void closeIdleConnections() {
		client.getConnectionManager().closeIdleConnections(1, TimeUnit.MILLISECONDS);
	}

	public void closeAllConnections() {
		client.getConnectionManager().shutdown();
	}

	/*
	 * GETTERS AND SETTERS
	 */

	public Typeface getTypeface() {
		if (typeface == null)
			typeface = Typeface.createFromAsset(getAssets(), "SegoeWP-Semilight.ttf");
		return typeface;
	}

	public Vkontakte getVkontakte() {
		if (vkontakte == null)
			vkontakte = new Vkontakte(getAccessToken(), client, getUserID(), getMaxNews(), getMaxAudio());
		return vkontakte;
	}

	public LastFM getLastFM() {
		if (lastfm == null) {
			int density = getResources().getDisplayMetrics().densityDpi;
			lastfm = new LastFM(client, getSession(), density);
		}
		return lastfm;
	}

	public String getAccessToken() {
		if (accessToken == null)
			accessToken = prefs.getString(ACCESS_TOKEN, null);
		return accessToken;
	}

	public long getExpiringTime() {
		if (expiringTime == 0)
			expiringTime = prefs.getLong(EXPIRES_IN, 0);
		return expiringTime;
	}

	public int getUserID() {
		if (userID == 0)
			userID = prefs.getInt(USER_ID, 0);
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
			playlist = prefs.getInt(PLAYLIST, PlayerActivity.NEWSFEED);
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
		if (owner == -1)
			owner = prefs.getInt(OWNER, 0);
		return owner;
	}

	public void setOwner(int number) {
		if (owner != number) {
			owner = number;
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt(OWNER, number);
			editor.commit();
		}
	}

	public int getAlbumId() {
		if (album == -1)
			album = prefs.getInt(ALBUM, 0);
		return album;
	}

	public void setAlbum(int album) {
		if (album != this.album) {
			this.album = album;
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt(ALBUM, album);
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
			maxNews = Integer.valueOf(prefs.getString(MAX_NEWS, getString(R.string.default_max_news)));
		return maxNews;
	}

	public void updateMaxNews() {
		maxNews = Integer.valueOf(prefs.getString(MAX_NEWS, getString(R.string.default_max_news)));
		getVkontakte().maxNews = maxNews;
	}

	private int getMaxAudio() {
		if (maxAudio == 0)
			maxAudio = Integer.valueOf(prefs.getString(MAX_AUDIO, getString(R.string.default_max_audio)));
		return maxAudio;
	}

	public void updateMaxAudio() {
		maxAudio = Integer.valueOf(prefs.getString(MAX_AUDIO, getString(R.string.default_max_audio)));
		getVkontakte().maxAudios = maxAudio;
	}

	public Song getCurrentSong() {
		if (current != null)
			return current;
		if (songs != null && songs.size() > 0)
			return songs.get(currentSong);
		return null;
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

	public NotificationManager getNotificationManager() {
		return notificationManager;
	}

}
