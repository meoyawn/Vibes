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

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.VibesApplication;

public class LastFMUserDialog extends Dialog implements OnClickListener {

	private final String username;
	private final String userImage;

	private VibesApplication app;

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

		ImageLoader.getInstance().displayImage(
				userImage,
				(ImageView) findViewById(R.id.imageUser),
				new DisplayImageOptions.Builder().showStubImage(R.drawable.last_fm_logo).showImageForEmptyUri(R.drawable.last_fm_logo).cacheInMemory()
						.cacheOnDisc().build());
	}

	@Override
	public void onClick(View v) {
		app.getSettings().resetLastFM();
		dismiss();
	}

}
