package com.stiggpwnz.vibes.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.stiggpwnz.vibes.NewActivity;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.Settings;
import com.stiggpwnz.vibes.adapters.TextAdapter;

public class PlaylistsDialog extends Dialog implements OnItemClickListener {

	public PlaylistsDialog(Context context, Typeface typeface) {
		super(context);
		setContentView(R.layout.list);
		setTitle(R.string.playlist);
		setCanceledOnTouchOutside(true);
		ListView listView = (ListView) findViewById(R.id.listView);
		String[] options = context.getResources().getStringArray(R.array.playlist_options);
		TextAdapter adapter = new TextAdapter(context, typeface, options);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> list, View view, int position, long id) {
		NewActivity activity = (NewActivity) getOwnerActivity();
		Settings settings = activity.getApp().getSettings();
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
