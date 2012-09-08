package com.stiggpwnz.vibes.dialogs;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.stiggpwnz.vibes.PlayerActivity;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.adapters.UnitsAdapter;
import com.stiggpwnz.vibes.imageloader.ImageLoader;
import com.stiggpwnz.vibes.restapi.Unit;

public class UnitsDialog extends Dialog implements OnItemClickListener {

	private UnitsAdapter unitsAdapter;

	public UnitsDialog(Context context, ImageLoader imageLoader, List<Unit> units, Typeface typeface) {
		super(context);
		setContentView(R.layout.list);
		setCanceledOnTouchOutside(true);
		unitsAdapter = new UnitsAdapter(context, typeface, units, imageLoader);
		ListView unitsList = (ListView) findViewById(R.id.listView);
		unitsList.setAdapter(unitsAdapter);
		unitsList.setEmptyView(findViewById(android.R.id.empty));
		unitsList.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> list, View view, int position, long id) {
		PlayerActivity activity = (PlayerActivity) getOwnerActivity();
		Unit unit = (Unit) list.getItemAtPosition(position);
		activity.setUnit(unit);
		activity.getApp().getSettings().setOwnerId(unit.id);
		activity.showDialog(PlayerActivity.DIALOG_UNIT);
	}

	public void setList(List<Unit> units) {
		unitsAdapter.setUnits(units);
		unitsAdapter.notifyDataSetChanged();
	}

}
