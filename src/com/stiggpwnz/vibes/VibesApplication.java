package com.stiggpwnz.vibes;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Application;
import android.util.Log;

import com.stiggpwnz.vibes.restapi.Album;
import com.stiggpwnz.vibes.restapi.LastFM;
import com.stiggpwnz.vibes.restapi.Song;
import com.stiggpwnz.vibes.restapi.Unit;
import com.stiggpwnz.vibes.restapi.Vkontakte;
import com.stiggpwnz.vibes.restapi.VkontakteException;

public class VibesApplication extends Application implements Settings.OnActionListener {

	public static final int TIMEOUT_CONNECTION = 3000;
	public static final int TIMEOUT_SOCKET = 5000;

	public static final String VIBES = "meridian";

	private static final int UPDATE_PLAYLIST_TIMEOUT_SECONDS = 4;

	private Settings settings;
	private AbstractHttpClient client;

	private Vkontakte vkontakte;
	private LastFM lastfm;

	public List<Song> songs;

	private boolean serviceRunning = false;

	@Override
	public void onCreate() {
		super.onCreate();
		client = VibesApplication.threadSafeHttpClient();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		getVkontakte().getCache().clear();
		getLastFM().getCache().clear();
	}

	public Settings getSettings() {
		if (settings == null)
			settings = new Settings(this, this);
		return settings;
	}

	public boolean isServiceRunning() {
		return serviceRunning;
	}

	public void setServiceRunning(boolean serviceRunning) {
		this.serviceRunning = serviceRunning;
	}

	public static AbstractHttpClient threadSafeHttpClient() {
		AbstractHttpClient client = new DefaultHttpClient();
		HttpParams params = client.getParams();
		HttpConnectionParams.setConnectionTimeout(params, TIMEOUT_CONNECTION);
		HttpConnectionParams.setSoTimeout(params, TIMEOUT_SOCKET);

		SchemeRegistry registry = client.getConnectionManager().getSchemeRegistry();

		ClientConnectionManager manager = new ThreadSafeClientConnManager(params, registry);
		client = new DefaultHttpClient(manager, params);
		return client;
	}

	public Vkontakte getVkontakte() {
		if (vkontakte == null)
			vkontakte = new Vkontakte(settings.getAccessToken(), client, settings.getUserID(), settings.getMaxNews(), settings.getMaxAudio());
		return vkontakte;
	}

	public LastFM getLastFM() {
		if (lastfm == null) {
			int density = getResources().getDisplayMetrics().densityDpi;
			lastfm = new LastFM(client, settings.getSession(), density);
		}
		return lastfm;
	}

	public void getSongs(String search) throws ClientProtocolException, IOException, VkontakteException {
		Settings settings = getSettings();
		Vkontakte vkontakte = getVkontakte();

		switch (settings.getPlaylist()) {
		case PlayerActivity.PLAYLIST_SEARCH:
			if (search == null)
				search = settings.getLastSearch();
			songs = vkontakte.search(search, 0);
			if (songs != null) {
				settings.setLastSearch(search);
			}
			break;

		case PlayerActivity.PLAYLIST_MY_AUDIOS:
			songs = vkontakte.getAudios(settings.getOwnerId(), settings.getAlbumId(), 0, false);
			break;

		case PlayerActivity.PLAYLIST_WALL:
			songs = vkontakte.getWallAudios(settings.getOwnerId(), 0, false, false);
			break;

		case PlayerActivity.PLAYLIST_NEWSFEED:
			songs = vkontakte.getNewsFeedAudios(false);
			break;

		}
	}

	public int updateSongs() throws ClientProtocolException, IOException, VkontakteException {
		Vkontakte vkontakte = getVkontakte();
		Settings settings = getSettings();

		switch (settings.getPlaylist()) {
		case PlayerActivity.PLAYLIST_MY_AUDIOS:
			songs = vkontakte.getAudios(settings.getOwnerId(), settings.getAlbumId(), 0, true);
			break;

		case PlayerActivity.PLAYLIST_WALL:
			songs = vkontakte.getWallAudios(settings.getOwnerId(), 0, false, true);
			break;

		case PlayerActivity.PLAYLIST_NEWSFEED:
			long prevUpdate = vkontakte.getLastUpdate();
			long thisUpdate = System.currentTimeMillis() / 1000;
			if (thisUpdate - prevUpdate > UPDATE_PLAYLIST_TIMEOUT_SECONDS) {
				Log.d(VibesApplication.VIBES, "updating songs from:" + prevUpdate);
				List<Song> temp = vkontakte.getNewsFeedAudios(true);
				if (temp != null && temp.size() > 0) {
					songs.addAll(0, temp);
					// removing last cached newsfeed playlist
					synchronized (vkontakte) {
						for (URI uri : vkontakte.getCache().keySet())
							if (uri.toString().contains(Vkontakte.NEWSFEED_GET))
								vkontakte.getCache().remove(uri);
					}
					// and adding a new one
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

	public List<Unit> getFriends() throws ClientProtocolException, IOException, VkontakteException {
		return getVkontakte().getFriends(false);
	}

	public List<Unit> getGroups() throws ClientProtocolException, IOException, VkontakteException {
		return getVkontakte().getGroups();
	}

	@Override
	public void onLastFmSessionChanged(String session) {
		getLastFM().setSession(session);

	}

	@Override
	public void onVkontakteMaxAudiosChanged(int maxAudios) {
		getVkontakte().maxAudios = maxAudios;

	}

	@Override
	public void onVkontakteMaxNewsChanged(int maxNews) {
		getVkontakte().maxNews = maxNews;
	}

}
