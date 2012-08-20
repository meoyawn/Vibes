package com.stiggpwnz.vibes.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.stiggpwnz.vibes.R;

public class CustomFontTextAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private Typeface typeface;
	private String[] data;

	public CustomFontTextAdapter(Context context, Typeface typeface, String[] array) {
		this.typeface = typeface;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		data = array;
	}

	@Override
	public int getCount() {
		if (data != null)
			return data.length;
		return 0;
	}

	@Override
	public String getItem(int position) {
		if (data != null)
			return data[position];
		return null;
	}

	@Override
	public long getItemId(int position) {
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
		textView.setText(data[position]);
		return convertView;
	}

}