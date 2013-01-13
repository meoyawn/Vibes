package com.stiggpwnz.vibes.adapters;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.restapi.Unit;

public class UnitsAdapter extends BaseAdapter {

	private final List<Unit> units;
	private final LayoutInflater inflater;
	private final Typeface typeface;

	public UnitsAdapter(Context context, List<Unit> units, Typeface typeface) {
		inflater = LayoutInflater.from(context);
		this.units = units;
		this.typeface = typeface;
	}

	@Override
	public int getCount() {
		if (units != null)
			return units.size();
		return 0;
	}

	@Override
	public Unit getItem(int position) {
		if (units != null)
			return units.get(position);
		return null;
	}

	@Override
	public long getItemId(int position) {
		if (units != null)
			return units.get(position).id;
		return position;
	}

	private View newView() {
		View convertView = inflater.inflate(R.layout.unit, null);
		ViewHolder viewholder = new ViewHolder();

		viewholder.image = (ImageView) convertView.findViewById(R.id.unitImage);
		viewholder.name = (TextView) convertView.findViewById(R.id.unitName);
		viewholder.name.setTypeface(typeface);

		convertView.setTag(viewholder);
		return convertView;
	}

	private final DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder().showStubImage(R.drawable.photo_placeholder)
			.showImageForEmptyUri(R.drawable.photo_placeholder).cacheInMemory().cacheOnDisc().displayer(new FadeInBitmapDisplayer(250)).build();

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null)
			convertView = newView();

		ViewHolder viewholder = (ViewHolder) convertView.getTag();

		Unit unit = getItem(position);

		viewholder.name.setText(unit.name);
		ImageLoader.getInstance().displayImage(unit.photo, viewholder.image, displayImageOptions);
		return convertView;
	}

	private static class ViewHolder {
		public ImageView image;
		public TextView name;
	}
}
