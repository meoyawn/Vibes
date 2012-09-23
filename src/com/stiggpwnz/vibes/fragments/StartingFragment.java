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

public class StartingFragment extends AlbumsFragment {

	public static interface Listener extends AlbumsFragment.Listener {

		public void showUnits(boolean friends);

	}

	private static final int FRIENDS = 0;
	private static final int GROUPS = 1;
	private static final int MY_AUDIOS = 2;
	private static final int WALL = 3;
	private static final int NEWSFEED = 4;

	public StartingFragment() {

	}

	public static StartingFragment newInstance(Unit unit, Playlist playlist) {
		StartingFragment fragment = new StartingFragment();
		Bundle args = AlbumsFragment.initWithUnit(unit);
		int selected;
		switch (playlist.type) {
		case AUDIOS:
			selected = MY_AUDIOS;
			break;

		case WALL:
			selected = WALL;
			break;

		case NEWSFEED:
			selected = NEWSFEED;
			break;

		default:
			selected = -1;
			break;
		}
		args.putInt(SELECTED_POSITION, selected);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		listener = (Listener) activity;
		super.onAttach(activity);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		String[] options = getResources().getStringArray(R.array.playlist_options);
		AlbumsAdapter adapter = new AlbumsAdapter(getSherlockActivity(), listener.getTypeface(), options);
		super.onViewCreated(view, savedInstanceState, adapter);
	}

	@Override
	public void onDetach() {
		Log.d(VibesApplication.VIBES, "onDetach choose");
		super.onDetach();
		listener = null;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String name = (String) l.getItemAtPosition(position);
		if (position > 1) {
			if (listener.isPlaylistLoading())
				return;
			setSelectedPosition(position);
		}

		switch (position) {
		case FRIENDS:
			((Listener) listener).showUnits(true);
			break;

		case GROUPS:
			((Listener) listener).showUnits(false);
			break;

		case MY_AUDIOS:
			listener.loadPlaylist(new Playlist(Type.AUDIOS, name, unit));
			break;

		case WALL:
			listener.loadPlaylist(new Playlist(Type.WALL, name, unit));
			break;

		case NEWSFEED:
			listener.loadPlaylist(new Playlist(Type.NEWSFEED, name));
			break;

		default:
			listener.loadPlaylist(new Playlist(Type.AUDIOS, name, unit, unit.albums.get(position - 5)));
			break;
		}
	}
}
