package com.stiggpwnz.vibes.fragments;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.stiggpwnz.vibes.PlayerActivity;
import com.stiggpwnz.vibes.R;

public class LastFMUserFragment extends SherlockDialogFragment implements OnClickListener {

	private static final String USERNAME = "username";
	private static final String USER_IMAGE = "user image";

	public static interface Listener extends FragmentListener {
		public void resetLastFM();
	}

	public static LastFMUserFragment newInstance(String username, String userImage) {
		LastFMUserFragment fragment = new LastFMUserFragment();
		Bundle args = new Bundle();
		args.putString(USERNAME, username);
		args.putString(USER_IMAGE, userImage);
		fragment.setArguments(args);
		return fragment;
	}

	private Listener listener;
	private ImageView imageLastFmUser;

	public LastFMUserFragment() {

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		listener = (Listener) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.last_user, container);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		getDialog().setCanceledOnTouchOutside(true);

		((Button) view.findViewById(R.id.btnSingOut)).setOnClickListener(this);
		((TextView) view.findViewById(R.id.textUser)).setText(getArguments().getString(USERNAME));

		imageLastFmUser = (ImageView) view.findViewById(R.id.imageUser);
		final String url = getArguments().getString(USER_IMAGE);
		if (url != null)
			new Thread("loading user image") {

				@Override
				public void run() {
					final Drawable drawable = new BitmapDrawable(getResources(), listener.getImageLoader().getBitmap(url));
					if (getSherlockActivity() != null)
						getSherlockActivity().runOnUiThread(new Runnable() {

							@Override
							public void run() {
								imageLastFmUser.setImageDrawable(drawable);
							}
						});
				}
			}.start();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
		PlayerActivity.recycle(imageLastFmUser);
	}

	@Override
	public void onClick(View v) {
		listener.resetLastFM();
		dismissAllowingStateLoss();
		new LastFMLoginFragment().show(getSherlockActivity().getSupportFragmentManager(), PlayerActivity.LAST_FM_LOGIN);
	}

}
