package com.stiggpwnz.vibes.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.stiggpwnz.vibes.PlayerActivity;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.imageloader.ImageLoader;

public class LastFMUserDialog extends Dialog implements OnClickListener {

	private TextView textLastFmUsername;
	private ImageView imageLastFmUser;
	private ImageLoader imageLoader;

	public LastFMUserDialog(Context context, ImageLoader imageLoader) {
		super(context);
		this.imageLoader = imageLoader;
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
		((PlayerActivity) getOwnerActivity()).getApp().getSettings().resetLastFM();
		dismiss();
		getOwnerActivity().showDialog(PlayerActivity.DIALOG_LAST_FM_AUTH);
	}

	public void setText(String username) {
		textLastFmUsername.setText(username);
	}

	public void setUserImage(String userImage) {
		imageLoader.setStubId(R.drawable.last_fm_logo);
		imageLoader.displayImage(userImage, imageLastFmUser);
	}

}
