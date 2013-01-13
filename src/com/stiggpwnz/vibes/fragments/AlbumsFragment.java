package com.stiggpwnz.vibes.fragments;

import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.actionbarsherlock.app.SherlockListFragment;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.VibesApplication;
import com.stiggpwnz.vibes.adapters.AlbumsAdapter;
import com.stiggpwnz.vibes.restapi.Album;
import com.stiggpwnz.vibes.restapi.Unit;

public class AlbumsFragment extends SherlockListFragment {

	protected static final String UNIT = "unit";

	protected static final String SCROLL_POSITION = "scroll position";
	protected static final String SELECTED_POSITION = "selected position";

	protected Unit unit;

	protected int scrollPosition;
	protected int selectedPosition = -1;

	private View loadingFooter;

	protected VibesApplication app;

	public AlbumsFragment() {

	}

	protected static Bundle initWithUnit(Unit unit) {
		Bundle args = new Bundle();
		args.putSerializable(UNIT, unit);
		return args;
	}

	protected void onViewCreated(View view, Bundle savedInstanceState, AlbumsAdapter adapter) {
		app = (VibesApplication) getSherlockActivity().getApplication();

		Unit unit = savedInstanceState != null ? (Unit) savedInstanceState.getSerializable(UNIT) : (Unit) getArguments().getSerializable(UNIT);
		selectedPosition = savedInstanceState != null ? savedInstanceState.getInt(SELECTED_POSITION) : getArguments().getInt(SELECTED_POSITION);

		this.unit = unit;

		if (unit.albums == null) {
			new LoadAlbums(adapter).execute();
		} else {
			adapter.setAlbums(unit.albums);
			setListAdapter(adapter);
		}
		getListView().setSelection(savedInstanceState != null ? savedInstanceState.getInt(SCROLL_POSITION) : scrollPosition);
		setSelectedPosition(selectedPosition);
		super.onViewCreated(view, savedInstanceState);
		if (unit != null && !unit.equals(app.getSelectedPlaylist().unit))
			setSelectedPosition(-1);
	}

	public View getFooter() {
		if (loadingFooter == null)
			loadingFooter = LayoutInflater.from(getSherlockActivity()).inflate(R.layout.footer, null);
		return loadingFooter;
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
		if (getListAdapter() != null) {
			AlbumsAdapter albumsAdapter = (AlbumsAdapter) getListAdapter();
			albumsAdapter.setSelected(position);
			selectedPosition = position;
		}
	}

	private class LoadAlbums extends AsyncTask<Void, Void, List<Album>> {

		private AlbumsAdapter adapter;

		public LoadAlbums(AlbumsAdapter adapter) {
			this.adapter = adapter;
		}

		@Override
		protected void onPreExecute() {
			getListView().addFooterView(getFooter());
			setListAdapter(adapter);
			super.onPreExecute();
		}

		@Override
		protected List<Album> doInBackground(Void... params) {
			Thread.currentThread().setName("Getting albums");
			return app.loadAlbums(unit.id);
		}

		@Override
		protected void onPostExecute(List<Album> result) {
			super.onPostExecute(result);
			adapter.setAlbums(result);
			adapter.notifyDataSetChanged();
			if (isVisible())
				getListView().removeFooterView(getFooter());
			unit.albums = result;
		}

	}

}
