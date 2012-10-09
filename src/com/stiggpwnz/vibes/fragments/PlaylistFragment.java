package com.stiggpwnz.vibes.fragments;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.VibesApplication;
import com.stiggpwnz.vibes.adapters.PlaylistAdapter;
import com.stiggpwnz.vibes.restapi.Playlist;
import com.stiggpwnz.vibes.restapi.Playlist.Type;
import com.stiggpwnz.vibes.restapi.Song;
import com.stiggpwnz.vibes.restapi.Unit;
import com.stiggpwnz.vibes.restapi.VKontakteException;

public class PlaylistFragment extends SherlockListFragment {

	public static interface Listener extends FragmentListener {

		public List<Song> loadSongs(Playlist playlist) throws IOException, VKontakteException;

		public List<Song> updateSongs(Playlist playlist) throws IOException, VKontakteException;

		public void play(int position, boolean hardReset);

		public void download(int position);

		public void remove(int position);

		public Unit getSelf();

		public void loveUnlove(int position);

		public void onPlaylistLoaded();

		public Playlist getPlaylist();

		public void setPlaylist(Playlist playlist);

		public int getCurrentTrack();

		public void setPlaylistLoading(boolean loading);

		public boolean isPlaying();

		public void onPlaylistUpdated(int newTracks);

	}

	public static interface OnItemsReadyListener {
		void onItemsReady(List<Song> items);
	}

	private static final int CONTEXT_LOVE_UNLOVE = 0;
	private static final int CONTEXT_REMOVE = 1;
	private static final int CONTEXT_DOWNLOAD = 2;

	private Listener listener;
	private PlaylistAdapter adapter;

	private View progress;
	private View list;
	private View buffering;

	private TextView textPassed;
	private TextView textLeft;
	private TextView textBuffering;

	private boolean large;
	private boolean isLoading;

	public PlaylistFragment() {
		Log.d(VibesApplication.VIBES, "creating new playlist");
	}

