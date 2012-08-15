package com.stiggpwnz.vibes.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.stiggpwnz.vibes.ImageLoader;
import com.stiggpwnz.vibes.NewActivity;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.Settings;

public class LastFMUserDialog extends Dialog implements OnClickListener {

	private TextView textLastFmUsername;
	private ImageView imageLastFmUser;
	private Settings settings;
	private ImageLoader imageLoader;

	public LastFMUserDialog(Context context, ImageLoader imageLoader) {
		super(context);
		this.imageLoader = imageLoader;
		settings = ((NewActivity) getOwnerActivity()).getApp().getSettings();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.last_user);
		setTitle(R.string.account);
		setCanceledOnTouchOutside(true);
		Button btnSignOut = (Button) findViewById(R.id.btnSingOut);
		btnSignOut.setOnClickListener(this);
		textLastFmUsername = (TextView) findViewById(R.id.textUser);
		imageLastFmUser = (ImageView) findViewById(R.id.imageUser);
	}

	@Override
	public void onClick(View v) {
		settings.resetLastFM();
		dismiss();
		getOwnerActivity().showDialog(NewActivity.DIALOG_LAST_FM_AUTH);
	}

	public void setText(String username) {
		textLastFmUsername.setText(username);
	}

	public void setUserImage(String userImage) {
		if (settings.getUserImage() != null) {
			imageLoader.setStubId(R.drawable.last_fm_logo);
			imageLoader.displayImage(userImage, imageLastFmUser);
		}
	}

}
