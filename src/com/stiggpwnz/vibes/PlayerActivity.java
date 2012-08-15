package com.stiggpwnz.vibes;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.stiggpwnz.vibes.adapters.AlbumsAdapter;
import com.stiggpwnz.vibes.adapters.TextAdapter;
import com.stiggpwnz.vibes.adapters.UnitsAdapter;
import com.stiggpwnz.vibes.dialogs.UnitDialog;

@SuppressLint("HandlerLeak")
public class PlayerActivity extends Activity implements OnClickListener, OnSeekBarChangeListener, OnItemClickListener {

	

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {

		case DIALOG_ALBUMS:
			List<Album> albumList;
			if (app.getOwnerId() != 0 && unit != null)
				albumList = unit.albums;
			else
				albumList = myAlbums;
			albumsAdapter.setAlbums(albumList);
			albumsAdapter.notifyDataSetChanged();
			break;

		case DIALOG_LAST_FM_USER:

			if (app.getUsername() != null) {
				textLastFmUsername.setText(app.getUsername());
				imageLoader.setStubId(R.drawable.last_fm_logo);
				imageLoader.displayImage(app.getUserImage(), imageLastFmUser);
			}
			break;

		case DIALOG_UNITS:
			String[] array = getResources().getStringArray(R.array.playlist_options);
			String title = frnds ? array[1] : array[2];
			dialog.setTitle(title);

			unitsAdapter.setUnits(frnds ? friends : groups);
			unitsAdapter.notifyDataSetChanged();
			break;

		case DIALOG_UNIT:
			if (unit != null && unit.name != null)
				dialog.setTitle(unit.name);
			break;
		}
	}

	private void seekbarProgress(int progress) {
		seekbar.setProgress(progress);
		int seconds = (progress / 1000) % 60;
		int minutes = (progress / 1000) / 60;
		if (seconds > 9)
			textPassed.setText(String.format("%d:%d", minutes, seconds));
		else
			textPassed.setText(String.format("%d:0%d", minutes, seconds));
		seconds = ((songDuration - progress) / 1000) % 60;
		minutes = ((songDuration - progress) / 1000) / 60;
		if (seconds > 9)
			textLeft.setText(String.format("%d:%d", minutes, seconds));
		else
			textLeft.setText(String.format("%d:0%d", minutes, seconds));
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

}