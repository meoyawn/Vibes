package com.stiggpwnz.vibes.adapters;

import java.util.List;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.restapi.Album;

public class AlbumsAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private Typeface typeface;
	private String[] data;
	private List<Album> albums;
	private int selected = -1;
	private int blue;
	private int white;
	private ColorStateList black;
	private Drawable background;

	public AlbumsAdapter(Context context, Typeface typeface, String[] array) {
		this.typeface = typeface;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		data = array;
		blue = context.getResources().getColor(R.color.normal);
		white = context.getResources().getColor(R.color.white);
	}

	public void setAlbums(List<Album> result) {
		this.albums = result;
	}

	public List<Album> getAlbums() {
		return albums;
	}

	@Override
	public int getCount() {
		if (albums == null)
			return data.length;
		else
			return data.length + albums.size();
	}

	@Override
	public String getItem(int position) {
		if (position < data.length)
			return data[position];
		else
			return albums.get(position - data.length).name;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView textView;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.simple_text_item, null);
			textView = (TextView) convertView.findViewById(R.id.text1);
			textView.setTypeface(typeface);
			convertView.setTag(textView);
			if (black == null) {
				black = textView.getTextColors();
				background = convertView.getBackground();
			}
		} else
			textView = (TextView) convertView.getTag();

		textView.setText(getItem(position));
		if (position == selected) {
			convertView.setBackgroundColor(blue);
			textView.setTextColor(white);
		} else {
			convertView.setBackgroundDrawable(background);
			textView.setTextColor(black);
		}
		return convertView;
	}

	public int getSelected() {
		return selected;
	}

	public void setSelected(int selected) {
		this.selected = selected;
		notifyDataSetChanged();
	}

}
