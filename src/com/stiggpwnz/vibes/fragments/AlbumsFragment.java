package com.stiggpwnz.vibes.fragments;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.app.SherlockListFragment;
import com.stiggpwnz.vibes.adapters.AlbumsAdapter;
import com.stiggpwnz.vibes.restapi.Album;
import com.stiggpwnz.vibes.restapi.Playlist;
import com.stiggpwnz.vibes.restapi.Unit;
import com.stiggpwnz.vibes.restapi.VKontakteException;

public class AlbumsFragment extends SherlockListFragment {

	public static interface Listener extends FragmentListener {

		public List<Album> loadAlbums(Unit unit) throws ClientProtocolException, IOException, VKontakteException;

		public void loadPlaylist(Playlist playlist);

		public View getFooterView();

		public boolean isPlaylistLoading();

	}

	protected static final String UNIT = "unit";

	protected static final String SCROLL_POSITION = "scroll position";
	protected static final String SELECTED_POSITION = "selected position";

	protected Listener listener;
	protected Unit unit;

	protected int scrollPosition;
	protected int selectedPosition = -1;

	public AlbumsFragment() {

	}

	protected static Bundle initWithUnit(Unit unit) {
		Bundle args = new Bundle();
		args.putSerializable(UNIT, unit);
		return args;
	}

	protected void onViewCreated(View view, Bundle savedInstanceState, AlbumsAdapter adapter) {
		Unit unit = savedInstanceState != null ? (Unit) savedInstanceState.getSerializable(UNIT) : (Unit) getArguments().getSerializable(UNIT);
		selectedPosition = savedInstanceState != null ? savedInstanceState.getInt(SELECTED_POSITION) : getArguments().getInt(SELECTED_POSITION);
		if (unit.albums == null) {
			new LoadAlbums(adapter).execute(unit);
		} else {
			adapter.setAlbums(unit.albums);
			setListAdapter(adapter);
			this.unit = unit;
		}
		getListView().setSelection(savedInstanceState != null ? savedInstanceState.getInt(SCROLL_POSITION) : scrollPosition);
		int position = savedInstanceState != null ? savedInstanceState.getInt(SELECTED_POSITION) : selectedPosition;
		setSelectedPosition(position);
		super.onViewCreated(view, savedInstanceState);
		if (unit != null && !unit.equals(listener.getSelectedPlaylist().unit))
			setSelectedPosition(-1);
	}

	// save current scroll position
	@Override
	public void onDestroyView() {
		scrollPosition = getListView().getFirstVisiblePosition();
		super.onDestroyView();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(UNIT, getArguments().getSerializable(UNIT));
		outState.putInt(SCROLL_POSITION, scrollPosition);
		outState.putInt(SELECTED_POSITION, selectedPosition);
	}

	public void setSelectedPosition(int position) {
		AlbumsAdapter albumsAdapter = (AlbumsAdapter) getListAdapter();
		albumsAdapter.setSelected(position);
		albumsAdapter.notifyDataSetChanged();
		selectedPosition = position;
	}

	private class LoadAlbums extends AsyncTask<Unit, Void, List<Album>> {

		private Unit unit;
		private AlbumsAdapter adapter;
		private View footer;

		public LoadAlbums(AlbumsAdapter adapter) {
			this.adapter = adapter;
			footer = listener.getFooterView();
		}

		@Override
		protected void onPreExecute() {
			getListView().addFooterView(footer);
			setListAdapter(adapter);
			super.onPreExecute();
		}

		private List<Album> getAlbums(Unit unit) {
			if (listener != null) {
				try {
					return listener.loadAlbums(unit);
				} catch (IOException e) {
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
							return getAlbums(unit);

						case VKontakteException.ACCESS_DENIED:
							listener.accessDenied();
							break;
						}
					}
				}
			}
			return null;
		}

		@Override
		protected List<Album> doInBackground(Unit... params) {
			Thread.currentThread().setName("Getting albums");
			unit = params[0];
			return getAlbums(unit);
		}

		@Override
		protected void onPostExecute(List<Album> result) {
			super.onPostExecute(result);
			adapter.setAlbums(result);
			adapter.notifyDataSetChanged();
			if (isVisible())
				getListView().removeFooterView(footer);
			unit.albums = result;
			AlbumsFragment.this.unit = unit;
		}

	}

}
