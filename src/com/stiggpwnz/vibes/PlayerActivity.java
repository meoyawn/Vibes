package com.stiggpwnz.vibes;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.stiggpwnz.vibes.Player.PlayerListener;
import com.stiggpwnz.vibes.Player.State;
import com.stiggpwnz.vibes.PlayerService.ServiceBinder;
import com.stiggpwnz.vibes.adapters.FragmentPagesAdapter;
import com.stiggpwnz.vibes.adapters.PlaylistAdapter;
import com.stiggpwnz.vibes.fragments.AlbumsFragment;
import com.stiggpwnz.vibes.fragments.ControlsFragment;
import com.stiggpwnz.vibes.fragments.ControlsFragment.ControlsListener;
import com.stiggpwnz.vibes.fragments.PlaylistFragment;
import com.stiggpwnz.vibes.fragments.PlaylistFragment.PlaylistListener;
import com.stiggpwnz.vibes.fragments.SleepingTimerFragment;
import com.stiggpwnz.vibes.fragments.SleepingTimerFragment.SleepingTimerListener;
import com.stiggpwnz.vibes.fragments.StartingFragment;
import com.stiggpwnz.vibes.fragments.StartingFragment.StartingListener;
import com.stiggpwnz.vibes.fragments.TutorialFragment;
import com.stiggpwnz.vibes.fragments.TutorialFragment.TutorialListener;
import com.stiggpwnz.vibes.fragments.UnitFragment;
import com.stiggpwnz.vibes.fragments.UnitsListFragment;
import com.stiggpwnz.vibes.fragments.UnitsListFragment.UnitsListListener;
import com.stiggpwnz.vibes.restapi.Album;
import com.stiggpwnz.vibes.restapi.LastFM;
import com.stiggpwnz.vibes.restapi.Playlist;
import com.stiggpwnz.vibes.restapi.Playlist.Type;
import com.stiggpwnz.vibes.restapi.Song;
import com.stiggpwnz.vibes.restapi.Unit;
import com.stiggpwnz.vibes.restapi.VKontakteException;

