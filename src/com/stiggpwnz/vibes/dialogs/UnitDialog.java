package com.stiggpwnz.vibes.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.stiggpwnz.vibes.NewActivity;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.Settings;
import com.stiggpwnz.vibes.adapters.TextAdapter;

public class UnitDialog extends Dialog implements OnItemClickListener {

	private NewActivity activity;
	private Settings settings;

	public UnitDialog(Context context) {
		super(context);
		activity = (NewActivity) getOwnerActivity();
		settings = activity.getApp().getSettings();
		setContentView(R.layout.list);
		setCanceledOnTouchOutside(true);
		ListView unitList = (ListView) findViewById(R.id.listView);
		String[] unitOptions = getContext().getResources().getStringArray(R.array.unit_options);
		TextAdapter unitAdapter = new TextAdapter(activity, unitOptions);
		unitList.setAdapter(unitAdapter);
		unitList.setEmptyView(findViewById(android.R.id.empty));
		unitList.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> list, View view, int position, long id) {

		switch (position) {
		case 0:
			settings.setPlaylist(NewActivity.MY_AUDIOS);
			settings.setAlbumId(0);
			activity.runGetSongs(null);
			activity.dismissDialog(NewActivity.DIALOG_UNIT);
			activity.dismissDialog(NewActivity.DIALOG_UNITS);
			activity.dismissDialog(NewActivity.DIALOG_PLAYLISTS);
			break;

		case 1:
			settings.setPlaylist(NewActivity.WALL);
			activity.runGetSongs(null);
			activity.dismissDialog(NewActivity.DIALOG_UNIT);
			activity.dismissDialog(NewActivity.DIALOG_UNITS);
			activity.dismissDialog(NewActivity.DIALOG_PLAYLISTS);
			break;

		case 2:
			activity.runGetALbums();
			break;
		}

	}
}
