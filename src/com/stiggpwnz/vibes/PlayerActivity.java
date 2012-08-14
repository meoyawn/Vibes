package com.stiggpwnz.vibes;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.client.methods.HttpPost;

import com.stiggpwnz.vibes.Adapters.AlbumsAdapter;
import com.stiggpwnz.vibes.Adapters.TextAdapter;
import com.stiggpwnz.vibes.Adapters.UnitsAdapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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





	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
				app.setOwnerId(unit.id);
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
						app.setAlbumId(0);
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
					app.setAlbumId(albumsAdapter.getItem(position).id);
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
						app.setOwnerId(0);
						app.setAlbumId(0);
						new GetSongs().execute((String) null);
						dismissDialog(DIALOG_PLAYLISTS);
						break;

					case WALL:
						app.setPlaylist(position);
						app.setOwnerId(0);
						app.setAlbumId(0);
						new GetSongs().execute((String) null);
						dismissDialog(DIALOG_PLAYLISTS);
						break;

					case NEWSFEED:
						app.setPlaylist(position);
						new GetSongs().execute((String) null);
						dismissDialog(DIALOG_PLAYLISTS);
						break;

					case ALBUMS:
						app.setOwnerId(0);
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