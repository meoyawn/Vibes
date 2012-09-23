package com.stiggpwnz.vibes.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.restapi.Song;

public class PlaylistAdapter extends BaseAdapter {

	private ArrayList<Song> songs;
	private LayoutInflater inflater;
	private ColorStateList black;
	private Animation shakeRight;
	private int blue;
	public boolean fromPlaylist;
	public int currentTrack = -1;
	private Typeface typeface;

	public PlaylistAdapter(Context context, ArrayList<Song> songs, Typeface typeface) {
		this.typeface = typeface;
		this.songs = songs;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.blue = context.getResources().getColor(R.color.normal);
		this.shakeRight = AnimationUtils.loadAnimation(context, R.anim.shake_right);
	}

	@Override
	public int getCount() {
		if (songs != null)
			return songs.size();
		return 0;
	}

	public ArrayList<Song> getSongs() {
		return songs;
	}

	public void setSongs(ArrayList<Song> songs) {
		this.songs = songs;
		notifyDataSetChanged();
	}

	@Override
	public Song getItem(int position) {
		if (songs != null)
			return songs.get(position);
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private static class ViewHolder {
		TextView artist;
		TextView title;
		View stick;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewholder;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.song, null);
			viewholder = new ViewHolder();
			viewholder.artist = (TextView) convertView.findViewById(R.id.textArtist);
			viewholder.artist.setTypeface(typeface);
			viewholder.title = (TextView) convertView.findViewById(R.id.textTitle);
			viewholder.title.setTypeface(typeface);
			if (black == null)
				black = viewholder.title.getTextColors();
			viewholder.stick = convertView.findViewById(R.id.leftView);
			convertView.setTag(viewholder);
		} else
			viewholder = (ViewHolder) convertView.getTag();

		viewholder.artist.setText(songs.get(position).performer);
		viewholder.title.setText(songs.get(position).title);

		if (currentTrack >= 0 && position == currentTrack) {
			viewholder.stick.setVisibility(View.VISIBLE);
			viewholder.title.setTextColor(blue);
			if (fromPlaylist) {
				fromPlaylist = false;
				convertView.startAnimation(shakeRight);
			}
		} else {
			viewholder.stick.setVisibility(View.INVISIBLE);
			viewholder.title.setTextColor(black);
		}

		return convertView;
	}

}