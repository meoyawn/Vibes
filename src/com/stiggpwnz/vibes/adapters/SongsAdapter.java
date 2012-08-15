package com.stiggpwnz.vibes.adapters;

import java.util.List;

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

import com.stiggpwnz.vibes.NewActivity;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.Song;

public class SongsAdapter extends BaseAdapter {

	private List<Song> songs;
	private LayoutInflater inflater;
	private ColorStateList black;
	private Animation shakeRight;
	private int blue;
	public boolean fromPlaylist;
	public int currentSong = -1;
	private Typeface typeface;

	public SongsAdapter(NewActivity activity) {
		typeface = activity.getTypeface();
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		blue = activity.getResources().getColor(R.color.normal);
		shakeRight = AnimationUtils.loadAnimation(activity, R.anim.shake_right);
	}

	@Override
	public int getCount() {
		if (songs != null)
			return songs.size();
		return 0;
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

	private class ViewHolder {
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

		if (currentSong >= 0 && position == currentSong) {
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