package com.stiggpwnz.vibes;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;

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

import com.stiggpwnz.vibes.NewService.ServiceBinder;
import com.stiggpwnz.vibes.Player.State;
import com.stiggpwnz.vibes.adapters.SongsAdapter;
import com.stiggpwnz.vibes.adapters.ViewPagerAdapter;
import com.stiggpwnz.vibes.dialogs.AlbumsDialog;
import com.stiggpwnz.vibes.dialogs.LastFMLoginDialog;
import com.stiggpwnz.vibes.dialogs.LastFMUserDialog;
import com.stiggpwnz.vibes.dialogs.PlaylistsDialog;
import com.stiggpwnz.vibes.dialogs.SearchDIalog;
import com.stiggpwnz.vibes.dialogs.UnitDialog;
import com.stiggpwnz.vibes.dialogs.UnitsDialog;

public class NewActivity extends Activity implements OnPlayerActionListener, OnClickListener, OnSeekBarChangeListener, OnItemClickListener {

	// non enum because we'll need to store this things
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

	private static final int UPDATE_PLAYLIST_TIMEOUT_SECONDS = 4;

	private final ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			service = ((ServiceBinder) binder).getService();
			service.setPlayerListener(NewActivity.this);
			bound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			service.setPlayerListener(null);
			service = null;
			bound = false;
		}
	};

	private NewService service;
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

	private SongsAdapter songsAdapter;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (VibesApplication) getApplication();
		doBindService();

		app = (VibesApplication) getApplication();
		pages = new LinkedList<View>();
		imageLoader = new ImageLoader(this, R.drawable.music);
		runGetSongs(null);
		initUI();
	}

	public void runGetSongs(String search) {
		new GetSongs().execute(search);
	}

	@Override
	protected void onResume() {
		super.onResume();
		doBindService();
		// TODO Auto-generated method stub

	}

	@Override
	protected void onPause() {
		super.onPause();
		imageLoader.getMemoryCache().clear();
		service.getPlayer().getLastFM().getCache().clear();
		if (bound)
			unbindService(connection);
		// TODO Auto-generated method stub

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// TODO Auto-generated method stub
	}

	private void doBindService() {
		Intent intent = new Intent(this, NewService.class);
		if (!app.isServiceRunning())
			startService(intent);
		if (!bound)
			bindService(intent, connection, 0);
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
		textArtist.setTypeface(getTypeface());

		textTitle = (TextView) page.findViewById(R.id.title);
		textTitle.setTypeface(getTypeface());

		albumImage = (ImageView) page.findViewById(R.id.imageAlbum);

		textBuffering = (TextView) page.findViewById(R.id.textBuffering);
		textBuffering.setTypeface(getTypeface());

		progressBuffering = (ProgressBar) page.findViewById(R.id.progressCircle);

		seekbar = (SeekBar) page.findViewById(R.id.seekBar);
		seekbar.setOnSeekBarChangeListener(this);

		textPassed = (TextView) page.findViewById(R.id.textPassed);
		textPassed.setTypeface(getTypeface());

		textLeft = (TextView) page.findViewById(R.id.textLeft);
		textLeft.setTypeface(getTypeface());

		btnLove = (Button) page.findViewById(R.id.btnLove);
		btnLove.setOnClickListener(this);

		Button btnShuffle = (Button) page.findViewById(R.id.btnShuffle);
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

		songsAdapter = new SongsAdapter(this);
		playlist = (ListView) page.findViewById(R.id.list);
		playlist.setAdapter(songsAdapter);
		TextView empty = (TextView) page.findViewById(android.R.id.empty);
		empty.setTypeface(getTypeface());
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
			showLoadingDialog(true);
			if (service.getPlayer().isPlaying())
				service.getPlayer().current = service.getPlayer().getCurrentSong();
			Log.d(VibesApplication.VIBES, "showing getsongs dialog");
		}

		private void getSongs(String... params) {
			try {
				NewActivity.this.getSongs(params[0]);
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
		protected Void doInBackground(String... params) {
			Thread.currentThread().setName("Getting songs");
			getSongs(params);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			hideLoadingDialog();
			viewPager.setCurrentItem(1, true);
			Player player = service.getPlayer();
			if (service.getPlayer().isPlaying()) {
				player.currentSong = -1;
			} else if (player.getSongs().size() > 0) {
				player.currentSong = 0;
				textArtist.setText(player.getCurrentSong().performer);
				textTitle.setText(player.getCurrentSong().title);
				textPassed.setText("0:00");
				textLeft.setText("0:00");
			}
			songsAdapter.currentSong = -1;
			songsAdapter.notifyDataSetChanged();
			playlist.setSelection(0);

			player.generateShuffleQueue();
		}
	}

	private class UpdateSongs extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressUpdating.setVisibility(View.VISIBLE);
		}

		private Integer updateSongs() {
			try {
				return NewActivity.this.updateSongs();
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
			if (result > 0 && service.getPlayer().currentSong != -1) {
				service.getPlayer().currentSong += result;
				if (songsAdapter.currentSong != -1)
					songsAdapter.currentSong += result;
			}
			songsAdapter.notifyDataSetChanged();
			service.getPlayer().generateShuffleQueue();
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
						return NewActivity.this.getAlbums(getUnit().id);
					else
						return getUnit().albums;
				} else {
					if (getMyAlbums() == null)
						return NewActivity.this.getAlbums(0);
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

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
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
					friends = getFriends();
				else
					groups = getGroups();
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
				return service.getPlayer().getLastFM().getAlbumImageURL(params[0], params[1]);
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
				Toast.makeText(NewActivity.this, getString(R.string.access_denied), Toast.LENGTH_LONG).show();
			}
		});
	}

	private void authFail() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(NewActivity.this, getString(R.string.authProblem), Toast.LENGTH_LONG).show();
				logOut();
			}
		});
	}

	private void unknownError() {
		// TODO stop playback
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(NewActivity.this, getString(R.string.unknownError), Toast.LENGTH_LONG).show();
			}
		});

	}

	private void internetFail() {
		// TODO stop playback
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(NewActivity.this, getString(R.string.no_internet), Toast.LENGTH_LONG).show();
			}
		});
	}

	private void hideLoadingDialog() {
		if (loadingDialog != null)
			loadingDialog.dismiss();
	}

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

			synchronized (this) {
				List<HttpPost> requests = player.getLastFM().getImageRequestQueue();
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

			textPassed.setText("0:00");
			textLeft.setText("0:00");
			seekbar.setProgress(0);
			seekbar.setSecondaryProgress(0);

			if (!fromPlaylist) {
				songsAdapter.currentSong = player.currentSong;
				songsAdapter.notifyDataSetChanged();
				playlist.smoothScrollToPosition(player.currentSong);
			}
		}
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
		textBuffering.setVisibility(View.VISIBLE);
		progressBuffering.setVisibility(View.VISIBLE);
		buffering = true;
	}

	@Override
	public void onBufferingEnded() {
		textBuffering.setVisibility(View.INVISIBLE);
		progressBuffering.setVisibility(View.INVISIBLE);
		buffering = false;
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
		case SEARCH:
			if (search == null)
				search = settings.getLastSearch();
			player.setSongs(vkontakte.search(search, 0));
			if (player.getSongs() != null) {
				settings.setLastSearch(search);
			}

		case MY_AUDIOS:
			player.setSongs(vkontakte.getAudios(settings.getOwnerId(), settings.getAlbumId(), 0, false));

		case WALL:
			player.setSongs(vkontakte.getWallAudios(settings.getOwnerId(), 0, false, false));

		case NEWSFEED:
			player.setSongs(vkontakte.getNewsFeedAudios(false));

		}
	}

	public int updateSongs() throws ClientProtocolException, IOException, VkontakteException {
		Player player = service.getPlayer();
		Vkontakte vkontakte = player.getVkontakte();
		Settings settings = app.getSettings();

		switch (settings.getPlaylist()) {
		case MY_AUDIOS:
			player.setSongs(vkontakte.getAudios(settings.getOwnerId(), settings.getAlbumId(), 0, true));
			break;

		case WALL:
			player.setSongs(vkontakte.getWallAudios(settings.getOwnerId(), 0, false, true));
			break;

		case NEWSFEED:
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

	@Override
	public void onAuthProblem() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onInternetProblem() {
		internetFail();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
		songsAdapter.fromPlaylist = true;
		songsAdapter.currentSong = position;
		songsAdapter.notifyDataSetChanged();
		Player player = service.getPlayer();
		if (player.getState() != State.STATE_PLAYING || player.getState() != State.STATE_PREPARING_FOR_PLAYBACK)
			btnPlay.setBackgroundResource(R.drawable.pause);
		player.currentSong = position;
		player.play();
		setCurrentSong(true);
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v) {
		Player player = service.getPlayer();
		Settings settings = app.getSettings();

		switch (v.getId()) {
		case R.id.btnPlay:
			v.startAnimation(shake);
			if (player.getState() == State.STATE_PAUSED_IDLING) {
				player.resume();
				v.setBackgroundResource(R.drawable.pause);
			} else if (player.isPlaying()) {
				player.pause();
				v.setBackgroundResource(R.drawable.play);
			} else if (player.getState() == State.STATE_NOT_PREPARED_IDLING && player.getCurrentSong() != null) {
				player.play();
				v.setBackgroundResource(R.drawable.pause);
			}
			break;

		case R.id.btnFwd:
			v.startAnimation(shake);
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
			service.download(player.currentSong);
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
				return player.getVkontakte().add(player.getCurrentSong());
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
			Thread.currentThread().setName("Loving song");
			own = settings.getPlaylist() == MY_AUDIOS && settings.getOwnerId() == 0;
			if (settings.getSession() != null)
				if (player.getLastFM().love(player.getCurrentSong())) {
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
		}
	}

	private class UnLove extends AsyncTask<Void, Void, Boolean> {

		boolean own;
		boolean lastUnloved;
		Player player = service.getPlayer();
		Settings settings = app.getSettings();

		private Boolean deleteSong() {
			try {
				return player.getVkontakte().delete(player.getCurrentSong());
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
			Thread.currentThread().setName("Unloving song");
			own = settings.getPlaylist() == MY_AUDIOS && settings.getOwnerId() == 0;
			if (settings.getSession() != null)
				if (player.getLastFM().unlove(player.getCurrentSong())) {
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
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser && service.getPlayer().isPlaying()) {
			onBufferingStrated();
			service.getPlayer().seekTo(progress);
		} else if (fromUser && (!service.getPlayer().isPlaying() || buffering))
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
			logOut();
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

	private void logOut() {
		app.getSettings().resetData();
		app.getSettings().setPlaylist(NEWSFEED);
		unbindService(connection);
		stopService(new Intent(NewActivity.this, NewService.class));
		startActivity(new Intent(NewActivity.this, LoginActivity.class));
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
			return new UnitDialog(this);

		case DIALOG_ALBUMS:
			return new AlbumsDialog(this);

		case DIALOG_LAST_FM_AUTH:
			return new LastFMLoginDialog(this);

		case DIALOG_LAST_FM_USER:
			return new LastFMUserDialog(this, imageLoader);

		case DIALOG_SEARCH:
			return new SearchDIalog(this);

		case DIALOG_UNITS:
			List<Unit> units = friendsList ? friends : groups;
			return new UnitsDialog(this, imageLoader, units);

		case DIALOG_PLAYLISTS:
			return new PlaylistsDialog(this);
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

	public NewService getService() {
		return service;
	}

	public List<Unit> getReadyFriends() {
		return friends;
	}

	public List<Unit> getReadyGroups() {
		return groups;
	}

}
