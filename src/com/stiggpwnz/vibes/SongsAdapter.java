package com.stiggpwnz.vibes;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SongsAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private VibesApplication app;
	private ColorStateList black;
	private Animation shakeRight;
	private int blue;
	public boolean fromPlaylist;
	public int currentSong = -1;

	public SongsAdapter(Context context) {
		app = (VibesApplication) ((Activity) context).getApplication();
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		blue = app.getResources().getColor(R.color.normal);
		shakeRight = AnimationUtils.loadAnimation(app, R.anim.shake_right);
	}

	@Override
	public int getCount() {
		if (app.songs != null)
			return app.songs.size();
		return 0;
	}

	@Override
	public Song getItem(int position) {
		if (app.songs != null)
			return app.songs.get(position);
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
			viewholder.artist.setTypeface(app.getTypeface());
			viewholder.title = (TextView) convertView.findViewById(R.id.textTitle);
			viewholder.title.setTypeface(app.getTypeface());
			if (black == null)
				black = viewholder.title.getTextColors();
			viewholder.stick = convertView.findViewById(R.id.leftView);
			convertView.setTag(viewholder);
		} else
			viewholder = (ViewHolder) convertView.getTag();

		viewholder.artist.setText(app.songs.get(position).performer);
		viewholder.title.setText(app.songs.get(position).title);

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