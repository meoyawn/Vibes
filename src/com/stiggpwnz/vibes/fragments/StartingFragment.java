package com.stiggpwnz.vibes.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ListView;

import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.adapters.AlbumsAdapter;
import com.stiggpwnz.vibes.events.BusProvider;
import com.stiggpwnz.vibes.restapi.Album;
import com.stiggpwnz.vibes.restapi.Playlist;
import com.stiggpwnz.vibes.restapi.Playlist.Type;
import com.stiggpwnz.vibes.restapi.Unit;

public class StartingFragment extends AlbumsFragment {

	private static final int FRIENDS = 0;
	private static final int GROUPS = 1;
	private static final int MY_AUDIOS = 2;
	private static final int WALL = 3;
	private static final int NEWSFEED = 4;

	public StartingFragment() {

	}

	@SuppressWarnings("incomplete-switch")
	public static StartingFragment newInstance(Context context, Unit unit, Playlist playlist) {
		StartingFragment fragment = new StartingFragment();
		Bundle args = AlbumsFragment.initWithUnit(unit);
		int selected = -1;
		switch (playlist.type) {
		case AUDIOS:
			if (playlist.album == null)
				selected = MY_AUDIOS;
			else
				selected = playlist.unit.albums.indexOf(playlist.album) + 5;
			break;

		case WALL:
			selected = WALL;
			break;

		case NEWSFEED:
			selected = NEWSFEED;
			break;
		}
		args.putInt(SELECTED_POSITION, selected);
		fragment.setArguments(args);

		if (playlist.name == null)
			playlist.name = context.getResources().getStringArray(R.array.playlist_options)[selected];

		return fragment;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		String[] options = getResources().getStringArray(R.array.playlist_options);
		AlbumsAdapter adapter = new AlbumsAdapter(getSherlockActivity(), app.getTypeface(), options);
		super.onViewCreated(view, savedInstanceState, adapter);
	}

	public void showUnits(boolean friends) {
		FragmentTransaction transaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
		UnitsListFragment unitsFragment = UnitsListFragment.newInstance(friends);
		transaction.replace(R.id.framePlaylists, unitsFragment, "units list fragment");
		transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);
		String name = (String) listView.getItemAtPosition(position);
		if (position > 1) {
			setSelectedPosition(position);
			getArguments().putInt(SELECTED_POSITION, position);
		}

		switch (position) {
		case FRIENDS:
			showUnits(true);
			break;

		case GROUPS:
			showUnits(false);
			break;

		case MY_AUDIOS:
			BusProvider.getInstance().post(Playlist.get(new Playlist(Type.AUDIOS, name, unit)));
			break;

		case WALL:
			BusProvider.getInstance().post(Playlist.get(new Playlist(Type.WALL, name, unit)));
			break;

		case NEWSFEED:
			BusProvider.getInstance().post(Playlist.get(new Playlist(Type.NEWSFEED, name, unit)));
			break;

		default:
			Album album = unit.albums.get(position - 5);
			BusProvider.getInstance().post(Playlist.get(new Playlist(Type.AUDIOS, name, unit, album)));
			break;
		}
	}
}
