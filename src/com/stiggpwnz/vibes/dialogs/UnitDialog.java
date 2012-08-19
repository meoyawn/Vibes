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

public class UnitDialog extends Dialog implements OnItemClickListener {

	public UnitDialog(Context context, Typeface typeface) {
		super(context);

		setContentView(R.layout.list);
		setCanceledOnTouchOutside(true);
		ListView unitList = (ListView) findViewById(R.id.listView);
		String[] unitOptions = getContext().getResources().getStringArray(R.array.unit_options);
		CustomFontTextAdapter unitAdapter = new CustomFontTextAdapter(context, typeface, unitOptions);
		unitList.setAdapter(unitAdapter);
		unitList.setEmptyView(findViewById(android.R.id.empty));
		unitList.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> list, View view, int position, long id) {

		PlayerActivity activity = (PlayerActivity) getOwnerActivity();
		Settings settings = activity.getApp().getSettings();

		switch (position) {
		case 0:
			settings.setPlaylist(PlayerActivity.PLAYLIST_MY_AUDIOS);
			settings.setAlbumId(0);
			activity.runGetSongs(null);
			dismiss();
			activity.dismissDialog(PlayerActivity.DIALOG_UNITS);
			activity.dismissDialog(PlayerActivity.DIALOG_PLAYLISTS);
			break;

		case 1:
			settings.setPlaylist(PlayerActivity.PLAYLIST_WALL);
			activity.runGetSongs(null);
			dismiss();
			activity.dismissDialog(PlayerActivity.DIALOG_UNITS);
			activity.dismissDialog(PlayerActivity.DIALOG_PLAYLISTS);
			break;

		case 2:
			activity.getAlbums();
			break;
		}

	}
}
