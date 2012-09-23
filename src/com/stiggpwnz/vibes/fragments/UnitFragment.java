package com.stiggpwnz.vibes.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.VibesApplication;
import com.stiggpwnz.vibes.adapters.AlbumsAdapter;
import com.stiggpwnz.vibes.restapi.Playlist;
import com.stiggpwnz.vibes.restapi.Unit;
import com.stiggpwnz.vibes.restapi.Playlist.Type;

public class UnitFragment extends AlbumsFragment {

	private static final int AUDIOS = 0;
	private static final int WALL = 1;

	public UnitFragment() {

	}

	@SuppressWarnings("incomplete-switch")
	public static UnitFragment newInstance(Unit unit, Playlist playlist) {
		UnitFragment fragment = new UnitFragment();
		Bundle args = AlbumsFragment.initWithUnit(unit);
		int selectedPosition = -1;
		if (playlist.unit != null && playlist.unit.equals(unit)) {
			switch (playlist.type) {
			case AUDIOS:
				if (playlist.album != null)
					selectedPosition = unit.albums.indexOf(playlist.album) + 2;
				else
					selectedPosition = 0;
				break;
			case WALL:
				selectedPosition = 1;
				break;
			}
		}
		args.putInt(SELECTED_POSITION, selectedPosition);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		Log.d(VibesApplication.VIBES, "onAttach UNIT");
		listener = (Listener) activity;
		super.onAttach(activity);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		String[] options = getResources().getStringArray(R.array.unit_options);
		AlbumsAdapter adapter = new AlbumsAdapter(getSherlockActivity(), listener.getTypeface(), options);
		super.onViewCreated(view, savedInstanceState, adapter);
	}

	@Override
	public void onDetach() {
		Log.d(VibesApplication.VIBES, "onDetach UNIT");
		listener = null;
		super.onDetach();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String name = (String) l.getItemAtPosition(position);
		if (listener.isPlaylistLoading())
			return;
		setSelectedPosition(position);
		switch (position) {
		case AUDIOS:
			listener.loadPlaylist(new Playlist(Type.AUDIOS, name, unit));
			break;

		case WALL:
			listener.loadPlaylist(new Playlist(Type.WALL, name, unit));
			break;

		default:
			listener.loadPlaylist(new Playlist(Type.AUDIOS, name, unit, unit.albums.get(position - 2)));
			break;
		}
	}
}
