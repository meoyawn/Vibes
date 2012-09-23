package com.stiggpwnz.vibes;

import java.io.IOException;
import java.util.ArrayList;

import net.simonvt.widget.MenuDrawer;
import net.simonvt.widget.MenuDrawerManager;

import org.apache.http.client.ClientProtocolException;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.stiggpwnz.vibes.Player.State;
import com.stiggpwnz.vibes.PlayerService.ServiceBinder;
import com.stiggpwnz.vibes.adapters.FragmentPagesAdapter;
import com.stiggpwnz.vibes.adapters.PlaylistAdapter;
import com.stiggpwnz.vibes.fragments.ControlsFragment;
import com.stiggpwnz.vibes.fragments.PlaylistFragment;
import com.stiggpwnz.vibes.fragments.StartingFragment;
import com.stiggpwnz.vibes.fragments.UnitFragment;
import com.stiggpwnz.vibes.fragments.UnitsFragment;
import com.stiggpwnz.vibes.imageloader.ImageLoader;
import com.stiggpwnz.vibes.restapi.Album;
import com.stiggpwnz.vibes.restapi.LastFM;
import com.stiggpwnz.vibes.restapi.Playlist;
import com.stiggpwnz.vibes.restapi.Song;
import com.stiggpwnz.vibes.restapi.Unit;
import com.stiggpwnz.vibes.restapi.VKontakteException;

