package com.stiggpwnz.vibes.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.stiggpwnz.vibes.PlayerActivity;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.Settings;
import com.stiggpwnz.vibes.adapters.CustomFontTextAdapter;

public class PlaylistsDialog extends Dialog implements OnItemClickListener {

	public PlaylistsDialog(Context context, Typeface typeface) {
		super(context);
		setContentView(R.layout.list);
		setTitle(R.string.playlist);
		setCanceledOnTouchOutside(true);
		ListView listView = (ListView) findViewById(R.id.listView);
		String[] options = context.getResources().getStringArray(R.array.playlist_options);
		CustomFontTextAdapter adapter = new CustomFontTextAdapter(context, typeface, options);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> list, View view, int position, long id) {
		PlayerActivity activity = (PlayerActivity) getOwnerActivity();
		Settings settings = activity.getApp().getSettings();
		switch (position) {

		case PlayerActivity.SEARCH:
			activity.showDialog(PlayerActivity.DIALOG_SEARCH);
			break;

		case PlayerActivity.FRIENDS:
			activity.setFriendsList(true);
			if (activity.getFriends() != null)
				activity.showDialog(PlayerActivity.DIALOG_UNITS);
			else
				activity.runGetUnits();
			break;

		case PlayerActivity.GROUPS:
			activity.setFriendsList(false);
			if (activity.getGroups() != null)
				activity.showDialog(PlayerActivity.DIALOG_UNITS);
			else
				activity.runGetUnits();
			break;

		case PlayerActivity.MY_AUDIOS:
			settings.setPlaylist(position);
			settings.setOwnerId(0);
			settings.setAlbumId(0);
			activity.runGetSongs(null);
			dismiss();
			break;

		case PlayerActivity.WALL:
			settings.setPlaylist(position);
			settings.setOwnerId(0);
			settings.setAlbumId(0);
			activity.runGetSongs(null);
			dismiss();
			break;

		case PlayerActivity.NEWSFEED:
			settings.setPlaylist(position);
			activity.runGetSongs(null);
			dismiss();
			break;

		case PlayerActivity.ALBUMS:
			settings.setOwnerId(0);
			activity.runGetALbums();
			break;
		}
	}

}
