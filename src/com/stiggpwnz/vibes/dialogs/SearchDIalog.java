package com.stiggpwnz.vibes.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.stiggpwnz.vibes.NewActivity;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.Settings;
import com.stiggpwnz.vibes.VibesApplication;

public class SearchDIalog extends Dialog implements OnEditorActionListener {

	public SearchDIalog(Context context) {
		super(context);
		setContentView(R.layout.search);
		setTitle(getContext().getResources().getStringArray(R.array.playlist_options)[0]);
		setCanceledOnTouchOutside(true);
		EditText searchView = (EditText) findViewById(R.id.autocomplete);
		searchView.setOnEditorActionListener(this);

		Window window = getWindow();
		window.setGravity(Gravity.TOP);

		LayoutParams params = window.getAttributes();
		params.width = LayoutParams.FILL_PARENT;
		window.setAttributes((android.view.WindowManager.LayoutParams) params);
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		NewActivity activity = (NewActivity) getOwnerActivity();
		Settings settings = activity.getApp().getSettings();

		settings.setPlaylist(0);
		Log.d(VibesApplication.VIBES, "performing search with param: " + v.getText().toString());
		activity.runGetSongs(v.getText().toString());
		dismiss();
		activity.dismissDialog(NewActivity.DIALOG_PLAYLISTS);
		return true;
	}

}
