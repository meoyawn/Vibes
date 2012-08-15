package com.stiggpwnz.vibes.dialogs;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.stiggpwnz.vibes.ImageLoader;
import com.stiggpwnz.vibes.NewActivity;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.Unit;
import com.stiggpwnz.vibes.adapters.UnitsAdapter;

public class UnitsDialog extends Dialog implements OnItemClickListener {

	private UnitsAdapter unitsAdapter;
	private NewActivity activity;
	private ImageLoader imageLoader;
	private List<Unit> units;

	public UnitsDialog(Context context, ImageLoader imageLoader, List<Unit> units) {
		super(context);
		activity = (NewActivity) getOwnerActivity();
		this.imageLoader = imageLoader;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		setCanceledOnTouchOutside(true);
		unitsAdapter = new UnitsAdapter(activity, units, imageLoader);
		ListView unitsList = (ListView) findViewById(R.id.listView);
		unitsList.setAdapter(unitsAdapter);
		unitsList.setEmptyView(findViewById(android.R.id.empty));
		unitsList.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> list, View view, int position, long id) {
		activity.setUnit((Unit) list.getItemAtPosition(position));
		activity.getApp().getSettings().setOwnerId(activity.getUnit().id);
		activity.showDialog(NewActivity.DIALOG_UNIT);
	}

	public void setList(List<Unit> list) {
		unitsAdapter.setUnits(list);
		unitsAdapter.notifyDataSetChanged();
	}

}
