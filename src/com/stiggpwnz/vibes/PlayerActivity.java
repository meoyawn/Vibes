package com.stiggpwnz.vibes;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.client.methods.HttpPost;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class PlayerActivity extends Activity implements OnClickListener, OnSeekBarChangeListener, OnItemClickListener {

	public static final int SEARCH = 0;
	public static final int FRIENDS = 1;
	public static final int GROUPS = 2;
	public static final int MY_AUDIOS = 3;
	public static final int WALL = 4;
	public static final int NEWSFEED = 5;
	public static final int ALBUMS = 6;

	private static final int DIALOG_LAST_FM_AUTH = 56;
	private static final int DIALOG_SIGNING_IN = 35;
	private static final int DIALOG_LAST_FM_USER = 29;
	private static final int DIALOG_PLAYLISTS = 69;
	private static final int DIALOG_SEARCH = 75;
	private static final int DIALOG_UNITS = 76;
	private static final int DIALOG_UNIT = 77;
	private static final int DIALOG_ALBUMS = 78;

	private final Messenger activityMessenger = new Messenger(new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PlayerService.MSG_BUFFERING_STARTED:
				startBuffer();
				break;

			case PlayerService.MSG_BUFFERING_STOPPED:
				stopBuffer();
				if (msg.arg1 != 0) {
					songDuration = msg.arg1;
					seekbar.setMax(songDuration - 1);
				}
				break;

			case PlayerService.MSG_SET_CURRENT_SONG:
				setCurrentSong(msg.arg1, msg.arg2);
				break;

			case PlayerService.MSG_SET_PROGRESS:
				if (!buffering) {
					seekbarProgress(msg.arg1);
				}
				break;

			case PlayerService.MSG_ON_RESUME:
				Log.d(VibesApplication.VIBES, "setting current song from onResume");
				switch (msg.arg1) {
				case PlayerService.STATE_PLAYING:
					btnPlay.setBackgroundResource(R.drawable.pause);
					songDuration = msg.arg2;
					seekbar.setMax(songDuration - 1);
					setCurrentSong(app.currentSong, 0);
					stopBuffer();
					wasPlaying = true;
					paused = false;
					break;

				case PlayerService.STATE_PAUSED_IDLING:
					btnPlay.setBackgroundResource(R.drawable.play);
					songDuration = msg.arg2;
					seekbar.setMax(songDuration - 1);
					setCurrentSong(app.currentSong, 0);
					stopBuffer();
					wasPlaying = true;
					paused = true;
					break;

				case PlayerService.STATE_NOT_PREPARED_IDLING:
					btnPlay.setBackgroundResource(R.drawable.play);
					stopBuffer();
					wasPlaying = false;
					paused = false;
					setCurrentSong(app.currentSong, 0);
					break;

				case PlayerService.STATE_PREPARING_FOR_PLAYBACK:
					btnPlay.setBackgroundResource(R.drawable.pause);
					wasPlaying = true;
					paused = false;
					setCurrentSong(app.currentSong, 0);
					break;
				}
				break;

			case PlayerService.MSG_UPDATE_BUFFER:
				seekbar.setSecondaryProgress(msg.arg1);
				break;

			case PlayerService.MSG_UNBIND_SERVICE:
				doUnbindService();
				break;

			case PlayerService.MSG_AUTH_PROBLEM:
				authFail();
				break;

			case PlayerService.MSG_INTERNET_PROBLEM:
				btnPlay.setBackgroundResource(R.drawable.play);
				stopBuffer();
				seekbar.setProgress(0);
				seekbar.setSecondaryProgress(0);
				wasPlaying = false;
				paused = false;
				internetFail();
				break;

			default:
				super.handleMessage(msg);
			}
		}
	});
	private final ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			PlayerActivity.this.serviceMessenger = new Messenger(service);
			try {
				Message msg = Message.obtain(null, PlayerService.MSG_REGISTER_CLIENT);
				msg.replyTo = activityMessenger;
				PlayerActivity.this.serviceMessenger.send(msg);
			} catch (RemoteException e) {

			}
			bound = true;
			Log.d(VibesApplication.VIBES, "Service bounded");
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			serviceMessenger = null;
			bound = false;
			Log.d(VibesApplication.VIBES, "Service unbounded");
		}
	};

	private Button btnPlay;
	private Button btnUpdate;
	private Button btnFwd;
	private Button btnRwd;
	private Button btnLove;
	private Button btnShuffle;
	private Button btnRepeat;

	private TextView textArtist;
	private TextView textTitle;
	private TextView textBuffering;
	private TextView textPassed;
	private TextView textLeft;
	private TextView textLastFmUsername;

	private ProgressBar progressBuffering;
	private ProgressBar progressUpdating;

	private EditText editUsername;
	private EditText editPassword;

	private ImageView imageLastFmUser;
	private ImageView albumImage;

	private ProgressDialog gettingSongsDialog;
	private ProgressDialog loadingDialog;

	private ViewPagerAdapter pagerAdapter;
	private SongsAdapter songsAdapter;
	private AlbumsAdapter albumsAdapter;
	private UnitsAdapter unitsAdapter;

	private Messenger serviceMessenger;
	private Animation shake;
	private SeekBar seekbar;
	private ViewPager viewPager;
	private ListView playlist;
	private VibesApplication app;
	private ImageLoader imageLoader;
	private GetAndSetAlbumImage getAlbumImage;
	private Unit unit;

	private List<View> pages;
	private List<Unit> friends;
	private List<Unit> groups;
	private List<Album> myAlbums;

	private boolean bound;
	private boolean buffering;
	private boolean wasPlaying;
	private boolean paused;
	private boolean repeat;
	private boolean frnds;

	private int songDuration;

	private void doBindService() {
		if (!bound) {
			Intent intent = new Intent(PlayerActivity.this, PlayerService.class);
			bindService(intent, connection, BIND_AUTO_CREATE);
			bound = true;
		}
	}

	private void doUnbindService() {
		Log.d(VibesApplication.VIBES, "stopping service");
		if (bound) {
			if (serviceMessenger != null) {
				try {
					Message msg = Message.obtain(null, PlayerService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = activityMessenger;
					serviceMessenger.send(msg);
				} catch (RemoteException e) {

				}
			}
			unbindService(connection);
			bound = false;
		}
	}

	private void sendCommand(int command, int data) {
		if (bound) {
			if (serviceMessenger != null) {
				try {
					Message msg = Message.obtain(null, command, data, 0);
					msg.replyTo = activityMessenger;
					serviceMessenger.send(msg);
				} catch (RemoteException e) {
					serviceMessenger = null;
				}
			}
		}
	}

	private void setCurrentSong(int currentSongService, int fromPlaylist) {

		VibesApplication app = this.app;
		SongsAdapter songsAdapter = this.songsAdapter;
		GetAndSetAlbumImage getAlbumImage = this.getAlbumImage;
		SeekBar seekbar = this.seekbar;

		if (app.getCurrentSong() != null) {
			String performer = app.getCurrentSong().performer;
			textArtist.setText(performer);

			String name = app.getCurrentSong().title;
			textTitle.setText(name);

			if (app.getCurrentSong().loved)
				btnLove.setBackgroundResource(R.drawable.love_blue);
			else
				btnLove.setBackgroundResource(R.drawable.love_grey);

			synchronized (this) {
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

			textPassed.setText("0:00");
			textLeft.setText("0:00");
			seekbar.setProgress(0);
			seekbar.setSecondaryProgress(0);
			if (fromPlaylist == 0) {
				songsAdapter.fromPlaylist = false;
				songsAdapter.currentSong = currentSongService;
				songsAdapter.notifyDataSetChanged();
				playlist.smoothScrollToPosition(app.currentSong);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (VibesApplication) getApplication();

		pages = new LinkedList<View>();
		imageLoader = new ImageLoader(this, R.drawable.music);
		new GetSongs().execute((String) null);
		initUI();
		checkIfServiceIsRunning();

	}

	@Override
	public void onBackPressed() {
		Log.d(VibesApplication.VIBES, "onBackPressed");
		moveTaskToBack(true);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(VibesApplication.VIBES, "onPaused");
		sendCommand(PlayerService.MSG_ON_PAUSE_SERVICE, 0);
		imageLoader.getMemoryCache().clear();
		app.getLastFM().getCache().clear();
	}

	@Override
	protected void onStop() {
		super.onStop();
		app.getVkontakte().getCache().clear();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (app.songs != null)
			doBindService();
		Log.d(VibesApplication.VIBES, "onResumed");
		sendCommand(PlayerService.MSG_ON_RESUME_SERVICE, 0);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (bound) {
			doUnbindService();
		}
		cancelNotification();
		Log.d(VibesApplication.VIBES, "onDestroyed");
	}

	private void initUI() {
		setContentView(R.layout.player);

		initPlayerPage();

		btnPlay = (Button) findViewById(R.id.btnPlay);
		btnPlay.setOnClickListener(this);

		btnFwd = (Button) findViewById(R.id.btnFwd);
		btnFwd.setOnClickListener(this);

		btnRwd = (Button) findViewById(R.id.btnRwd);
		btnRwd.setOnClickListener(this);

		Button btnPlaylist = (Button) findViewById(R.id.btnPlaylist);
		btnPlaylist.setOnClickListener(this);

		pagerAdapter = new ViewPagerAdapter(pages);

		viewPager = (ViewPager) findViewById(R.id.viewpager);
		viewPager.setAdapter(pagerAdapter);
		// viewPager.setCurrentItem(0);

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
		textArtist.setTypeface(app.getTypeface());

		textTitle = (TextView) page.findViewById(R.id.title);
		textTitle.setTypeface(app.getTypeface());

		albumImage = (ImageView) page.findViewById(R.id.imageAlbum);

		textBuffering = (TextView) page.findViewById(R.id.textBuffering);
		textBuffering.setTypeface(app.getTypeface());

		progressBuffering = (ProgressBar) page.findViewById(R.id.progressCircle);

		seekbar = (SeekBar) page.findViewById(R.id.seekBar);
		seekbar.setOnSeekBarChangeListener(this);

		textPassed = (TextView) page.findViewById(R.id.textPassed);
		textPassed.setTypeface(app.getTypeface());

		textLeft = (TextView) page.findViewById(R.id.textLeft);
		textLeft.setTypeface(app.getTypeface());

		btnLove = (Button) page.findViewById(R.id.btnLove);
		btnLove.setOnClickListener(this);

		btnShuffle = (Button) page.findViewById(R.id.btnShuffle);
		btnShuffle.setOnClickListener(this);
		if (app.getShuffle())
			btnShuffle.setBackgroundResource(R.drawable.shuffle_blue);
		else
			btnShuffle.setBackgroundResource(R.drawable.shuffle_grey);

		btnRepeat = (Button) page.findViewById(R.id.btnRepeat);
		btnRepeat.setOnClickListener(this);

		pages.add(page);
	}

	private void initPlaylistPage() {
		LayoutInflater inflater = LayoutInflater.from(this);
		View page = inflater.inflate(R.layout.playlist, null);

		Button btnBack = (Button) page.findViewById(R.id.btnBack);
		btnBack.setOnClickListener(this);

		btnUpdate = (Button) page.findViewById(R.id.btnUpdate);
		btnUpdate.setOnClickListener(this);

		progressUpdating = (ProgressBar) page.findViewById(R.id.progressUpdating);

		songsAdapter = new SongsAdapter(this);
		playlist = (ListView) page.findViewById(R.id.list);
		playlist.setAdapter(songsAdapter);
		TextView empty = (TextView) page.findViewById(android.R.id.empty);
		empty.setTypeface(app.getTypeface());
		playlist.setEmptyView(empty);
		playlist.setOnItemClickListener(this);

		pages.add(page);
		pagerAdapter.notifyDataSetChanged();
	}

	private void startBuffer() {
		textBuffering.setVisibility(View.VISIBLE);
		progressBuffering.setVisibility(View.VISIBLE);
		buffering = true;
	}

	private void stopBuffer() {
		textBuffering.setVisibility(View.INVISIBLE);
		progressBuffering.setVisibility(View.INVISIBLE);
		buffering = false;
	}

	private void checkIfServiceIsRunning() {
		if (PlayerService.isRunning && !bound) {
			doBindService();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnPlay:
			btnPlay.startAnimation(shake);
			if (paused) {
				sendCommand(PlayerService.MSG_RESUME, 0);
				btnPlay.setBackgroundResource(R.drawable.pause);
				paused = false;
				wasPlaying = true;
			} else {
				if (wasPlaying) {
					sendCommand(PlayerService.MSG_PAUSE, 0);
					btnPlay.setBackgroundResource(R.drawable.play);
					paused = true;
				} else {
					if (app.getCurrentSong() != null) {
						sendCommand(PlayerService.MSG_PLAY, 0);
						btnPlay.setBackgroundResource(R.drawable.pause);
						wasPlaying = true;
					}
				}
			}
			break;

		case R.id.btnFwd:
			if (paused && app.songs.size() > 0) {
				wasPlaying = false;
				paused = false;
			}
			btnFwd.startAnimation(shake);
			sendCommand(PlayerService.MSG_NEXT, 0);
			break;

		case R.id.btnRwd:
			btnRwd.startAnimation(shake);
			sendCommand(PlayerService.MSG_PREV, 0);
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

		case R.id.btnSingIn:
			if (editUsername.getText().length() > 0 && editPassword.getText().length() > 0)
				new LastFmSignIn().execute();
			break;

		case R.id.btnSingOut:
			app.resetLastFM();
			dismissDialog(DIALOG_LAST_FM_USER);
			showDialog(DIALOG_LAST_FM_AUTH);
			break;

		case R.id.btnLove:
			if (app.getCurrentSong().loved) {
				unlove();
			} else {
				love();
			}
			break;

		case R.id.btnShuffle:
			if (app.getShuffle()) {
				btnShuffle.setBackgroundResource(R.drawable.shuffle_grey);
				sendCommand(PlayerService.MSG_SET_SHUFFLE, 0);
				app.setShuffle(false);
			} else {
				btnShuffle.setBackgroundResource(R.drawable.shuffle_blue);
				sendCommand(PlayerService.MSG_SET_SHUFFLE, 1);
				app.setShuffle(true);
			}
			break;

		case R.id.btnRepeat:
			if (repeat) {
				btnRepeat.setBackgroundResource(R.drawable.repeat_grey);
				sendCommand(PlayerService.MSG_SET_REPEAT, 0);
				repeat = false;
			} else {
				btnRepeat.setBackgroundResource(R.drawable.repeat_blue);
				sendCommand(PlayerService.MSG_SET_REPEAT, 1);
				repeat = true;
			}
			break;

		case R.id.btnPlaylist:
			showDialog(DIALOG_PLAYLISTS);
			break;

		case R.id.btnDownload:
			Log.d(VibesApplication.VIBES, "starting download");
			sendCommand(PlayerService.MSG_DOWNLOAD, 0);
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
		songsAdapter.fromPlaylist = true;
		songsAdapter.currentSong = position;
		songsAdapter.notifyDataSetChanged();
		if (!wasPlaying || paused)
			btnPlay.setBackgroundResource(R.drawable.pause);
		sendCommand(PlayerService.MSG_SET_CURRENT_SONG_SERVICE, position);
		paused = false;
		wasPlaying = true;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser && wasPlaying) {
			int secondary = seekBar.getSecondaryProgress();
			startBuffer();
			if (progress > secondary) {
				seekBar.setProgress(secondary);
				sendCommand(PlayerService.MSG_SEEK_TO, secondary);
			} else
				sendCommand(PlayerService.MSG_SEEK_TO, progress);
		} else if (fromUser && (!wasPlaying || buffering))
			seekBar.setProgress(0);
	}

	private void love() {
		btnLove.setBackgroundResource(R.drawable.love_blue);
		new Love().execute();
	}

	private void unlove() {
		btnLove.setBackgroundResource(R.drawable.love_grey);
		new UnLove().execute();
	}

	private class LastFmSignIn extends AsyncTask<Void, Void, String[]> {

		private String username;
		private String password;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showDialog(DIALOG_SIGNING_IN);
			username = editUsername.getText().toString();
			password = editPassword.getText().toString();
		}

		@Override
		protected String[] doInBackground(Void... params) {
			Thread.currentThread().setName("Signing in to LastFM");
			return app.getLastFM().auth(username, password);
		}

		@Override
		protected void onPostExecute(String[] result) {
			super.onPostExecute(result);
			dismissDialog(DIALOG_SIGNING_IN);
			if (result != null) {
				app.saveLastFM(result);
				dismissDialog(DIALOG_LAST_FM_AUTH);
				Toast.makeText(PlayerActivity.this, R.string.last_success, Toast.LENGTH_SHORT).show();
			} else
				Toast.makeText(PlayerActivity.this, R.string.authProblem, Toast.LENGTH_LONG).show();
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

	private class Love extends AsyncTask<Void, Void, Integer> {

		boolean own;
		boolean lastLoved;

		private Integer addSong() {
			try {
				return app.getVkontakte().add(app.getCurrentSong());
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
			own = app.getPlaylist() == MY_AUDIOS && app.getOwnerId() == 0;
			if (app.getSession() != null)
				if (app.getLastFM().love(app.getCurrentSong())) {
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
					app.getCurrentSong().myAid = result;
					app.getCurrentSong().loved = true;
				} else
					btnLove.setBackgroundResource(R.drawable.love_grey);

			} else if (app.getSession() != null) {

				if (lastLoved)
					app.getCurrentSong().loved = true;
				else
					btnLove.setBackgroundResource(R.drawable.love_grey);
			} else {
				app.getCurrentSong().loved = true;
			}
		}
	}

	private void cancelNotification() {
		app.getNotificationManager().cancel(VibesApplication.SONG, VibesApplication.NOTIFICATION);
	}

	private class UnLove extends AsyncTask<Void, Void, Boolean> {

		boolean own;
		boolean lastUnloved;

		private Boolean deleteSong() {
			try {
				return app.getVkontakte().delete(app.getCurrentSong());
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
			own = app.getPlaylist() == MY_AUDIOS && app.getOwnerId() == 0;
			if (app.getSession() != null)
				if (app.getLastFM().unlove(app.getCurrentSong())) {
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
					app.getCurrentSong().myAid = 0;
					app.getCurrentSong().loved = false;
				} else {
					btnLove.setBackgroundResource(R.drawable.love_blue);
				}
			} else if (app.getSession() != null) {

				if (lastUnloved)
					app.getCurrentSong().loved = false;
				else
					btnLove.setBackgroundResource(R.drawable.love_blue);
			} else {
				app.getCurrentSong().loved = false;
			}
		}
	}

	private void showGettingSongs() {

		gettingSongsDialog = new ProgressDialog(this);
		gettingSongsDialog.setIndeterminate(true);
		gettingSongsDialog.setCancelable(false);
		switch (app.getPlaylist()) {
		case SEARCH:
			gettingSongsDialog.setMessage(getString(R.string.searchingSongs));
			break;

		case MY_AUDIOS:
			if (app.getAlbumId() == 0)
				gettingSongsDialog.setMessage(getString(R.string.gettingSongsAudios));
			else
				gettingSongsDialog.setMessage(getString(R.string.gettingSongsAlbum));
			break;

		case WALL:
			gettingSongsDialog.setMessage(getString(R.string.gettingSongsWall));
			break;

		case NEWSFEED:
			gettingSongsDialog.setMessage(getString(R.string.gettingSongsNewsfeed));
			break;

		}
		gettingSongsDialog.show();
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
				app.resetData();
				app.setPlaylist(NEWSFEED);
				startActivity(new Intent(PlayerActivity.this, LoginActivity.class));
				finish();
			}
		});
	}

	private void internetFail() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(PlayerActivity.this, getString(R.string.no_internet), Toast.LENGTH_LONG).show();
			}
		});
	}

	private void hideGettingSongs() {
		if (gettingSongsDialog != null)
			gettingSongsDialog.dismiss();
	}

	private void showLoadingDialog() {
		loadingDialog = new ProgressDialog(this);
		loadingDialog.setIndeterminate(true);
		loadingDialog.setCancelable(false);
		loadingDialog.setMessage(getString(R.string.loading));
		loadingDialog.show();
	}

	private void hideLoadingDialog() {
		if (loadingDialog != null)
			loadingDialog.dismiss();
	}

	private class GetSongs extends AsyncTask<String, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showGettingSongs();
			if (wasPlaying)
				app.current = app.getCurrentSong();
			Log.d(VibesApplication.VIBES, "showing getsongs dialog");
		}

		private Boolean getSongs(String... params) {
			try {
				return app.getSongs(params[0]);
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
					return getSongs(params);

				case VkontakteException.ACCESS_DENIED:
					accessDenied();
					app.setPlaylist(NEWSFEED);
					break;

				case VkontakteException.PERMISSION_TO_PERFORM_THIS_ACTION_IS_DENIED_BY_USER:
					accessDenied();
					app.setPlaylist(NEWSFEED);
					break;

				default:
					return false;
				}
			}
			return false;
		}

		@Override
		protected Boolean doInBackground(String... params) {
			Thread.currentThread().setName("Getting songs");
			return getSongs(params);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			hideGettingSongs();
			if (!result) {
				// authFail();
			} else {
				viewPager.setCurrentItem(1, true);
				if (wasPlaying) {
					app.currentSong = -1;
				} else if (app.songs.size() > 0) {
					app.currentSong = 0;
					textArtist.setText(app.getCurrentSong().performer);
					textTitle.setText(app.getCurrentSong().title);
					textPassed.setText("0:00");
					textLeft.setText("0:00");
				}
				songsAdapter.currentSong = -1;
				songsAdapter.notifyDataSetChanged();
				playlist.setSelection(0);

				doBindService();
				if (app.getShuffle())
					sendCommand(PlayerService.MSG_SET_SHUFFLE, 1);
			}
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
			showLoadingDialog();
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

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {

	}

	private void unknownError() {

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(PlayerActivity.this, getString(R.string.unknownError), Toast.LENGTH_LONG).show();
			}
		});

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

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
			sendCommand(PlayerService.MSG_PAUSE, 0);
			app.resetData();
			app.setPlaylist(NEWSFEED);
			doUnbindService();
			startActivity(new Intent(this, LoginActivity.class).putExtra(LoginActivity.RESET, true));
			finish();
			return true;

		case R.id.itemLastFM:
			if (app.getSession() == null)
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

	@Override
	protected Dialog onCreateDialog(int id) {

		final OnItemClickListener onUnitClickListener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> list, View view, int position, long id) {
				unit = (Unit) list.getItemAtPosition(position);
				app.setOwner(unit.id);
				showDialog(DIALOG_UNIT);
			}
		};

		switch (id) {
		case DIALOG_UNIT:
			Dialog unitDialog = new Dialog(PlayerActivity.this);
			unitDialog.setContentView(R.layout.list);
			unitDialog.setCanceledOnTouchOutside(true);
			ListView unitList = (ListView) unitDialog.findViewById(R.id.listView);
			String[] unitOptions = getResources().getStringArray(R.array.unit_options);
			TextAdapter unitAdapter = new TextAdapter(this, unitOptions);
			unitList.setAdapter(unitAdapter);
			unitList.setEmptyView(unitDialog.findViewById(android.R.id.empty));
			unitList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> list, View view, int position, long id) {

					switch (position) {
					case 0:
						app.setPlaylist(MY_AUDIOS);
						app.setAlbum(0);
						new GetSongs().execute((String) null);
						dismissDialog(DIALOG_UNIT);
						dismissDialog(DIALOG_UNITS);
						dismissDialog(DIALOG_PLAYLISTS);
						break;

					case 1:
						app.setPlaylist(WALL);
						new GetSongs().execute((String) null);
						dismissDialog(DIALOG_UNIT);
						dismissDialog(DIALOG_UNITS);
						dismissDialog(DIALOG_PLAYLISTS);
						break;

					case 2:
						new GetAlbums().execute();
						break;
					}

				}
			});
			return unitDialog;

		case DIALOG_ALBUMS:
			Dialog albums = new Dialog(this);
			albums.setContentView(R.layout.list);
			albums.setTitle(getResources().getStringArray(R.array.unit_options)[2]);
			albums.setCanceledOnTouchOutside(true);
			ListView albumsList = (ListView) albums.findViewById(R.id.listView);
			List<Album> albumList = app.getOwnerId() != 0 ? unit.albums : myAlbums;
			albumsAdapter = new AlbumsAdapter(this, albumList);
			albumsList.setAdapter(albumsAdapter);

			TextView emptyAlbum = (TextView) albums.findViewById(android.R.id.empty);
			emptyAlbum.setTypeface(app.getTypeface());
			albumsList.setEmptyView(emptyAlbum);

			albumsList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> list, View view, int position, long id) {
					app.setPlaylist(MY_AUDIOS);
					app.setAlbum(albumsAdapter.getItem(position).id);
					new GetSongs().execute((String) null);
					dismissDialog(DIALOG_ALBUMS);
					if (app.getOwnerId() != 0) {
						dismissDialog(DIALOG_UNIT);
						dismissDialog(DIALOG_UNITS);
					}
					dismissDialog(DIALOG_PLAYLISTS);
				}
			});
			return albums;

		case DIALOG_LAST_FM_AUTH:
			final Dialog lastAuth = new Dialog(this);
			lastAuth.setContentView(R.layout.last_auth);
			lastAuth.setTitle(R.string.auth);
			lastAuth.setCanceledOnTouchOutside(true);
			editUsername = (EditText) lastAuth.findViewById(R.id.editUsername);
			editPassword = (EditText) lastAuth.findViewById(R.id.editPassword);
			Button btnSignIn = (Button) lastAuth.findViewById(R.id.btnSingIn);
			btnSignIn.setOnClickListener(this);
			return lastAuth;

		case DIALOG_LAST_FM_USER:
			Dialog lastUser = new Dialog(this);
			lastUser.setContentView(R.layout.last_user);
			lastUser.setTitle(R.string.account);
			lastUser.setCanceledOnTouchOutside(true);
			Button btnSignOut = (Button) lastUser.findViewById(R.id.btnSingOut);
			btnSignOut.setOnClickListener(this);
			textLastFmUsername = (TextView) lastUser.findViewById(R.id.textUser);
			imageLastFmUser = (ImageView) lastUser.findViewById(R.id.imageUser);
			if (app.getUsername() != null) {
				imageLoader.setStubId(R.drawable.last_fm_logo);
				imageLoader.displayImage(app.getUserImage(), imageLastFmUser);
			}
			return lastUser;

		case DIALOG_SIGNING_IN:
			ProgressDialog auth = new ProgressDialog(this);
			auth.setMessage(getString(R.string.auth));
			auth.setIndeterminate(true);
			auth.setCancelable(false);
			return auth;

		case DIALOG_SEARCH:
			Dialog search = new Dialog(this);
			search.setContentView(R.layout.search);
			search.setTitle(getResources().getStringArray(R.array.playlist_options)[0]);
			search.setCanceledOnTouchOutside(true);
			EditText searchView = (EditText) search.findViewById(R.id.autocomplete);
			searchView.setOnEditorActionListener(new OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					app.setPlaylist(0);
					Log.d(VibesApplication.VIBES, "performing search with param: " + v.getText().toString());
					new GetSongs().execute(v.getText().toString());
					dismissDialog(DIALOG_SEARCH);
					dismissDialog(DIALOG_PLAYLISTS);
					return true;
				}
			});

			Window window = search.getWindow();
			window.setGravity(Gravity.TOP);

			LayoutParams params = window.getAttributes();
			params.width = LayoutParams.FILL_PARENT;
			window.setAttributes((android.view.WindowManager.LayoutParams) params);

			return search;

		case DIALOG_UNITS:
			Dialog units = new Dialog(this);
			units.setContentView(R.layout.list);

			units.setCanceledOnTouchOutside(true);
			ListView unitsList = (ListView) units.findViewById(R.id.listView);

			List<Unit> unts = frnds ? friends : groups;
			unitsAdapter = new UnitsAdapter(this, unts, imageLoader);

			unitsList.setAdapter(unitsAdapter);
			unitsList.setEmptyView(units.findViewById(android.R.id.empty));
			unitsList.setOnItemClickListener(onUnitClickListener);
			return units;

		case DIALOG_PLAYLISTS:
			Dialog playlists = new Dialog(this);
			playlists.setContentView(R.layout.list);
			playlists.setTitle(R.string.playlist);
			playlists.setCanceledOnTouchOutside(true);
			ListView listView = (ListView) playlists.findViewById(R.id.listView);
			String[] options = getResources().getStringArray(R.array.playlist_options);
			TextAdapter adapter = new TextAdapter(this, options);
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> list, View view, int position, long id) {
					switch (position) {

					case SEARCH:
						showDialog(DIALOG_SEARCH);
						break;

					case FRIENDS:
						frnds = true;
						if (PlayerActivity.this.friends != null)
							showDialog(DIALOG_UNITS);
						else
							new GetUnits().execute();
						break;

					case GROUPS:
						frnds = false;
						if (PlayerActivity.this.groups != null)
							showDialog(DIALOG_UNITS);
						else
							new GetUnits().execute();
						break;

					case MY_AUDIOS:
						app.setPlaylist(position);
						app.setOwner(0);
						app.setAlbum(0);
						new GetSongs().execute((String) null);
						dismissDialog(DIALOG_PLAYLISTS);
						break;

					case WALL:
						app.setPlaylist(position);
						app.setOwner(0);
						app.setAlbum(0);
						new GetSongs().execute((String) null);
						dismissDialog(DIALOG_PLAYLISTS);
						break;

					case NEWSFEED:
						app.setPlaylist(position);
						new GetSongs().execute((String) null);
						dismissDialog(DIALOG_PLAYLISTS);
						break;

					case ALBUMS:
						app.setOwner(0);
						new GetAlbums().execute();
						break;
					}
				}
			});
			return playlists;

		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {

		case DIALOG_ALBUMS:
			List<Album> albumList;
			if (app.getOwnerId() != 0 && unit != null)
				albumList = unit.albums;
			else
				albumList = myAlbums;
			albumsAdapter.setAlbums(albumList);
			albumsAdapter.notifyDataSetChanged();
			break;

		case DIALOG_LAST_FM_USER:

			if (app.getUsername() != null) {
				textLastFmUsername.setText(app.getUsername());
				imageLoader.setStubId(R.drawable.last_fm_logo);
				imageLoader.displayImage(app.getUserImage(), imageLastFmUser);
			}
			break;

		case DIALOG_UNITS:
			String[] array = getResources().getStringArray(R.array.playlist_options);
			String title = frnds ? array[1] : array[2];
			dialog.setTitle(title);

			unitsAdapter.setUnits(frnds ? friends : groups);
			unitsAdapter.notifyDataSetChanged();
			break;

		case DIALOG_UNIT:
			if (unit != null && unit.name != null)
				dialog.setTitle(unit.name);
			break;
		}
	}

	private void seekbarProgress(int progress) {
		seekbar.setProgress(progress);
		int seconds = (progress / 1000) % 60;
		int minutes = (progress / 1000) / 60;
		if (seconds > 9)
			textPassed.setText(String.format("%d:%d", minutes, seconds));
		else
			textPassed.setText(String.format("%d:0%d", minutes, seconds));
		seconds = ((songDuration - progress) / 1000) % 60;
		minutes = ((songDuration - progress) / 1000) / 60;
		if (seconds > 9)
			textLeft.setText(String.format("%d:%d", minutes, seconds));
		else
			textLeft.setText(String.format("%d:0%d", minutes, seconds));
	}

}