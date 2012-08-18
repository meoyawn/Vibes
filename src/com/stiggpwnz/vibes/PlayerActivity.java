package com.stiggpwnz.vibes;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.client.methods.HttpPost;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.stiggpwnz.vibes.Player.State;
import com.stiggpwnz.vibes.PlayerService.ServiceBinder;
import com.stiggpwnz.vibes.adapters.PlaylistAdapter;
import com.stiggpwnz.vibes.adapters.ViewPagerAdapter;
import com.stiggpwnz.vibes.dialogs.AlbumsDialog;
import com.stiggpwnz.vibes.dialogs.LastFMLoginDialog;
import com.stiggpwnz.vibes.dialogs.LastFMUserDialog;
import com.stiggpwnz.vibes.dialogs.PlaylistsDialog;
import com.stiggpwnz.vibes.dialogs.SearchDIalog;
import com.stiggpwnz.vibes.dialogs.UnitDialog;
import com.stiggpwnz.vibes.dialogs.UnitsDialog;
import com.stiggpwnz.vibes.imageloader.ImageLoader;
import com.stiggpwnz.vibes.restapi.Album;
import com.stiggpwnz.vibes.restapi.Song;
import com.stiggpwnz.vibes.restapi.Unit;
import com.stiggpwnz.vibes.restapi.VkontakteException;

public class PlayerActivity extends Activity implements Player.OnActionListener, OnClickListener, OnSeekBarChangeListener, OnItemClickListener {

	// non enum because we'll need to store this things in settings
	public static final int SEARCH = 0;
	public static final int FRIENDS = 1;
	public static final int GROUPS = 2;
	public static final int MY_AUDIOS = 3;
	public static final int WALL = 4;
	public static final int NEWSFEED = 5;
	public static final int ALBUMS = 6;

	// non enum because of android api
	public static final int DIALOG_LAST_FM_AUTH = 56;
	public static final int DIALOG_LAST_FM_USER = 29;
	public static final int DIALOG_PLAYLISTS = 69;
	public static final int DIALOG_SEARCH = 75;
	public static final int DIALOG_UNITS = 76;
	public static final int DIALOG_UNIT = 77;
	public static final int DIALOG_ALBUMS = 78;

