package com.stiggpwnz.vibes;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.stiggpwnz.vibes.Adapters.SongsAdapter;
import com.stiggpwnz.vibes.Adapters.ViewPagerAdapter;
import com.stiggpwnz.vibes.NewService.ServiceBinder;

public class NewActivity extends Activity implements PlayerListener, OnClickListener, OnSeekBarChangeListener, OnItemClickListener {

	private static final int SEARCH = 0;
	private static final int FRIENDS = 1;
	private static final int GROUPS = 2;
	private static final int MY_AUDIOS = 3;
	private static final int WALL = 4;
	public static final int NEWSFEED = 5;
	private static final int ALBUMS = 6;

	private static final int UPDATE_PLAYLIST_TIMEOUT_SECONDS = 4;

	private static final int DIALOG_LAST_FM_AUTH = 56;
	private static final int DIALOG_SIGNING_IN = 35;
	private static final int DIALOG_LAST_FM_USER = 29;
	private static final int DIALOG_PLAYLISTS = 69;
	private static final int DIALOG_SEARCH = 75;
	private static final int DIALOG_UNITS = 76;
	private static final int DIALOG_UNIT = 77;
	private static final int DIALOG_ALBUMS = 78;

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

	private Button btnPlay;
	private View btnLove;

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
	private SongsAdapter songsAdapter;

	private Animation shake;
	private SeekBar seekbar;
	private ViewPager viewPager;
	private ListView playlist;
	private ImageLoader imageLoader;
	private GetAndSetAlbumImage getAlbumImage;
	private Unit unit;

	private List<View> pages;

	private boolean buffering;
	private boolean wasPlaying;
	private boolean paused;
	private boolean repeat;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (VibesApplication) getApplication();
		if (app.isServiceRunning()) {
			doBindService();
		} else {

		}

		app = (VibesApplication) getApplication();
		pages = new LinkedList<View>();
		imageLoader = new ImageLoader(this, R.drawable.music);
		new GetSongs().execute((String) null);
		initUI();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (app.isServiceRunning()) {
			doBindService();
		} else {

		}
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

	private void doBindService() {
		Intent intent = new Intent(this, NewService.class);
		bindService(intent, connection, Context.BIND_AUTO_CREATE);
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

	private class GetSongs extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showLoadingDialog(true);
			if (wasPlaying)
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
			if (wasPlaying) {
				service.getPlayer().currentSong = -1;
			} else if (service.getPlayer().getSongs().size() > 0) {
				service.getPlayer().currentSong = 0;
				textArtist.setText(service.getPlayer().getCurrentSong().performer);
				textTitle.setText(service.getPlayer().getCurrentSong().title);
				textPassed.setText("0:00");
				textLeft.setText("0:00");
			}
			songsAdapter.currentSong = -1;
			songsAdapter.notifyDataSetChanged();
			playlist.setSelection(0);

			service.generateShuffleQueue();
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
			if (result == 0) {

			} else if (app.currentSong != -1) {
				app.currentSong += result;
				if (songsAdapter.currentSong != -1)
					songsAdapter.currentSong += result;
			}
			songsAdapter.notifyDataSetChanged();
			sendCommand(PlayerService.MSG_SET_SHUFFLE, 1);
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
				if (app.getOwnerId() != 0) {
					if (unit.albums == null)
						return app.getAlbums(unit.id);
					else
						return unit.albums;
				} else {
					if (myAlbums == null)
						return app.getAlbums(0);
					else
						return myAlbums;
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
				if (app.getOwnerId() != 0)
					unit.albums = result;
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
			showLoadingDialog();
		}

		private Void getUnits() {
			try {
				if (frnds)
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
		} else
			loadingDialog.setMessage(getString(R.string.loading));
		loadingDialog.show();
	}

	public void accessDenied() {
		// TODO Auto-generated method stub

	}

	public void authFail() {
		// TODO Auto-generated method stub

	}

	public void unknownError() {
		// TODO Auto-generated method stub

	}

	public void internetFail() {
		// TODO Auto-generated method stub

	}

	private void hideLoadingDialog() {
		if (loadingDialog != null)
			loadingDialog.dismiss();
	}

	private void setCurrentSong(boolean fromPlaylist) {

		Song currentSong = service.getPlayer().getCurrentSong();
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
				List<HttpPost> requests = service.getPlayer().getLastFM().getImageRequestQueue();
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
				songsAdapter.fromPlaylist = false;
				songsAdapter.currentSong = service.getPlayer().currentSong;
				songsAdapter.notifyDataSetChanged();
				playlist.smoothScrollToPosition(service.getPlayer().currentSong);
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
		switch (app.getSettings().getPlaylist()) {
		case MY_AUDIOS:
			player.setSongs(vkontakte.getAudios(app.getSettings().getOwnerId(), app.getSettings().getAlbumId(), 0, true));
			break;

		case WALL:
			player.setSongs(vkontakte.getWallAudios(app.getSettings().getOwnerId(), 0, false, true));
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
		// TODO Auto-generated method stub

	}

}
