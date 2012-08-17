package com.stiggpwnz.vibes.adapters;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.vkapi.Album;

public class AlbumsAdapter extends BaseAdapter {

	private List<Album> albums;
	private LayoutInflater inflater;
	private Typeface typeface;

	public AlbumsAdapter(Context context, Typeface typeface, List<Album> albumList) {
		this.albums = albumList;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.typeface = typeface;
	}

	public void setAlbums(List<Album> albums) {
		this.albums = albums;
	}

	@Override
	public int getCount() {
		if (albums != null)
			return albums.size();
		return 0;
	}

	@Override
	public Album getItem(int position) {
		if (albums != null)
			return albums.get(position);
		return null;
	}

	@Override
	public long getItemId(int position) {
		if (albums != null)
			return albums.get(position).id;
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView textView;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.simple_list_item_1, null);
			textView = (TextView) convertView.findViewById(R.id.text1);
			textView.setTypeface(typeface);
			convertView.setTag(textView);
		} else
			textView = (TextView) convertView.getTag();
		textView.setText(albums.get(position).name);
		return convertView;
	}

}