public class PlayerActivity extends SherlockFragmentActivity implements StartingFragment.Listener, UnitsFragment.Listener, PlaylistFragment.Listener, ControlsFragment.Listener,
		Player.Listener, OnClickListener {

	private static final String ICON = "icon";
	private static final String TITLE = "title";
	private static final String START = "init";
	private static final String UNITS = "units";
	private static final String UNIT = "unit";

	private final ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			service = ((ServiceBinder) binder).getService();
			service.getPlayer().setListener(PlayerActivity.this);
			bound = true;
			service.cancelNotification();
			service.stopWaiter();
			onNewTrack();
			Log.d(VibesApplication.VIBES, "service bound");
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {

		}
	};

	private PlayerService service;
	private VibesApplication app;

	private PlaylistFragment playlistFragment;
	private ControlsFragment controlsFragment;

	private boolean bound;
	private boolean playlistLoading;
	private ViewPager pager;
	private View play;
	private MenuDrawerManager mMenuDrawer;
	private String title;
	private String icon;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(VibesApplication.VIBES, "onCreate activity");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);
		FragmentManager supportFragmentManager = getSupportFragmentManager();
		Fragment startFragment = supportFragmentManager.findFragmentByTag(START);
		if (findViewById(R.id.framePlaylists) != null) {
			controlsFragment = (ControlsFragment) supportFragmentManager.findFragmentById(R.id.fragment_controls);
			playlistFragment = (PlaylistFragment) supportFragmentManager.findFragmentById(R.id.fragmentPlaylist);
			if (startFragment == null) {
				if (getApp().getSelected() == null)
					playlistFragment.loadPlaylist(isPlaying());
			}
		} else {
			mMenuDrawer = new MenuDrawerManager(this, MenuDrawer.MENU_DRAG_CONTENT);
			mMenuDrawer.setContentView(R.layout.activity_player);
			mMenuDrawer.setMenuView(R.layout.side_menu);

			getSupportActionBar().setDisplayHomeAsUpEnabled(true);

			FragmentPagesAdapter adapter = new FragmentPagesAdapter(supportFragmentManager);
			pager = (ViewPager) findViewById(R.id.pager);
			pager.setAdapter(adapter);

			play = findViewById(R.id.btnPlay);
			play.setOnClickListener(this);

			findViewById(R.id.btnFwd).setOnClickListener(this);
			findViewById(R.id.btnRwd).setOnClickListener(this);
			findViewById(R.id.btnPlaylist).setOnClickListener(this);
		}

		if (startFragment == null)
			initFragments();

		if (savedInstanceState != null) {
			title = savedInstanceState.getString(TITLE);
			icon = savedInstanceState.getString(ICON);
		} else {
			title = getApp().getPlaylist().name;
			icon = getApp().getPlaylist().unit.photo;
		}
		setTitleAndIcon();
	}

	@Override
	protected void onResume() {
		super.onResume();
		doBindService();
	}

	private void doBindService() {
		Intent intent = new Intent(this, PlayerService.class);
		if (!getApp().isServiceRunning()) {
			Log.d(VibesApplication.VIBES, "starting service");
			startService(intent);
		}
		if (!bound) {
			Log.d(VibesApplication.VIBES, "binding service");
			bindService(intent, connection, 0);
		}
	}

	private void doUnbindService() {
		if (bound) {
			State state = service.getPlayer().getState();
			if (state == State.PLAYING)
				service.makeNotification();
			else if (state == State.NOT_PREPARED || state == State.PAUSED || state == State.PREPARING_FOR_IDLE || state == State.SEEKING_FOR_IDLE)
				service.startWaiter();
			service.getPlayer().setListener(null);
			unbindService(connection);
			service = null;
			bound = false;
			Log.d(VibesApplication.VIBES, "service unbound");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		doUnbindService();
	}

	@Override
	public void onBackPressed() {
		if (mMenuDrawer != null) {
			final int drawerState = mMenuDrawer.getDrawerState();
			if (getSupportFragmentManager().findFragmentByTag(START).isVisible() && (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING)) {
				mMenuDrawer.closeMenu();
				return;
			} else if (!getSupportFragmentManager().findFragmentByTag(START).isVisible() && (drawerState == MenuDrawer.STATE_CLOSED || drawerState == MenuDrawer.STATE_CLOSING)) {
				mMenuDrawer.openMenu();
				return;
			}
		}
		super.onBackPressed();
	}

	public void nullEverything() {
		controlsFragment.nullEverything();
		playlistFragment.nullEverything();
	}

	private void setCurrentSong() {
		final Player player = service.getPlayer();
		final Song currentSong = player.getCurrentSong();
		final State state = player.getState();

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				controlsFragment.setCurrentSong(currentSong, getApp().getLastFM().getImageRequestQueue());
				playlistFragment.setCurrentSong(getApp().getPlaylist(), player.currentTrack);

				if (currentSong != null) {
					if (state == State.PLAYING || state == State.PAUSED) {
						onBufferingEnded(player.getSongDuration());
						onProgressChanged(player.getCurrentPosition());
						if (state == State.PLAYING)
							setPlayButtonDrawable(R.drawable.pause);
						else
							setPlayButtonDrawable(R.drawable.play);
					} else if (state == State.PREPARING_FOR_IDLE || state == State.SEEKING_FOR_IDLE) {
						onBufferingStrated();
						setPlayButtonDrawable(R.drawable.play);
						if (state == State.PREPARING_FOR_IDLE)
							nullEverything();
					} else if (state == State.PREPARING_FOR_PLAYBACK || state == State.SEEKING_FOR_PLAYBACK || state == State.NEXT_FOR_PLAYBACK) {
						onBufferingStrated();
						setPlayButtonDrawable(R.drawable.pause);
						if (state == State.PREPARING_FOR_PLAYBACK || state == State.NEXT_FOR_PLAYBACK)
							nullEverything();
					} else if (state == State.NOT_PREPARED) {
						onBufferingEnded(0);
						nullEverything();
						setPlayButtonDrawable(R.drawable.play);
					}
				}
			}
		});
	}

	private void setPlayButtonDrawable(int resource) {
		controlsFragment.setPlayButtonDrawable(resource);
		if (play != null)
			play.setBackgroundResource(resource);
	}

	private void initFragments() {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
		transaction.add(R.id.framePlaylists, StartingFragment.newInstance(this, getApp().getSelf(), getApp().getSelected()), START);
		transaction.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.playermenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemLogOut:
			logOut(true);
			return true;

		case android.R.id.home:
			mMenuDrawer.toggleMenu();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void logOut(boolean logout) {
		getApp().getSettings().resetVKontakte();
		doUnbindService();
		stopService(new Intent(this, PlayerService.class));
		Intent intent = new Intent(this, LoginActivity.class);
		intent.putExtra(LoginActivity.RESET, logout);
		startActivity(intent);
		finish();
	}

	@Override
	public void unknownError() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (service != null)
					service.getPlayer().stop();
				Toast.makeText(PlayerActivity.this, getString(R.string.unknownError), Toast.LENGTH_LONG).show();
			}
		});
	}

	@Override
	public void internetFail() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (service != null)
					service.getPlayer().stop();
				Toast.makeText(PlayerActivity.this, getString(R.string.no_internet), Toast.LENGTH_LONG).show();
			}
		});
	}

	@Override
	public void authFail() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(PlayerActivity.this, getString(R.string.authProblem), Toast.LENGTH_LONG).show();
				logOut(false);
			}
		});
	}

	@Override
	public void accessDenied() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(PlayerActivity.this, getString(R.string.access_denied), Toast.LENGTH_LONG).show();
			}
		});
	}

	@Override
	public Typeface getTypeface() {
		return getApp().getTypeface();
	}

	@Override
	public ImageLoader getImageLoader() {
		return getApp().getImageLoader();
	}

	@Override
	public void showUnits(boolean friends) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
		UnitsFragment unitsFragment = UnitsFragment.newInstance(friends, friends ? getApp().getFriends() : getApp().getGroups());
		transaction.replace(R.id.framePlaylists, unitsFragment, UNITS);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
	public void showUnit(Unit unit) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
		transaction.replace(R.id.framePlaylists, UnitFragment.newInstance(unit, getApp().getSelected()), UNIT);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
	public ArrayList<Unit> loadUnits(boolean friends) throws ClientProtocolException, IOException, VKontakteException {
		if (getApp() != null) {
			if (friends)
				return getApp().loadFriends();
			else
				return getApp().loadGroups();
		}
		return null;
	}

	@Override
	public ArrayList<Album> loadAlbums(Unit unit) throws ClientProtocolException, IOException, VKontakteException {
		if (unit != null)
			return getApp().loadAlbums(unit.id);
		else
			return getApp().loadAlbums(0);
	}

	@Override
	public View getFooterView() {
		return getApp().getFooter();
	}

	@Override
	public void loadPlaylist(Playlist playlist) {
		if (!app.getSelected().equals(playlist)) {
			getApp().setSelected(playlist);
			playlistFragment.loadPlaylist(isPlaying());
		}
		if (pager != null) {
			mMenuDrawer.closeMenu(true);
			pager.setCurrentItem(1, true);
		}
	}

	@Override
	public ArrayList<Song> loadSongs(Playlist playlist) throws IOException, VKontakteException {
		return getApp().loadSongs(playlist);
	}

	@Override
	public Unit getSelf() {
		return getApp().getSelf();
	}

	@Override
	public void onBufferingStrated() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				playlistFragment.onBufferingStarted();
				controlsFragment.onBufferingStarted();
			}
		});
	}

	@Override
	public void onProgressChanged(int progress) {
		int songDuration = service.getPlayer().getSongDuration();
		controlsFragment.onProgressChanged(progress, songDuration);
		playlistFragment.onProgressChanged(progress, songDuration);
	}

	@Override
	public void onBufferingUpdate(int percent) {
		controlsFragment.updateBuffering(percent);
	}

	@Override
	public void onAuthProblem() {
		authFail();
	}

	@Override
	public void onInternetProblem() {
		internetFail();
	}

	@Override
	public void onNewTrack() {
		if (service != null && controlsFragment != null && playlistFragment != null)
			setCurrentSong();
	}

	@Override
	public void play(int position, boolean hardReset) {
		setPlayButtonDrawable(R.drawable.pause);
		service.getPlayer().play(position, hardReset);
		setCurrentSong();
	}

	@Override
	public void download(int position) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loveUnlove(int position) {
		Song song = getApp().getSongs().get(position);

		if (song != null) {
			if (song.loved)
				controlsFragment.unlove(song);
			else
				controlsFragment.love(song);
		}
	}

	@Override
	public void remove(int position) {
		Player player = service.getPlayer();
		PlaylistAdapter adapter = playlistFragment.getAdapter();
		if (player.currentTrack == position) {
			player.next();
		} else if (player.currentTrack > position) {
			player.currentTrack--;
			adapter.currentTrack--;
		}
		getApp().getSongs().remove(position);
		adapter.notifyDataSetChanged();
		if (getApp().getSettings().getShuffle())
			player.generateShuffleQueue();
	}

	@Override
	public String getAlbumImageUrl() {
		Song currentSong = service.getPlayer().getCurrentSong();
		if (currentSong != null) {
			if (currentSong.albumImageUrl != null) {
				if (currentSong.albumImageUrl.equals(LastFM.EMPTY))
					return null;
				else
					return currentSong.albumImageUrl;
			} else
				return getApp().getLastFM().getAndSetAlbumImageUrl(currentSong);
		}
		return null;
	}

	@Override
	public void onBufferingEnded(final int duration) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				controlsFragment.onBufferingEnded(duration);
				playlistFragment.onBufferingEnded();
			}
		});
	}

	@Override
	public void onPlayButtonPressed(View v) {
		v.startAnimation(getApp().getShake());
		Player player = service.getPlayer();
		State state = player.getState();
		if (state == State.PAUSED || state == State.PREPARING_FOR_IDLE || state == State.SEEKING_FOR_IDLE) {
			player.resume();
			setPlayButtonDrawable(R.drawable.pause);
		} else if (state == State.PLAYING || state == State.PREPARING_FOR_PLAYBACK || state == State.SEEKING_FOR_PLAYBACK) {
			player.pause();
			setPlayButtonDrawable(R.drawable.play);
		} else if (state == State.NOT_PREPARED && player.getCurrentSong() != null) {
			player.play();
			setPlayButtonDrawable(R.drawable.pause);
		}
	}

	@Override
	public void onNextButtonPressed(View v) {
		v.startAnimation(getApp().getShake());
		service.getPlayer().next();
	}

	@Override
	public void onPrevButtonPressed(View v) {
		v.startAnimation(getApp().getShake());
		service.getPlayer().prev();
	}

	@Override
	public void onShuffleButtonPressed(View v) {
		Player player = service.getPlayer();
		Settings settings = getApp().getSettings();
		if (settings.getShuffle()) {
			v.setBackgroundResource(R.drawable.shuffle_grey);
			settings.setShuffle(false);
		} else {
			v.setBackgroundResource(R.drawable.shuffle_blue);
			settings.setShuffle(true);
			player.generateShuffleQueue();
		}
	}

	@Override
	public void onRepeatButtonPressed(View v) {
		Player player = service.getPlayer();
		if (player.isLooping()) {
			v.setBackgroundResource(R.drawable.repeat_grey);
			player.setLooping(false);
		} else {
			v.setBackgroundResource(R.drawable.repeat_blue);
			player.setLooping(true);
		}
	}

	@Override
	public int add(Song song) throws IOException, VKontakteException {
		return getApp().getVkontakte().add(song);
	}

	@Override
	public boolean delete(Song song) throws IOException, VKontakteException {
		return getApp().getVkontakte().delete(song);
	}

	@Override
	public void love(Song song) {
		if (getApp().getSettings().getSession() != null)
			getApp().getLastFM().love(song);
	}

	@Override
	public void unlove(Song song) {
		if (getApp().getSettings().getSession() != null)
			getApp().getLastFM().unlove(song);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			State state = service.getPlayer().getState();
			if (state == State.PLAYING || state == State.PAUSED || state == State.SEEKING_FOR_IDLE || state == State.SEEKING_FOR_PLAYBACK) {
				onBufferingStrated();
				service.getPlayer().seekTo(progress);
			} else
				seekBar.setProgress(0);
		}
	}

	@Override
	public void onPlaylistLoaded() {
		onNewTrack();
	}

	@Override
	public Playlist getPlaylist() {
		return getApp().getPlaylist();
	}

	@Override
	public void setPlaylist(Playlist playlist) {
		getApp().setPlaylist(playlist);
		if (getApp().getSettings().getShuffle() && service != null)
			service.getPlayer().generateShuffleQueue();
		title = playlist.name;
		icon = playlist.unit.photo;
		setTitleAndIcon();
	}

	private void setTitleAndIcon() {
		if (title != null)
			getSupportActionBar().setTitle(title);
		new LogoLoader().execute(icon);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(TITLE, title);
		outState.putString(ICON, icon);
	}

	private class LogoLoader extends AsyncTask<String, Void, Drawable> {

		@Override
		protected Drawable doInBackground(String... params) {
			if (params[0] != null)
				return new BitmapDrawable(getResources(), getApp().getImageLoader().getBitmap(params[0]));
			return null;
		}

		@Override
		protected void onPostExecute(Drawable result) {
			super.onPostExecute(result);
			if (result != null)
				getSupportActionBar().setIcon(result);
		}

	}

	@Override
	public int getCurrentTrack() {
		return service.getPlayer().currentTrack;
	}

	@Override
	public void setPlaylistLoading(boolean loading) {
		playlistLoading = loading;
	}

	@Override
	public boolean isPlaylistLoading() {
		return playlistLoading;
	}

	@Override
	public boolean getShuffle() {
		return getApp().getSettings().getShuffle();
	}

	@Override
	public void onViewCreated(Fragment fragment) {
		if (pager != null) {
			if (fragment instanceof ControlsFragment) {
				controlsFragment = (ControlsFragment) fragment;
			} else if (fragment instanceof PlaylistFragment) {
				playlistFragment = (PlaylistFragment) fragment;
				playlistFragment.loadPlaylist(isPlaying());
				pager.setCurrentItem(1, true);
			}
			onNewTrack();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnPlay:
			onPlayButtonPressed(v);
			break;

		case R.id.btnFwd:
			onNextButtonPressed(v);
			break;

		case R.id.btnRwd:
			onPrevButtonPressed(v);
			break;

		case R.id.btnPlaylist:
			initFragments();
			break;
		}
	}

	@Override
	public ArrayList<Song> getSongs(Playlist playlist) {
		return getApp().getPlaylists().get(playlist);
	}

	@Override
	public boolean isPlaying() {
		return service != null ? service.getPlayer().getState() != State.NOT_PREPARED : false;
	}

	@Override
	public Playlist getSelectedPlaylist() {
		return getApp().getSelected();

	}

	@Override
	public ArrayList<Unit> getUnits(boolean friends) {
		return friends ? getApp().getFriends() : getApp().getGroups();
	}

	private VibesApplication getApp() {
		if (app == null)
			app = (VibesApplication) getApplication();
		return app;
	}

}
