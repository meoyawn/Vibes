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

import com.stiggpwnz.vibes.ImageLoader;
import com.stiggpwnz.vibes.NewActivity;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.Unit;

public class UnitsAdapter extends BaseAdapter {

	private List<Unit> units;
	private LayoutInflater inflater;
	private ImageLoader imageLoader;
	private Typeface typeface;

	public UnitsAdapter(NewActivity activity, List<Unit> units, ImageLoader imageLoader) {
		this.units = units;
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		typeface = activity.getTypeface();
		this.imageLoader = imageLoader;
	}

	public void setUnits(List<Unit> units) {
		this.units = units;
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

	private class ViewHolder {
		ImageView image;
		TextView name;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder viewholder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.unit_list_item, null);
			viewholder = new ViewHolder();
			viewholder.image = (ImageView) convertView.findViewById(R.id.unitImage);
			viewholder.name = (TextView) convertView.findViewById(R.id.unitName);
			viewholder.name.setTypeface(typeface);
			convertView.setTag(viewholder);
		} else
			viewholder = (ViewHolder) convertView.getTag();

		final Unit unit = units.get(position);
		viewholder.name.setText(unit.name);

		if (imageLoader.getStubId() != R.drawable.photo_placeholder)
			imageLoader.setStubId(R.drawable.photo_placeholder);
		imageLoader.displayImage(unit.photo, viewholder.image);

		return convertView;
	}
}
