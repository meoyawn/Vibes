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

import com.stiggpwnz.vibes.GeneralListener;
import com.stiggpwnz.vibes.R;

public class LastFMUserDialog extends Dialog implements OnClickListener {

	public static interface LastFMUserListener extends GeneralListener {
		public void resetLastFM();
	}

	private String username;
	private String userImage;
	private LastFMUserListener listener;

	public LastFMUserDialog(Context context, String username, String userImage) {
		super(context);
		this.username = username;
		this.userImage = userImage;
		this.listener = (LastFMUserListener) context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.last_user);
		setCanceledOnTouchOutside(true);
		((Button) findViewById(R.id.btnSingOut)).setOnClickListener(this);
		((TextView) findViewById(R.id.textUser)).setText(username);

		listener.getImageLoader().setStubId(R.drawable.last_fm_logo);
		listener.getImageLoader().displayImage(userImage, (ImageView) findViewById(R.id.imageUser));
	}

	@Override
	public void onClick(View v) {
		listener.resetLastFM();
		dismiss();
	}

}
