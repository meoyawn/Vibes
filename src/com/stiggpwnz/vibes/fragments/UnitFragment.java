package com.stiggpwnz.vibes.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.adapters.AlbumsAdapter;
import com.stiggpwnz.vibes.events.BusProvider;
import com.stiggpwnz.vibes.restapi.Album;
import com.stiggpwnz.vibes.restapi.Playlist;
import com.stiggpwnz.vibes.restapi.Playlist.Type;
import com.stiggpwnz.vibes.restapi.Unit;

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
	public void onViewCreated(View view, Bundle savedInstanceState) {
		String[] options = getResources().getStringArray(R.array.unit_options);
		AlbumsAdapter adapter = new AlbumsAdapter(getSherlockActivity(), app.getTypeface(), options);
		super.onViewCreated(view, savedInstanceState, adapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String name = (String) l.getItemAtPosition(position);
		setSelectedPosition(position);
		switch (position) {
		case AUDIOS:
			BusProvider.getInstance().post(Playlist.get(new Playlist(Type.AUDIOS, name, unit)));
			break;

		case WALL:
			BusProvider.getInstance().post(Playlist.get(new Playlist(Type.WALL, name, unit)));
			break;

		default:
			Album album = unit.albums.get(position - 2);
			BusProvider.getInstance().post(Playlist.get(new Playlist(Type.AUDIOS, name, unit, album)));
			break;
		}
	}
}
