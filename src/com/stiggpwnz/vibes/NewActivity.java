package com.stiggpwnz.vibes;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.stiggpwnz.vibes.NewService.ServiceBinder;

public class NewActivity extends Activity implements PlayerListener {

	private static final int UPDATE_PLAYLIST_TIMEOUT_SECONDS = 4;

	private final ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			service = ((ServiceBinder) binder).getService();
			service.setPlayerListener(NewActivity.this);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			service.setPlayerListener(null);
			service = null;
		}
	};

	private NewService service;
	private VibesApplication app;
	private Typeface typeface;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (VibesApplication) getApplication();
	}

	@Override
	protected void onResume() {
		super.onResume();

		Intent intent = new Intent(this, NewService.class);
		bindService(intent, connection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onPause() {
		super.onPause();

		unbindService(connection);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// TODO Auto-generated method stub
	}

	@Override
	public void onPlayerBufferingUpdate(int percent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerStopped() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerProgressUpdate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBufferingStrated() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBufferingEnded() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSongChanged() {
		// TODO Auto-generated method stub

	}

	public Typeface getTypeface() {
		if (typeface == null)
			typeface = Typeface.createFromAsset(getAssets(), "SegoeWP-Semilight.ttf");
		return typeface;
	}

	public void getSongs(String search) throws ClientProtocolException, IOException, VkontakteException {
		Settings settings = app.getSettings();
		Player player = service.getPlayer();
		Vkontakte vkontakte = player.getVkontakte();

		switch (settings.getPlaylist()) {
		case PlayerActivity.SEARCH:
			if (search == null)
				search = settings.getLastSearch();
			player.setSongs(vkontakte.search(search, 0));
			if (player.getSongs() != null) {
				settings.setLastSearch(search);
			}

		case PlayerActivity.MY_AUDIOS:
			player.setSongs(vkontakte.getAudios(settings.getOwnerId(), settings.getAlbumId(), 0, false));

		case PlayerActivity.WALL:
			player.setSongs(vkontakte.getWallAudios(settings.getOwnerId(), 0, false, false));

		case PlayerActivity.NEWSFEED:
			player.setSongs(vkontakte.getNewsFeedAudios(false));

		}
	}

	public int updateSongs() throws ClientProtocolException, IOException, VkontakteException {

		Player player = service.getPlayer();
		Vkontakte vkontakte = player.getVkontakte();
		switch (app.getSettings().getPlaylist()) {
		case PlayerActivity.MY_AUDIOS:
			player.setSongs(vkontakte.getAudios(app.getSettings().getOwnerId(), app.getSettings().getAlbumId(), 0, true));
			break;

		case PlayerActivity.WALL:
			player.setSongs(vkontakte.getWallAudios(app.getSettings().getOwnerId(), 0, false, true));
			break;

		case PlayerActivity.NEWSFEED:
			long prevUpdate = vkontakte.getLastUpdate();
			long thisUpdate = System.currentTimeMillis() / 1000;
			if (thisUpdate - prevUpdate > UPDATE_PLAYLIST_TIMEOUT_SECONDS) {
				Log.d(VibesApplication.VIBES, "updating songs from:" + prevUpdate);
				List<Song> temp = vkontakte.getNewsFeedAudios(true);
				if (temp != null) {
					List<Song> songs = player.getSongs();
					songs.addAll(0, temp);
					vkontakte.getCache().put(vkontakte.getNewsFeedUri(), songs);
					return temp.size();
				}
			}
		}
		return 0;
	}

	public List<Album> getAlbums(int id) throws ClientProtocolException, IOException, VkontakteException {
		return service.getPlayer().getVkontakte().getAlbums(id, 0);
	}

	public List<Unit> getFriends() throws ClientProtocolException, IOException, VkontakteException {
		return service.getPlayer().getVkontakte().getFriends(false);
	}

	public List<Unit> getGroups() throws ClientProtocolException, IOException, VkontakteException {
		return service.getPlayer().getVkontakte().getGroups();
	}

}
