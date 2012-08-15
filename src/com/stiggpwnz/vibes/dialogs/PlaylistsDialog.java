package com.stiggpwnz.vibes.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.stiggpwnz.vibes.NewActivity;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.Settings;
import com.stiggpwnz.vibes.adapters.TextAdapter;

public class PlaylistsDialog extends Dialog implements OnItemClickListener {

	private NewActivity activity;
	private Settings settings;

	public PlaylistsDialog(Context context) {
		super(context);
		activity = (NewActivity) getOwnerActivity();
		settings = activity.getApp().getSettings();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		setTitle(R.string.playlist);
		setCanceledOnTouchOutside(true);
		ListView listView = (ListView) findViewById(R.id.listView);
		String[] options = getContext().getResources().getStringArray(R.array.playlist_options);
		TextAdapter adapter = new TextAdapter(activity, options);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> list, View view, int position, long id) {
		switch (position) {

		case NewActivity.SEARCH:
			activity.showDialog(NewActivity.DIALOG_SEARCH);
			break;

		case NewActivity.FRIENDS:
			activity.setFriendsList(true);
			if (activity.getReadyFriends() != null)
				activity.showDialog(NewActivity.DIALOG_UNITS);
			else
				activity.runGetUnits();
			break;

		case NewActivity.GROUPS:
			activity.setFriendsList(false);
			if (activity.getReadyGroups() != null)
				activity.showDialog(NewActivity.DIALOG_UNITS);
			else
				activity.runGetUnits();
			break;

		case NewActivity.MY_AUDIOS:
			settings.setPlaylist(position);
			settings.setOwnerId(0);
			settings.setAlbumId(0);
			activity.runGetSongs(null);
			dismiss();
			break;

		case NewActivity.WALL:
			settings.setPlaylist(position);
			settings.setOwnerId(0);
			settings.setAlbumId(0);
			activity.runGetSongs(null);
			dismiss();
			break;

		case NewActivity.NEWSFEED:
			settings.setPlaylist(position);
			activity.runGetSongs(null);
			dismiss();
			break;

		case NewActivity.ALBUMS:
			settings.setOwnerId(0);
			activity.runGetALbums();
			break;
		}
	}

}
