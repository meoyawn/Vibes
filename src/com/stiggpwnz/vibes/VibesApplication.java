package com.stiggpwnz.vibes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.stiggpwnz.vibes.imageloader.ImageLoader;
import com.stiggpwnz.vibes.restapi.Album;
import com.stiggpwnz.vibes.restapi.LastFM;
import com.stiggpwnz.vibes.restapi.Playlist;
import com.stiggpwnz.vibes.restapi.Song;
import com.stiggpwnz.vibes.restapi.Unit;
import com.stiggpwnz.vibes.restapi.VKontakte;
import com.stiggpwnz.vibes.restapi.VKontakteException;
import com.stiggpwnz.vibes.restapi.Playlist.Type;

public class VibesApplication extends Application implements Settings.OnActionListener {

	public static final String VIBES = "vibes";

	public static final int TIMEOUT_CONNECTION = 3000;
	public static final int TIMEOUT_SOCKET = 5000;

	private static final int UPDATE_PLAYLIST_TIMEOUT_SECONDS = 4;

	// common system stuff
	private Settings settings;
	private AbstractHttpClient client;
	private ImageLoader imageLoader;
	private boolean serviceRunning = false;

	// web services
	private VKontakte vkontakte;
	private LastFM lastfm;

	// player data
	private Playlist playlist;
	private ArrayList<Song> songs;

	// cached stuff
	private ArrayList<Unit> friends;
	private ArrayList<Unit> groups;
	private Unit self;
	private Map<Playlist, ArrayList<Song>> playlists;

	// common interface objects
	private View loadingFooter;
	private Typeface font;
	private Animation shake;
	private Playlist selected;

	@Override
	public void onCreate() {
		super.onCreate();
		font = Typeface.createFromAsset(getAssets(), "SegoeWP-Semilight.ttf");
		imageLoader = new ImageLoader(this, R.drawable.music);
		loadingFooter = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer, null);
		shake = AnimationUtils.loadAnimation(this, R.anim.shake);
		client = threadSafeHttpClient();
		settings = new Settings(this, this);
		vkontakte = new VKontakte(settings.getAccessToken(), client, settings.getUserID(), settings.getMaxNews(), settings.getMaxAudio());
		playlists = new HashMap<Playlist, ArrayList<Song>>();
		playlist = new Playlist(Type.NEWSFEED, VIBES);
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		playlists.clear();
		imageLoader.getMemoryCache().clear();
	}

	public VKontakte getVkontakte() {
		return vkontakte;
	}

	public Settings getSettings() {
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

	public LastFM getLastFM() {
		if (lastfm == null) {
			int density = getResources().getDisplayMetrics().densityDpi;
			lastfm = new LastFM(client, settings.getSession(), density);
		}
		return lastfm;
	}

	public Map<Playlist, ArrayList<Song>> getPlaylists() {
		return playlists;
	}

	public ArrayList<Song> loadSongs(Playlist playlist) throws IOException, VKontakteException {
		int ownerId = playlist.unit != null ? playlist.unit.id : 0;
		int albumId = playlist.album != null ? playlist.album.id : 0;

		ArrayList<Song> songs = null;
		switch (playlist.type) {
		case AUDIOS:
			songs = vkontakte.getAudios(ownerId, albumId, 0);
			break;

		case WALL:
			songs = vkontakte.getWallAudios(ownerId, 0, false);
			break;

		case NEWSFEED:
			songs = vkontakte.getNewsFeedAudios(0, 0);
			break;

		case SEARCH:
			songs = vkontakte.search(playlist.search, 0);
			break;
		}

		playlists.put(playlist, songs);
		return songs;
	}

	public ArrayList<Album> loadAlbums(int id) throws ClientProtocolException, IOException, VKontakteException {
		return vkontakte.getAlbums(id, 0);
	}

	public ArrayList<Unit> loadFriends() throws ClientProtocolException, IOException, VKontakteException {
		friends = vkontakte.getFriends(false);
		return friends;
	}

	public ArrayList<Unit> loadGroups() throws ClientProtocolException, IOException, VKontakteException {
		groups = vkontakte.getGroups();
		return groups;
	}

	@Override
	public void onLastFmSessionChanged(String session) {
		getLastFM().setSession(session);
	}

	@Override
	public void onVkontakteAccessTokenChanged(String accessToken) {
		vkontakte.setAccessToken(accessToken);
	}

	@Override
	public void onVkontakteMaxAudiosChanged(int maxAudios) {
		vkontakte.maxAudios = maxAudios;

	}

	@Override
	public void onVkontakteMaxNewsChanged(int maxNews) {
		vkontakte.maxNews = maxNews;
	}

	public Typeface getTypeface() {
		return font;
	}

	public ImageLoader getImageLoader() {
		return imageLoader;
	}

	public ArrayList<Unit> getFriends() {
		return friends;
	}

	public ArrayList<Unit> getGroups() {
		return groups;
	}

	public void setFriends(ArrayList<Unit> friends) {
		this.friends = friends;
	}

	public void setGroups(ArrayList<Unit> groups) {
		this.groups = groups;
	}

	public Unit getSelf() {
		if (self == null) {
			self = new Unit(0, null, null);
			new Thread("Loading self") {
				@Override
				public void run() {
					loadSelf();
				}
			}.start();
		}
		return self;
	}

	private void loadSelf() {
		// TODO Auto-generated method stub
	}

	public View getFooter() {
		return loadingFooter;
	}

	public ArrayList<Song> getSongs() {
		return songs;
	}

	public Animation getShake() {
		return shake;
	}

	public Playlist getPlaylist() {
		return playlist;
	}

	public void setPlaylist(Playlist playlist) {
		this.playlist = playlist;
		songs = playlists.get(playlist);
	}

	public Playlist getSelected() {
		return selected;
	}

	public void setSelected(Playlist selected) {
		this.selected = selected;
	}

}