	private final AtomicInteger runningThreads = new AtomicInteger();
	private final ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			service = ((ServiceBinder) binder).getService();
			if (app.songs == null)
				runGetSongs(null);
			service.setPlayerListener(PlayerActivity.this);
			bound = true;
			service.cancelNotification();
			service.stopWaiter();
			onNewTrack();
			Log.d(VibesApplication.VIBES, "service bound");
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			// this motherfucking liar never gets called, cost me 2 hours to realize
		}
	};

	private PlayerService service;
	private VibesApplication app;

	private Typeface typeface;

	private Button btnPlay;
	private Button btnLove;

	private TextView textArtist;
	private TextView textTitle;
	private TextView textBuffering;
	private TextView textPassed;
	private TextView textLeft;

	private ProgressBar progressBuffering;
	private ProgressBar progressUpdating;

	private ImageView albumImage;

	private ProgressDialog loadingDialog;

	private ViewPagerAdapter pagerAdapter;
	private ViewPager viewPager;
	private List<View> pages;

	private PlaylistAdapter playlistAdapter;

	private Animation shake;
	private SeekBar seekbar;
	private ListView playlist;
	private ImageLoader imageLoader;
	private GetAndSetAlbumImage getAlbumImage;
	private Unit unit;

	private boolean buffering;
	private boolean bound;

	private List<Album> myAlbums;
	private List<Unit> friends;
	private List<Unit> groups;
	private boolean friendsList;
	private Button btnShuffle;
	private boolean inFront;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (VibesApplication) getApplication();

		pages = new LinkedList<View>();
		imageLoader = new ImageLoader(this, R.drawable.music);
		typeface = Typeface.createFromAsset(getAssets(), "SegoeWP-Semilight.ttf");
		initUI();
	}

	@Override
	protected void onResume() {
		super.onResume();
		doBindService();
		inFront = true;
	}

	private void doBindService() {
		Intent intent = new Intent(this, PlayerService.class);
		if (!app.isServiceRunning()) {
			Log.d(VibesApplication.VIBES, "starting service");
			startService(intent);
		}
		if (!bound) {
			Log.d(VibesApplication.VIBES, "binding service");
			bindService(intent, connection, BIND_AUTO_CREATE);
		}
	}

	public void doUnbindService() {
		if (bound) {
			unbindService(connection);
			service.setPlayerListener(null);
			service = null;
			bound = false;
			Log.d(VibesApplication.VIBES, "service unbound from onpause");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (service != null) {
			Player player = service.getPlayer();
			State state = player.getState();
			if (state == State.PLAYING)
				service.makeNotification();
			else if (state == State.NOT_PREPARED || state == State.PAUSED || state == State.PREPARING_FOR_IDLE)
				service.startWaiter();
		}
		if (runningThreads.get() == 0)
			doUnbindService();
		inFront = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// TODO Auto-generated method stub
	}

	private void initUI() {
		setContentView(R.layout.player);

		initPlayerPage();

		btnPlay = (Button) findViewById(R.id.btnPlay);
		btnPlay.setOnClickListener(this);

		Button btnFwd = (Button) findViewById(R.id.btnFwd);
		btnFwd.setOnClickListener(this);

		Button btnRwd = (Button) findViewById(R.id.btnRwd);
		btnRwd.setOnClickListener(this);

		Button btnPlaylist = (Button) findViewById(R.id.btnPlaylist);
		btnPlaylist.setOnClickListener(this);

		pagerAdapter = new ViewPagerAdapter(pages);

		viewPager = (ViewPager) findViewById(R.id.viewpager);
		viewPager.setAdapter(pagerAdapter);

		shake = AnimationUtils.loadAnimation(this, R.anim.shake);

		initPlaylistPage();
	}

	private void initPlayerPage() {
		LayoutInflater inflater = LayoutInflater.from(this);
		View page = inflater.inflate(R.layout.control, null);

		View btnPlsFwd = page.findViewById(R.id.btnPlsFwd);
		btnPlsFwd.setOnClickListener(this);

		View btnDownload = page.findViewById(R.id.btnDownload);
		btnDownload.setOnClickListener(this);

		textArtist = (TextView) page.findViewById(R.id.artist);
		textArtist.setTypeface(typeface);

		textTitle = (TextView) page.findViewById(R.id.title);
		textTitle.setTypeface(typeface);

		albumImage = (ImageView) page.findViewById(R.id.imageAlbum);

		textBuffering = (TextView) page.findViewById(R.id.textBuffering);
		textBuffering.setTypeface(typeface);

		progressBuffering = (ProgressBar) page.findViewById(R.id.progressCircle);

		seekbar = (SeekBar) page.findViewById(R.id.seekBar);
		seekbar.setOnSeekBarChangeListener(this);

		textPassed = (TextView) page.findViewById(R.id.textPassed);
		textPassed.setTypeface(typeface);

		textLeft = (TextView) page.findViewById(R.id.textLeft);
		textLeft.setTypeface(typeface);

		btnLove = (Button) page.findViewById(R.id.btnLove);
		btnLove.setOnClickListener(this);

		btnShuffle = (Button) page.findViewById(R.id.btnShuffle);
		btnShuffle.setOnClickListener(this);
		if (app.getSettings().getShuffle())
			btnShuffle.setBackgroundResource(R.drawable.shuffle_blue);
		else
			btnShuffle.setBackgroundResource(R.drawable.shuffle_grey);

		Button btnRepeat = (Button) page.findViewById(R.id.btnRepeat);
		btnRepeat.setOnClickListener(this);

		pages.add(page);
	}

	private void initPlaylistPage() {
		LayoutInflater inflater = LayoutInflater.from(this);
		View page = inflater.inflate(R.layout.playlist, null);

		Button btnBack = (Button) page.findViewById(R.id.btnBack);
		btnBack.setOnClickListener(this);

		Button btnUpdate = (Button) page.findViewById(R.id.btnUpdate);
		btnUpdate.setOnClickListener(this);

		progressUpdating = (ProgressBar) page.findViewById(R.id.progressUpdating);

		playlistAdapter = new PlaylistAdapter(this, typeface);
		playlist = (ListView) page.findViewById(R.id.list);
		playlist.setAdapter(playlistAdapter);
		TextView empty = (TextView) page.findViewById(android.R.id.empty);
		empty.setTypeface(typeface);
		playlist.setEmptyView(empty);
		playlist.setOnItemClickListener(this);

		pages.add(page);
		pagerAdapter.notifyDataSetChanged();
	}

	public void runGetALbums() {
		new GetAlbums().execute();
	}

	private class GetSongs extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			enteredThread();
			showLoadingDialog(true);
			if (service.getPlayer().getState() == State.PLAYING)
				service.getPlayer().current = service.getPlayer().getCurrentSong();
			Log.d(VibesApplication.VIBES, "showing getsongs dialog");
		}

		@Override
		protected Void doInBackground(String... params) {
			Thread.currentThread().setName("Getting songs");
			getSongs(params);
			return null;
		}

		private void getSongs(String... params) {
			try {
				app.getSongs(params[0]);
			} catch (IOException e) {
				internetFail();
			} catch (VkontakteException e) {
				switch (e.getCode()) {
				case VkontakteException.UNKNOWN_ERROR_OCCURED:
					unknownError();
					break;

				case VkontakteException.USER_AUTHORIZATION_FAILED:
					authFail();
					break;

				case VkontakteException.TOO_MANY_REQUESTS_PER_SECOND:
					getSongs(params);

				case VkontakteException.ACCESS_DENIED:
					accessDenied();
					app.getSettings().setPlaylist(NEWSFEED);
					break;

				case VkontakteException.PERMISSION_TO_PERFORM_THIS_ACTION_IS_DENIED_BY_USER:
					accessDenied();
					app.getSettings().setPlaylist(NEWSFEED);
					break;
				}
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			hideLoadingDialog();
			viewPager.setCurrentItem(1, true);
			Player player = service.getPlayer();
			playlistAdapter.setSongs(app.songs);
			if (player.getState() == State.PLAYING) {
				player.currentTrack = -1;
			} else {
				if (app.songs != null && app.songs.size() > 0) {
					player.currentTrack = 0;
					onNewTrack();
				}
			}

			playlistAdapter.currentSong = -1;
			playlistAdapter.notifyDataSetChanged();
			playlist.setSelection(0);
			player.generateShuffleQueue();
			outOfThread();
		}
	}

	private class UpdateSongs extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			enteredThread();
			progressUpdating.setVisibility(View.VISIBLE);
		}

		private int updateSongs() {
			try {
				return app.updateSongs();
			} catch (IOException e) {
				internetFail();
			} catch (VkontakteException e) {
				switch (e.getCode()) {
				case VkontakteException.UNKNOWN_ERROR_OCCURED:
					unknownError();
					break;

				case VkontakteException.USER_AUTHORIZATION_FAILED:
					authFail();
					break;

				case VkontakteException.TOO_MANY_REQUESTS_PER_SECOND:
					return updateSongs();

				case VkontakteException.ACCESS_DENIED:
					accessDenied();
					break;

				case VkontakteException.PERMISSION_TO_PERFORM_THIS_ACTION_IS_DENIED_BY_USER:
					accessDenied();
					break;

				default:
					return 0;
				}
			}
			return 0;
		}

		@Override
		protected Integer doInBackground(Void... params) {
			Thread.currentThread().setName("Updating songs");
			return updateSongs();
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			Log.d(VibesApplication.VIBES, "new songs: " + result);
			progressUpdating.setVisibility(View.INVISIBLE);
			if (result > 0 && service.getPlayer().currentTrack != -1) {
				service.getPlayer().currentTrack += result;
				if (playlistAdapter.currentSong != -1)
					playlistAdapter.currentSong += result;
			}
			playlistAdapter.notifyDataSetChanged();
			service.getPlayer().generateShuffleQueue();
			outOfThread();
		}
	}

	private class GetAlbums extends AsyncTask<Void, Void, List<Album>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showLoadingDialog(false);
		}

		private List<Album> getAlbums() {
			try {
				if (app.getSettings().getOwnerId() != 0) {
					if (getUnit().albums == null)
						return app.getAlbums(getUnit().id);
					else
						return getUnit().albums;
				} else {
					if (getMyAlbums() == null)
						return app.getAlbums(0);
					else
						return getMyAlbums();
				}
			} catch (IOException e) {
				internetFail();
			} catch (VkontakteException e) {
				switch (e.getCode()) {
				case VkontakteException.UNKNOWN_ERROR_OCCURED:
					unknownError();
					break;

				case VkontakteException.USER_AUTHORIZATION_FAILED:
					authFail();
					break;

				case VkontakteException.TOO_MANY_REQUESTS_PER_SECOND:
					return getAlbums();

				case VkontakteException.ACCESS_DENIED:
					accessDenied();
					break;

				default:
					return null;
				}
			}
			return null;
		}

		@Override
		protected List<Album> doInBackground(Void... params) {
			Thread.currentThread().setName("Getting albums");
			return getAlbums();
		}

		@Override
		protected void onPostExecute(List<Album> result) {
			super.onPostExecute(result);
			if (result != null) {
				if (app.getSettings().getOwnerId() != 0)
					getUnit().albums = result;
				else
					myAlbums = result;
				showDialog(DIALOG_ALBUMS);
			}
			hideLoadingDialog();
		}
	}

	private class GetUnits extends AsyncTask<Void, Void, Void> {

		private boolean success;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showLoadingDialog(false);
		}

		private Void getUnits() {
			try {
				if (friendsList)
					friends = app.getFriends();
				else
					groups = app.getGroups();
				success = true;
			} catch (IOException e) {
				success = false;
				internetFail();
			} catch (VkontakteException e) {
				success = false;
				switch (e.getCode()) {
				case VkontakteException.UNKNOWN_ERROR_OCCURED:
					unknownError();
					break;

				case VkontakteException.USER_AUTHORIZATION_FAILED:
					authFail();
					break;

				case VkontakteException.TOO_MANY_REQUESTS_PER_SECOND:
					return getUnits();

				default:
					return null;
				}
			}
			return null;
		}

		@Override
		protected Void doInBackground(Void... params) {
			Thread.currentThread().setName("Getting units");
			return getUnits();
		}

		@Override
		protected void onPostExecute(Void result) {
			if (success)
				showDialog(DIALOG_UNITS);
			hideLoadingDialog();
		}

	}

	private class GetAndSetAlbumImage extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			Thread.currentThread().setName("Getting and setting an album image");
			synchronized (this) {
				if (isCancelled())
					return null;
				return app.getLastFM().getAlbumImageURL(params[0], params[1]);
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result != null) {
				if (imageLoader.getStubId() != R.drawable.music)
					imageLoader.setStubId(R.drawable.music);
				imageLoader.displayImage(result, albumImage);
			} else {
				Log.d(VibesApplication.VIBES, "result is null");
				albumImage.setImageResource(R.drawable.music);
			}
		}

	}

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}

	private void showLoadingDialog(boolean playlist) {
		loadingDialog = new ProgressDialog(this);
		loadingDialog.setIndeterminate(true);
		loadingDialog.setCancelable(false);
		if (playlist) {
			switch (app.getSettings().getPlaylist()) {
			case SEARCH:
				loadingDialog.setMessage(getString(R.string.searchingSongs));
				break;

			case MY_AUDIOS:
				if (app.getSettings().getAlbumId() == 0)
					loadingDialog.setMessage(getString(R.string.gettingSongsAudios));
				else
					loadingDialog.setMessage(getString(R.string.gettingSongsAlbum));
				break;

			case WALL:
				loadingDialog.setMessage(getString(R.string.gettingSongsWall));
				break;

			case NEWSFEED:
				loadingDialog.setMessage(getString(R.string.gettingSongsNewsfeed));
				break;

			}
		} else {
			loadingDialog.setMessage(getString(R.string.loading));
		}
		loadingDialog.show();
	}

	private void accessDenied() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(PlayerActivity.this, getString(R.string.access_denied), Toast.LENGTH_LONG).show();
			}
		});
	}

	private void authFail() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(PlayerActivity.this, getString(R.string.authProblem), Toast.LENGTH_LONG).show();
				logOut(false);
			}
		});
	}

	private void unknownError() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				service.getPlayer().stop();
				Toast.makeText(PlayerActivity.this, getString(R.string.unknownError), Toast.LENGTH_LONG).show();
			}
		});

	}

	private void internetFail() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				service.getPlayer().stop();
				Toast.makeText(PlayerActivity.this, getString(R.string.no_internet), Toast.LENGTH_LONG).show();
			}
		});
	}

	private void hideLoadingDialog() {
		if (loadingDialog != null)
			loadingDialog.dismiss();
	}

	@SuppressLint("NewApi")
	private void setCurrentSong(boolean fromPlaylist) {

		Player player = service.getPlayer();
		Song currentSong = player.getCurrentSong();
		if (currentSong != null) {
			String performer = currentSong.performer;
			textArtist.setText(performer);

			String name = currentSong.title;
			textTitle.setText(name);

			if (currentSong.loved)
				btnLove.setBackgroundResource(R.drawable.love_blue);
			else
				btnLove.setBackgroundResource(R.drawable.love_grey);

			synchronized (app.getLastFM()) {
				List<HttpPost> requests = app.getLastFM().getImageRequestQueue();
				if (getAlbumImage != null && getAlbumImage.getStatus() == AsyncTask.Status.RUNNING) {
					Log.e(VibesApplication.VIBES, "cancelling image loader: " + requests.size() + " items in queue");
					for (HttpPost request : requests) {
						request.abort();
					}
					getAlbumImage.cancel(true);
				}
				requests.clear();
			}
			getAlbumImage = new GetAndSetAlbumImage();
			getAlbumImage.execute(performer, name);

			State state = player.getState();
			if (state == State.PLAYING || state == State.PAUSED) {
				onBufferingEnded();
				onProgressChanged(player.getCurrentPosition());
				if (state == State.PLAYING)
					btnPlay.setBackgroundResource(R.drawable.pause);
				else
					btnPlay.setBackgroundResource(R.drawable.play);
			} else if (state == State.PREPARING_FOR_IDLE || state == State.SEEKING_FOR_IDLE) {
				onBufferingStrated();
				btnPlay.setBackgroundResource(R.drawable.play);
				if (state == State.PREPARING_FOR_IDLE)
					nullEverything();
			} else if (state == State.PREPARING_FOR_PLAYBACK || state == State.SEEKING_FOR_PLAYBACK || state == State.NEXT_FOR_PLAYBACK) {
				onBufferingStrated();
				btnPlay.setBackgroundResource(R.drawable.pause);
				if (state == State.PREPARING_FOR_PLAYBACK || state == State.NEXT_FOR_PLAYBACK)
					nullEverything();
			} else if (state == State.NOT_PREPARED) {
				onBufferingEnded();
				nullEverything();
				btnPlay.setBackgroundResource(R.drawable.play);
			}

			if (!fromPlaylist) {
				if (state != State.NOT_PREPARED) {
					playlistAdapter.currentSong = player.currentTrack;
					if (android.os.Build.VERSION.SDK_INT >= 8) {
						// playlist.smoothScrollToPosition(player.currentTrack);
					} else {
						playlist.setSelection(player.currentTrack);
					}

				} else {
					playlistAdapter.currentSong = -1;
				}
				playlistAdapter.notifyDataSetChanged();

			}
		}
	}

	public void nullEverything() {
		textPassed.setText("0:00");
		textLeft.setText("0:00");
		seekbar.setProgress(0);
		seekbar.setSecondaryProgress(0);
	}

	@Override
	public void onBufferingUpdate(int percent) {
		seekbar.setSecondaryProgress(seekbar.getMax() * percent / 100);
	}

	@Override
	public void onBufferingStrated() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				textBuffering.setVisibility(View.VISIBLE);
				progressBuffering.setVisibility(View.VISIBLE);
				buffering = true;
			}
		});
	}

	@Override
	public void onBufferingEnded() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				seekbar.setMax(service.getPlayer().getSongDuration());
				textBuffering.setVisibility(View.INVISIBLE);
				progressBuffering.setVisibility(View.INVISIBLE);
				buffering = false;
			}
		});
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
	public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
		playlistAdapter.fromPlaylist = true;
		playlistAdapter.currentSong = position;
		playlistAdapter.notifyDataSetChanged();
		btnPlay.setBackgroundResource(R.drawable.pause);
		service.getPlayer().play(position);
		setCurrentSong(true);
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {

	}

	// private final Runnable playJumper = new Runnable() {
	//
	// @Override
	// public void run() {
	// btnPlay.startAnimation(shake);
	// }
	// };

	@Override
	public void onClick(View v) {
		Player player = service.getPlayer();
		Settings settings = app.getSettings();
		State state = player.getState();

		switch (v.getId()) {
		case R.id.btnPlay:
			v.startAnimation(shake);
			Log.d(VibesApplication.VIBES, "pressing play and state = " + state);
			if (state == State.PAUSED || state == State.PREPARING_FOR_IDLE || state == State.SEEKING_FOR_IDLE) {
				player.resume();
				v.setBackgroundResource(R.drawable.pause);
			} else if (state == State.PLAYING || state == State.PREPARING_FOR_PLAYBACK || state == State.SEEKING_FOR_PLAYBACK) {
				player.pause();
				v.setBackgroundResource(R.drawable.play);
			} else if (state == State.NOT_PREPARED && player.getCurrentSong() != null) {
				player.play();
				v.setBackgroundResource(R.drawable.pause);
			}
			break;

		case R.id.btnFwd:
			v.startAnimation(shake);
			// if (state == State.STATE_PREPARING_FOR_PLAYBACK || state == State.STATE_PLAYING || state ==
			// State.STATE_SEEKING_FOR_PLAYBACK)
			// service.getHandler().postDelayed(playJumper, 75);
			player.next();
			break;

		case R.id.btnRwd:
			v.startAnimation(shake);
			player.prev();
			break;

		case R.id.btnBack:
			viewPager.setCurrentItem(0, true);
			break;

		case R.id.btnPlsFwd:
			viewPager.setCurrentItem(1, true);
			break;

		case R.id.btnUpdate:
			new UpdateSongs().execute();
			break;

		case R.id.btnLove:
			if (player.getCurrentSong().loved)
				unlove();
			else
				love();
			break;

		case R.id.btnShuffle:
			if (settings.getShuffle()) {
				v.setBackgroundResource(R.drawable.shuffle_grey);
				settings.setShuffle(false);
			} else {
				v.setBackgroundResource(R.drawable.shuffle_blue);
				settings.setShuffle(true);
				player.generateShuffleQueue();
			}
			break;

		case R.id.btnRepeat:
			if (player.isLooping()) {
				v.setBackgroundResource(R.drawable.repeat_grey);
				player.setLooping(false);
			} else {
				v.setBackgroundResource(R.drawable.repeat_blue);
				player.setLooping(true);
			}
			break;

		case R.id.btnPlaylist:
			showDialog(DIALOG_PLAYLISTS);
			break;

		case R.id.btnDownload:
			service.download(player.currentTrack);
			break;
		}
	}

	private void love() {
		btnLove.setBackgroundResource(R.drawable.love_blue);
		new Love().execute();
	}

	private void unlove() {
		btnLove.setBackgroundResource(R.drawable.love_grey);
		new UnLove().execute();
	}

	private class Love extends AsyncTask<Void, Void, Integer> {

		boolean own;
		boolean lastLoved;
		Player player = service.getPlayer();
		Settings settings = app.getSettings();

		private Integer addSong() {
			try {
				return app.getVkontakte().add(player.getCurrentSong());
			} catch (IOException e) {
				internetFail();
			} catch (VkontakteException e) {
				switch (e.getCode()) {

				case VkontakteException.USER_AUTHORIZATION_FAILED:
					authFail();
					break;

				case VkontakteException.TOO_MANY_REQUESTS_PER_SECOND:
					return addSong();

				case VkontakteException.ACCESS_DENIED:
					accessDenied();
					break;

				default:
					return null;
				}
			}
			return null;
		}

		@Override
		protected Integer doInBackground(Void... params) {
			enteredThread();
			Thread.currentThread().setName("Loving song");
			own = settings.getPlaylist() == MY_AUDIOS && settings.getOwnerId() == 0;
			if (settings.getSession() != null)
				if (app.getLastFM().love(player.getCurrentSong())) {
					lastLoved = true;
					Log.d(VibesApplication.VIBES, "last fm: loved");
				}
			if (!own)
				return addSong();
			return null;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (!own) {

				if (result != null) {
					player.getCurrentSong().myAid = result;
					player.getCurrentSong().loved = true;
				} else
					btnLove.setBackgroundResource(R.drawable.love_grey);

			} else if (settings.getSession() != null) {

				if (lastLoved)
					player.getCurrentSong().loved = true;
				else
					btnLove.setBackgroundResource(R.drawable.love_grey);
			} else {
				player.getCurrentSong().loved = true;
			}
			outOfThread();
		}
	}

	private class UnLove extends AsyncTask<Void, Void, Boolean> {

		boolean own;
		boolean lastUnloved;
		Player player = service.getPlayer();
		Settings settings = app.getSettings();

		private Boolean deleteSong() {
			try {
				return app.getVkontakte().delete(player.getCurrentSong());
			} catch (IOException e) {
				internetFail();
			} catch (VkontakteException e) {
				switch (e.getCode()) {

				case VkontakteException.USER_AUTHORIZATION_FAILED:
					authFail();
					break;

				case VkontakteException.TOO_MANY_REQUESTS_PER_SECOND:
					return deleteSong();

				case VkontakteException.ACCESS_DENIED:
					accessDenied();
					break;

				default:
					return false;
				}
			}
			return false;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			enteredThread();
			Thread.currentThread().setName("Unloving song");
			own = settings.getPlaylist() == MY_AUDIOS && settings.getOwnerId() == 0;
			if (settings.getSession() != null)
				if (app.getLastFM().unlove(player.getCurrentSong())) {
					lastUnloved = true;
					Log.d(VibesApplication.VIBES, "last fm: unloved");
				}
			if (!own)
				return deleteSong();
			return null;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (!own) {
				if (result) {
					player.getCurrentSong().myAid = 0;
					player.getCurrentSong().loved = false;
				} else {
					btnLove.setBackgroundResource(R.drawable.love_blue);
				}
			} else if (settings.getSession() != null) {

				if (lastUnloved)
					player.getCurrentSong().loved = false;
				else
					btnLove.setBackgroundResource(R.drawable.love_blue);
			} else {
				player.getCurrentSong().loved = false;
			}
			outOfThread();
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser && service.getPlayer().getState() == State.PLAYING) {
			onBufferingStrated();
			service.getPlayer().seekTo(progress);
		} else if (fromUser && (service.getPlayer().getState() != State.PLAYING || buffering))
			seekBar.setProgress(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.playermenu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {

		case R.id.itemLogOut:
			logOut(true);
			return true;

		case R.id.itemLastFM:
			if (app.getSettings().getSession() == null)
				showDialog(DIALOG_LAST_FM_AUTH);
			else
				showDialog(DIALOG_LAST_FM_USER);
			return true;

		case R.id.itemPrefs:
			startActivity(new Intent(this, PreferencesActivity.class));
			return true;

		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	private void logOut(boolean logout) {
		app.getSettings().resetData();
		app.getSettings().setPlaylist(NEWSFEED);
		doUnbindService();
		stopService(new Intent(PlayerActivity.this, PlayerService.class));
		Intent intent = new Intent(PlayerActivity.this, LoginActivity.class);
		intent.putExtra(LoginActivity.RESET, logout);
		startActivity(intent);
		finish();
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_UNIT:
			return new UnitDialog(this, typeface);

		case DIALOG_ALBUMS:
			List<Album> albums = app.getSettings().getOwnerId() != 0 ? unit.albums : getMyAlbums();
			return new AlbumsDialog(this, albums, typeface);

		case DIALOG_LAST_FM_AUTH:
			return new LastFMLoginDialog(this);

		case DIALOG_LAST_FM_USER:
			return new LastFMUserDialog(this, imageLoader);

		case DIALOG_SEARCH:
			return new SearchDIalog(this);

		case DIALOG_UNITS:
			List<Unit> units = friendsList ? friends : groups;
			return new UnitsDialog(this, imageLoader, units, typeface);

		case DIALOG_PLAYLISTS:
			return new PlaylistsDialog(this, typeface);
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {
		case DIALOG_ALBUMS:
			List<Album> albumList = app.getSettings().getOwnerId() != 0 && unit != null ? unit.albums : myAlbums;
			AlbumsDialog albumsDialog = (AlbumsDialog) dialog;
			albumsDialog.setAlbums(albumList);
			break;

		case DIALOG_LAST_FM_USER:
			LastFMUserDialog lastFMUserDialog = (LastFMUserDialog) dialog;
			lastFMUserDialog.setText(app.getSettings().getUsername());
			lastFMUserDialog.setUserImage(app.getSettings().getUserImage());
			break;

		case DIALOG_UNITS:
			String[] array = getResources().getStringArray(R.array.playlist_options);
			String title = friendsList ? array[1] : array[2];
			dialog.setTitle(title);

			List<Unit> list = friendsList ? friends : groups;
			UnitsDialog unitsDialog = (UnitsDialog) dialog;
			unitsDialog.setList(list);
			break;

		case DIALOG_UNIT:
			if (unit != null && unit.name != null)
				dialog.setTitle(unit.name);
			break;
		}
	}

	public List<Album> getMyAlbums() {
		return myAlbums;
	}

	public boolean isFriendsList() {
		return friendsList;
	}

	public void setFriendsList(boolean friendsList) {
		this.friendsList = friendsList;
	}

	public void runGetUnits() {
		new GetUnits().execute();
	}

	public VibesApplication getApp() {
		return app;
	}

	public PlayerService getService() {
		return service;
	}

	public List<Unit> getFriends() {
		return friends;
	}

	public List<Unit> getGroups() {
		return groups;
	}

	@Override
	public void onProgressChanged(int progress) {
		seekbar.setProgress(progress);

		int seconds = (progress / 1000) % 60;
		int minutes = (progress / 1000) / 60;
		if (seconds > 9)
			textPassed.setText(String.format("%d:%d", minutes, seconds));
		else
			textPassed.setText(String.format("%d:0%d", minutes, seconds));

		int songDuration = service.getPlayer().getSongDuration();
		seconds = ((songDuration - progress) / 1000) % 60;
		minutes = ((songDuration - progress) / 1000) / 60;
		if (seconds > 9)
			textLeft.setText(String.format("%d:%d", minutes, seconds));
		else
			textLeft.setText(String.format("%d:0%d", minutes, seconds));
	}

	public void runGetSongs(String search) {
		new GetSongs().execute(search);
	}

	@Override
	public void onNewTrack() {
		setCurrentSong(false);
	}

	private void enteredThread() {
		runningThreads.incrementAndGet();
	}

	private void outOfThread() {
		if (runningThreads.decrementAndGet() == 0 && !inFront)
			doUnbindService();
	}

}
