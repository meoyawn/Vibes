package com.stiggpwnz.vibes.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.VibesApplication;

public class LastFMUserDialog extends Dialog implements OnClickListener {

	private VibesApplication app;

	private String username;
	private String userImage;

	public LastFMUserDialog(Context context, String username, String userImage) {
		super(context);
		this.username = username;
		this.userImage = userImage;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		app = (VibesApplication) getOwnerActivity().getApplication();
		setContentView(R.layout.last_user);
		setCanceledOnTouchOutside(true);
		((Button) findViewById(R.id.btnSingOut)).setOnClickListener(this);
		((TextView) findViewById(R.id.textUser)).setText(username);

		// TODO integrate new imageloader

		listener.getImageLoader().setStubId(R.drawable.last_fm_logo);
		listener.getImageLoader().displayImage(userImage, (ImageView) findViewById(R.id.imageUser));
	}

	@Override
	public void onClick(View v) {
		app.getSettings().resetLastFM();
		dismiss();
	}

}
