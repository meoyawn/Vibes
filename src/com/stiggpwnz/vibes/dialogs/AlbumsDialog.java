package com.stiggpwnz.vibes.dialogs;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.stiggpwnz.vibes.PlayerActivity;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.Settings;
import com.stiggpwnz.vibes.adapters.AlbumsAdapter;
import com.stiggpwnz.vibes.restapi.Album;

public class AlbumsDialog extends Dialog implements OnItemClickListener {

	private AlbumsAdapter albumsAdapter;

	public AlbumsDialog(Context context, List<Album> albums, Typeface typeface) {
		super(context);
		setContentView(R.layout.list);
		setTitle(getContext().getResources().getStringArray(R.array.unit_options)[2]);
		setCanceledOnTouchOutside(true);
		ListView albumsList = (ListView) findViewById(R.id.listView);
		albumsAdapter = new AlbumsAdapter(context, typeface, albums);
		albumsList.setAdapter(albumsAdapter);

		TextView emptyAlbum = (TextView) findViewById(android.R.id.empty);
		emptyAlbum.setTypeface(typeface);
		albumsList.setEmptyView(emptyAlbum);

		albumsList.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> list, View view, int position, long id) {
		PlayerActivity activity = (PlayerActivity) getOwnerActivity();
		Settings settings = activity.getApp().getSettings();
		settings.setPlaylist(PlayerActivity.MY_AUDIOS);
		settings.setAlbumId(albumsAdapter.getItem(position).id);
		activity.runGetSongs(null);
		activity.dismissDialog(PlayerActivity.DIALOG_ALBUMS);
		if (settings.getOwnerId() != 0) {
			activity.dismissDialog(PlayerActivity.DIALOG_UNIT);
			activity.dismissDialog(PlayerActivity.DIALOG_UNITS);
		}
		activity.dismissDialog(PlayerActivity.DIALOG_PLAYLISTS);
	}

	public void setAlbums(List<Album> albumList) {
		albumsAdapter.setAlbums(albumList);
		albumsAdapter.notifyDataSetChanged();
	}

}