	@Override
	public void onAttach(Activity activity) {
		listener = (Listener) activity;
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(VibesApplication.VIBES, "onCreateView playlist");
		return inflater.inflate(R.layout.playlist, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Log.d(VibesApplication.VIBES, "onViewCreated playlist");
		super.onViewCreated(view, savedInstanceState);

		progress = view.findViewById(R.id.progressBar);
		list = view.findViewById(R.id.layout);
		((TextView) view.findViewById(android.R.id.empty)).setTypeface(listener.getTypeface());

		textPassed = (TextView) view.findViewById(R.id.textPassed);
		if (textPassed != null) {
			list = view.findViewById(R.id.layout);

			textPassed.setTypeface(listener.getTypeface());

			TextView textView = (TextView) view.findViewById(R.id.textView1);
			textView.setTypeface(listener.getTypeface());

			textLeft = (TextView) view.findViewById(R.id.textLeft);
			textLeft.setTypeface(listener.getTypeface());

			buffering = view.findViewById(R.id.progressBuffering);
			textBuffering = (TextView) view.findViewById(R.id.textBuffering);
			textBuffering.setTypeface(listener.getTypeface());

			large = true;
		}
		registerForContextMenu(getListView());
		listener.onViewCreated(this);

		Playlist selectedPlaylist = listener.getSelectedPlaylist();
		if (selectedPlaylist != null) {
			List<Song> songs = selectedPlaylist.songs;
			if (songs != null) {
				if (adapter != null) {
					adapter.setSongs(songs);
					getListView().setSelection(0);
				} else {
					adapter = new PlaylistAdapter(getSherlockActivity(), songs, listener.getTypeface());
					setListAdapter(adapter);
				}
				boolean isPlaying = listener.isPlaying();
				if (!selectedPlaylist.equals(listener.getPlaylist())) {
					adapter.currentTrack = -1;
					adapter.notifyDataSetChanged();
				} else if (isPlaying) {
					adapter.currentTrack = listener.getCurrentTrack();
					adapter.notifyDataSetChanged();
				}
			} else {
				new LoadSongs().execute();
			}
			getListView().setSelectionAfterHeaderView();
		}
	}

	@Override
	public void onDetach() {
		listener = null;
		super.onDetach();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		adapter.fromPlaylist = true;
		adapter.currentTrack = position;
		adapter.notifyDataSetChanged();

		boolean hardReset = false;
		Playlist selectedPlaylist = listener.getSelectedPlaylist();
		if (!selectedPlaylist.equals(listener.getPlaylist())) {
			listener.setPlaylist(selectedPlaylist);
			hardReset = true;
		}
		listener.play(position, hardReset);
		super.onListItemClick(l, v, position, id);
	}

	@Override
	public void setListShown(boolean shown) {
		if (shown) {
			progress.setVisibility(View.INVISIBLE);
			list.setVisibility(View.VISIBLE);
		} else {
			list.setVisibility(View.INVISIBLE);
			progress.setVisibility(View.VISIBLE);
		}
		listener.setPlaylistLoading(!shown);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		int position = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
		menu.setHeaderTitle(adapter.getItem(position).toString());

		String[] options = getResources().getStringArray(R.array.context_options);
		if (adapter.getItem(position).loved)
			options[0] = getString(R.string.remove_unlove);

		for (int i = 0; i < options.length; i++)
			menu.add(Menu.NONE, i, i, options[i]);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int position = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;
		int index = item.getOrder();
		switch (index) {
		case CONTEXT_LOVE_UNLOVE:
			listener.loveUnlove(position);
			return true;

		case CONTEXT_REMOVE:
			listener.remove(position);
			return true;

		case CONTEXT_DOWNLOAD:
			listener.download(position);
			return true;
		}
		return false;
	}

	public void loadPlaylist(boolean isPlaying) {
		Playlist playlist = listener.getSelectedPlaylist();
		List<Song> songs = playlist.songs;
		if (songs != null) {
			if (adapter != null) {
				adapter.setSongs(songs);
			} else {
				adapter = new PlaylistAdapter(getSherlockActivity(), songs, listener.getTypeface());
				setListAdapter(adapter);
			}
			if (!playlist.equals(listener.getPlaylist())) {
				adapter.currentTrack = -1;
				adapter.notifyDataSetChanged();
			} else if (isPlaying) {
				adapter.currentTrack = listener.getCurrentTrack();
				adapter.notifyDataSetChanged();
			}
		} else {
			new LoadSongs().execute();
		}
		getListView().setSelectionAfterHeaderView();
	}

	private class LoadSongs extends AsyncTask<Void, Void, List<Song>> {

		private Playlist playlist;

		@Override
		protected void onPreExecute() {
			setListShown(false);
			isLoading = true;
			playlist = listener.getSelectedPlaylist();
			super.onPreExecute();
		}

		private List<Song> loadSongs() {
			try {
				if (listener != null)
					return listener.loadSongs(playlist);
			} catch (IOException e) {
				if (listener != null)
					listener.internetFail();
			} catch (VKontakteException e) {
				if (listener != null) {
					switch (e.getCode()) {
					case VKontakteException.UNKNOWN_ERROR_OCCURED:
						listener.unknownError();
						break;

					case VKontakteException.USER_AUTHORIZATION_FAILED:
						listener.authFail();
						break;

					case VKontakteException.TOO_MANY_REQUESTS_PER_SECOND:
						return loadSongs();

					case VKontakteException.ACCESS_DENIED:
						listener.accessDenied();
						break;

					case VKontakteException.PERMISSION_TO_PERFORM_THIS_ACTION_IS_DENIED_BY_USER:
						listener.accessDenied();
						break;
					}
				}
			}
			return null;
		}

		@Override
		protected List<Song> doInBackground(Void... params) {
			Thread.currentThread().setName("Getting songs");
			if (listener != null)
				return loadSongs();
			return null;
		}

		@Override
		protected void onPostExecute(List<Song> result) {
			super.onPostExecute(result);
			playlist.songs = result;
			if (listener != null) {
				if (adapter == null) {
					adapter = new PlaylistAdapter(getSherlockActivity(), result, listener.getTypeface());
					setListAdapter(adapter);
				} else {
					adapter.setSongs(result);
				}
				setListShown(true);
				if (!listener.getSelectedPlaylist().equals(listener.getPlaylist())) {
					adapter.currentTrack = -1;
					adapter.notifyDataSetChanged();
				} else {
					listener.onPlaylistLoaded();
				}
			}
			isLoading = false;
		}
	}

	public PlaylistAdapter getAdapter() {
		return adapter;
	}

	public void nullEverything() {
		if (large) {
			textPassed.setText("0:00");
			textLeft.setText("0:00");
		}
	}

	public void setCurrentSong(int position) {
		if (adapter != null && listener != null && listener.getSelectedPlaylist().equals(listener.getPlaylist()) && listener.isPlaying()) {
			adapter.currentTrack = position;
			adapter.notifyDataSetChanged();
			getListView().smoothScrollToPosition(position);
		}
	}

	public void onBufferingStarted() {
		if (large) {
			textBuffering.setVisibility(View.VISIBLE);
			buffering.setVisibility(View.VISIBLE);
		}
	}

	public void onBufferingEnded() {
		if (large) {
			textBuffering.setVisibility(View.INVISIBLE);
			buffering.setVisibility(View.INVISIBLE);
		}
	}

	public void onProgressChanged(int progress, int songDuration) {
		if (large) {
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

	private class UpdateSongs extends AsyncTask<Void, Void, List<Song>> {

		private Playlist playlist;

		@Override
		protected void onPreExecute() {
			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
			isLoading = true;
			if (listener != null)
				playlist = listener.getSelectedPlaylist();
			super.onPreExecute();
		}

		private List<Song> updateSongs() {
			try {
				if (listener != null)
					return listener.updateSongs(playlist);
			} catch (IOException e) {
				if (listener != null)
					listener.internetFail();
			} catch (VKontakteException e) {
				if (listener != null) {
					switch (e.getCode()) {
					case VKontakteException.UNKNOWN_ERROR_OCCURED:
						listener.unknownError();
						break;

					case VKontakteException.USER_AUTHORIZATION_FAILED:
						listener.authFail();
						break;

					case VKontakteException.TOO_MANY_REQUESTS_PER_SECOND:
						return updateSongs();

					case VKontakteException.ACCESS_DENIED:
						listener.accessDenied();
						break;

					case VKontakteException.PERMISSION_TO_PERFORM_THIS_ACTION_IS_DENIED_BY_USER:
						listener.accessDenied();
						break;
					}
				}
			}
			return null;
		}

		@Override
		protected List<Song> doInBackground(Void... params) {
			Thread.currentThread().setName("Getting songs");
			List<Song> result = updateSongs();
			List<Song> songs = playlist.songs;
			if (songs != null) {
				int position = -1;
				int index = 0;
				do {
					position++;
					index = result.indexOf(songs.get(position));
				} while (index < 0 && position < result.size() && position < songs.size());

				position = index >= 0 ? index : position;
				return result.subList(0, position);
			}
			return result;
		}

		@Override
		protected void onPostExecute(List<Song> result) {
			super.onPostExecute(result);
			if (result != null) {
				List<Song> songs = playlist.songs;
				if (songs != null)
					songs.addAll(0, result);
				else
					songs = result;

				if (listener != null) {
					if (adapter == null) {
						adapter = new PlaylistAdapter(getSherlockActivity(), songs, listener.getTypeface());
						setListAdapter(adapter);
					}

					if (!listener.getSelectedPlaylist().equals(listener.getPlaylist())) {
						adapter.currentTrack = -1;
					} else if (listener.isPlaying()) {
						int size = result.size();
						adapter.currentTrack += size;
						getListView().smoothScrollToPosition(adapter.currentTrack);
						listener.onPlaylistUpdated(result.size());
					}
					adapter.notifyDataSetChanged();
				}
			}
			isLoading = false;
			if (getSherlockActivity() != null)
				getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
		}
	}

	public void refresh() {
		Playlist selectedPlaylist = listener.getSelectedPlaylist();
		if (selectedPlaylist.type != Type.SEARCH && !isLoading)
			new UpdateSongs().execute();
	}
}