public class PlayerActivity extends SherlockFragmentActivity implements StartingListener, UnitsListListener, PlaylistListener, ControlsListener,
		PlayerListener, OnClickListener, OnDrawerStateChangeListener, TutorialListener, SleepingTimerListener {

	private static final String STARTING_FRAGMENT = "starting fragment";
	public static final String UNIT_FRAGMENT = "unit fragment";

	// system stuff
	private PlayerService service;
	private VibesApplication app;

	// fragments
	private PlaylistFragment playlistFragment;
	private ControlsFragment controlsFragment;

	// portrait mode gui
	private MenuDrawerManager menuDrawer;
	private ViewPager fragmentPager;
	private Button playButton;

	// utils
	private boolean playlistIsLoading;
	private Animation shake;
	private Typeface typeface;

	public Animation getShakeAnimation() {
		if (shake == null)
			shake = AnimationUtils.loadAnimation(this, R.anim.shake);
		return shake;
	}



	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);
		FragmentManager fragmentManager = getSupportFragmentManager();
		setSupportProgressBarIndeterminateVisibility(false);

		if (findViewById(R.id.framePlaylists) != null) {
			controlsFragment = (ControlsFragment) fragmentManager.findFragmentById(R.id.fragment_controls);
			playlistFragment = (PlaylistFragment) fragmentManager.findFragmentById(R.id.fragmentPlaylist);
		} else {
			menuDrawer = new MenuDrawerManager(this, MenuDrawer.MENU_DRAG_CONTENT);
			menuDrawer.setContentView(R.layout.activity_player);
			menuDrawer.setMenuView(R.layout.side_menu);
			menuDrawer.getMenuDrawer().setOnDrawerStateChangeListener(this);

			getSupportActionBar().setDisplayHomeAsUpEnabled(true);

			FragmentPagesAdapter adapter = new FragmentPagesAdapter(fragmentManager);
			fragmentPager = (ViewPager) findViewById(R.id.pager);
			fragmentPager.setAdapter(adapter);

			playButton = (Button) findViewById(R.id.btnPlay);
			playButton.setOnClickListener(this);

			findViewById(R.id.btnFwd).setOnClickListener(this);
			findViewById(R.id.btnRwd).setOnClickListener(this);
		}

		if (fragmentManager.findFragmentByTag(STARTING_FRAGMENT) == null)
			initNavigationMenu();

		setTitleAndIcon();
	}

	@Override
	public void onDrawerStateChange(int oldState, int newState) {
		getSupportActionBar().setDisplayHomeAsUpEnabled(newState == MenuDrawer.STATE_CLOSED);
	}

	@Override
	protected void onResume() {
		super.onResume();
		doBindService();
	}

	private void doBindService() {
		Intent intent = new Intent(this, PlayerService.class);
		if (!PlayerService.isRunning) {
			Log.d(VibesApplication.VIBES, "starting service");
			startService(intent);
		}
		if (service == null) {
			Log.d(VibesApplication.VIBES, "binding service");
			bindService(intent, connection, 0);
		}
	}

	private final ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			service = ((ServiceBinder) binder).getService();
			service.getPlayer().setListener(PlayerActivity.this);
			if (getApp().getSettings().getShuffle())
				service.getPlayer().generateShuffleQueue();
			service.cancelSongNotification();
			service.stopWaiter();
			onNewTrack();
			Log.d(VibesApplication.VIBES, "service bound");
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {

		}
	};

	private void doUnbindService() {
		if (service != null) {
			State state = service.getPlayer().getState();
			if (state == State.PLAYING)
				service.showSongNotification();
			else if (state == State.NOT_PREPARED || state == State.PAUSED || state == State.PREPARING_FOR_IDLE || state == State.SEEKING_FOR_IDLE)
				service.startWaiter();
			service.getPlayer().setListener(null);
			unbindService(connection);
			service = null;
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
		if (menuDrawer != null) {
			int drawerState = menuDrawer.getDrawerState();
			Fragment startingFragment = getSupportFragmentManager().findFragmentByTag(STARTING_FRAGMENT);
			if (startingFragment.isVisible() && (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING)) {
				menuDrawer.closeMenu();
				return;
			} else if (!startingFragment.isVisible() && (drawerState == MenuDrawer.STATE_CLOSED || drawerState == MenuDrawer.STATE_CLOSING)) {
				menuDrawer.openMenu();
				return;
			}
		}
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		getApp().getImageLoader().getMemoryCache().clear();
		super.onDestroy();
	}

	public void nullEverything(State state) {
		controlsFragment.nullEverything();
		playlistFragment.nullEverything(state);
	}

	private void setCurrentSong() {
		Log.d(VibesApplication.VIBES, "setting current song");
		final Player player = service.getPlayer();
		final Song currentSong = player.getCurrentSong();
		final State state = player.getState();

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				controlsFragment.setCurrentSong(currentSong, getApp().getLastFM().getImageRequestQueue());
				playlistFragment.setCurrentSong(player.currentTrack);

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
							nullEverything(state);
					} else if (state == State.PREPARING_FOR_PLAYBACK || state == State.SEEKING_FOR_PLAYBACK || state == State.NEXT_FOR_PLAYBACK) {
						onBufferingStrated();
						setPlayButtonDrawable(R.drawable.pause);
						if (state == State.PREPARING_FOR_PLAYBACK || state == State.NEXT_FOR_PLAYBACK)
							nullEverything(state);
					} else if (state == State.NOT_PREPARED) {
						onBufferingEnded(0);
						nullEverything(state);
						setPlayButtonDrawable(R.drawable.play);
					}
				}
			}
		});
	}

	private void setPlayButtonDrawable(int resource) {
		controlsFragment.setPlayButtonDrawable(resource);
		if (playButton != null) {
			playButton.setBackgroundResource(resource);
		}
	}

	private void initNavigationMenu() {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
		transaction.add(R.id.framePlaylists, StartingFragment.newInstance(this, getApp().getSelf(), getApp().getSelectedPlaylist()), STARTING_FRAGMENT);
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

		case android.R.id.home:
			menuDrawer.toggleMenu();
			return true;

		case R.id.itemSearch:
			onSearchRequested();
			return true;

		case R.id.itemDownload:
			download();
			return true;

		case R.id.itemRefresh:
			refresh();
			return true;

		case R.id.itemSleepingTimer:
			new SleepingTimerFragment().show(getSupportFragmentManager(), "sleeping timer");
			return true;

		case R.id.itemPrefs:
			startActivity(new Intent(this, PreferencesActivity.class));
			return true;

		case R.id.itemLogOut:
			logOut(true);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void refresh() {
		playlistFragment.refresh();
		if (fragmentPager != null) {
			menuDrawer.closeMenu();
			fragmentPager.setCurrentItem(1, true);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (intent != null && Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, SearchProvider.AUTHORITY, SearchProvider.MODE);
			suggestions.saveRecentQuery(query, null);

			loadPlaylist(Playlist.get(new Playlist(Type.SEARCH, query, query)));
		}
	}

	public static void recycle(View view) {
		if (view != null) {
			Drawable background = view.getBackground();
			if (background != null && background instanceof BitmapDrawable)
				((BitmapDrawable) background).getBitmap().recycle();

			if (view instanceof ImageView) {
				Drawable drawable = ((ImageView) view).getDrawable();
				if (drawable != null && drawable instanceof BitmapDrawable)
					((BitmapDrawable) drawable).getBitmap().recycle();
			}
		}
	}

	private void logOut(boolean reset) {
		getApp().getSettings().resetVKontakte();
		doUnbindService();
		stopService(new Intent(this, PlayerService.class));
		Intent intent = new Intent(this, LoginActivity.class);
		intent.putExtra(LoginActivity.RESET, reset);
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
				if (service != null && service.getPlayer().getState() != State.NOT_PREPARED)
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
	public void showUnits(boolean friends) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
		UnitsListFragment unitsFragment = UnitsListFragment.newInstance(friends, friends ? getApp().getFriends() : getApp().getGroups());
		transaction.replace(R.id.framePlaylists, unitsFragment, "units list fragment");
		transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
	public void showUnit(Unit unit) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
		transaction.replace(R.id.framePlaylists, UnitFragment.newInstance(unit, getApp().getSelectedPlaylist()), UNIT_FRAGMENT);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
	public List<Unit> loadUnits(boolean friends) throws ClientProtocolException, IOException, VKontakteException {
		if (getApp() != null) {
			if (friends)
				return getApp().loadFriends();
			else
				return getApp().loadGroups();
		}
		return null;
	}

	@Override
	public List<Album> loadAlbums(Unit unit) throws ClientProtocolException, IOException, VKontakteException {
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
		if (!app.getSelectedPlaylist().equals(playlist)) {
			getApp().setSelectedPlaylist(playlist);
			playlistFragment.loadPlaylist(isPlaying());
		}
		if (fragmentPager != null) {
			menuDrawer.closeMenu();
			fragmentPager.setCurrentItem(1, true);
		}

		if (playlist.type == Type.SEARCH) {
			Fragment startingFragment = getSupportFragmentManager().findFragmentByTag(STARTING_FRAGMENT);
			if (startingFragment != null)
				((AlbumsFragment) startingFragment).setSelectedPosition(-1);

			Fragment unitFragment = getSupportFragmentManager().findFragmentByTag(UNIT_FRAGMENT);
			if (unitFragment != null)
				((AlbumsFragment) unitFragment).setSelectedPosition(-1);
		}
	}

	@Override
	public List<Song> loadSongs(Playlist playlist) throws IOException, VKontakteException {
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
		if (service != null) {
			int songDuration = service.getPlayer().getSongDuration();
			if (controlsFragment != null)
				controlsFragment.onProgressChanged(progress, songDuration);
			if (playlistFragment != null)
				playlistFragment.onProgressChanged(progress, songDuration);
		}
	}

	@Override
	public void onBufferingUpdate(int percent) {
		if (controlsFragment != null)
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
		Playlist selectedPlaylist = getApp().getSelectedPlaylist();
		if (selectedPlaylist != null && selectedPlaylist.songs != null && service != null) {
			Song song = selectedPlaylist.songs.get(position);
			service.download(song);
		}
	}

	private void download() {
		Song song = service.getPlayer().getCurrentSong();
		service.download(song);
	}

	@Override
	public void loveUnlove(int position) {
		Song song = app.getSelectedPlaylist().songs.get(position);

		if (song != null) {
			if (song.myAid != 0)
				controlsFragment.unlove(song);
			else
				controlsFragment.love(song);
		}
	}

	@Override
	public void remove(int position) {
		Player player = service.getPlayer();
		PlaylistAdapter adapter = playlistFragment.getAdapter();
		boolean equals = app.getSelectedPlaylist().equals(app.getPlaylist());
		if (equals) {
			if (player.currentTrack == position) {
				player.next();
			} else if (player.currentTrack > position) {
				player.currentTrack--;
				adapter.currentTrack--;
			}
		}
		app.getSelectedPlaylist().songs.remove(position);
		adapter.notifyDataSetChanged();
		if (equals && getApp().getSettings().getShuffle())
			player.generateShuffleQueue();
	}

	@Override
	public String getAlbumImageUrl() {
		if (service != null) {
			Song song = service.getPlayer().getCurrentSong();
			if (song != null) {
				if (song.albumImageUrl == null)
					song.albumImageUrl = getApp().getLastFM().getAlbumImageUrl(song);
				else if (LastFM.WITHOUT_IMAGE.equals(song.albumImageUrl))
					return null;
				return song.albumImageUrl;
			}
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
		v.startAnimation(getApp().getShakeAnimation());
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
		v.startAnimation(getApp().getShakeAnimation());
		service.getPlayer().next();
	}

	@Override
	public void onPrevButtonPressed(View v) {
		v.startAnimation(getApp().getShakeAnimation());
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
		if (service != null) {
			Player player = service.getPlayer();
			if (player.isLooping()) {
				v.setBackgroundResource(R.drawable.repeat_grey);
				player.setLooping(false);
			} else {
				v.setBackgroundResource(R.drawable.repeat_blue);
				player.setLooping(true);
			}
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
			if (state == State.PLAYING || state == State.PAUSED) {
				onBufferingStrated();
				service.getPlayer().seekTo(progress);
			} else if (state != State.SEEKING_FOR_IDLE && state != State.SEEKING_FOR_PLAYBACK)
				seekBar.setProgress(0);
		}
	}

	@Override
	public void setPlaylist(Playlist playlist) {
		getApp().setPlaylist(playlist);
		if (service != null && getApp().getSettings().getShuffle())
			service.getPlayer().generateShuffleQueue();
		setTitleAndIcon();
	}

	@Override
	public void onPlaylistLoaded() {
		if (service != null && getApp().getSettings().getShuffle())
			service.getPlayer().generateShuffleQueue();

		setTitleAndIcon();
		onNewTrack();

		if (fragmentPager != null && !getApp().getSettings().isTutorialComplete()) {
			menuDrawer.closeMenu();
			fragmentPager.setCurrentItem(1);
			new TutorialFragment().show(getSupportFragmentManager(), Settings.TUTORIAL);
		}
	}

	@Override
	public Playlist getPlaylist() {
		return getApp().getPlaylist();
	}

	private static String capitalize(String line) {
		return Character.toUpperCase(line.charAt(0)) + line.substring(1);
	}

	private void setTitleAndIcon() {
		Playlist playlist = getApp().getPlaylist();
		if (playlist.name != null)
			getSupportActionBar().setTitle(capitalize(playlist.name));

		if (playlist.unit != null)
			new LogoLoader().execute(playlist.unit.photo);
		else
			getSupportActionBar().setIcon(getResources().getDrawable(R.drawable.ic_action_search));
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
		playlistIsLoading = loading;
	}

	@Override
	public boolean isPlaylistLoading() {
		return playlistIsLoading;
	}

	@Override
	public boolean getShuffle() {
		return getApp().getSettings().getShuffle();
	}

	@Override
	public void onViewCreated(Fragment fragment) {
		if (fragmentPager != null) {
			if (fragment instanceof ControlsFragment) {
				controlsFragment = (ControlsFragment) fragment;
			} else if (fragment instanceof PlaylistFragment) {
				playlistFragment = (PlaylistFragment) fragment;
				fragmentPager.setCurrentItem(1);
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
		}
	}

	@Override
	public boolean isPlaying() {
		return service != null ? service.getPlayer().getState() != State.NOT_PREPARED : false;
	}

	@Override
	public Playlist getSelectedPlaylist() {
		return getApp().getSelectedPlaylist();

	}

	@Override
	public List<Unit> getUnits(boolean friends) {
		return friends ? getApp().getFriends() : getApp().getGroups();
	}

	private VibesApplication getApp() {
		if (app == null)
			app = (VibesApplication) getApplication();
		return app;
	}

	@Override
	public boolean getRepeat() {
		if (service != null)
			return service.getPlayer().isLooping();
		return false;
	}

	@Override
	public void onTutorialSwipe() {
		if (fragmentPager != null) {
			fragmentPager.setCurrentItem(0);
			app.getSettings().setTutorialComplete(true);
		}
	}

	@Override
	public List<Song> updateSongs(Playlist playlist) throws IOException, VKontakteException {
		return getApp().updateSongs(playlist);
	}

	@Override
	public void onPlaylistUpdated(int newTracks) {
		if (service != null && newTracks > 0) {
			Player player = service.getPlayer();
			player.currentTrack += newTracks;
			if (getApp().getSettings().getShuffle())
				service.getPlayer().generateShuffleQueue();
		}

	}

	@Override
	public void setTimer(int totalMinutes) {
		service.getPlayer().setTimer(totalMinutes * 60 * 1000);
		int hours = totalMinutes / 60;
		int minutes = totalMinutes % 60;
		String message = null;
		if (hours > 0) {
			if (minutes > 0)
				message = getString(R.string.gonna_stop) + " " + hours + " " + getString(R.string.hours) + " " + getString(R.string.and) + " " + minutes
						+ " " + getString(R.string.minutes);
			else
				message = getString(R.string.gonna_stop) + " " + hours + " " + getString(R.string.hours);

		} else if (minutes > 0)
			message = getString(R.string.gonna_stop) + " " + minutes + " " + getString(R.string.minutes);
		else
			message = getString(R.string.timer_off);

		if (message != null)
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
}
