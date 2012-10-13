package com.stiggpwnz.vibes.fragments;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.stiggpwnz.vibes.R;

public class TutorialFragment extends SherlockDialogFragment {

	public static interface TutorialListener {
		public void onTutorialSwipe();
	}

	private TutorialListener listener;

	public TutorialFragment() {

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		listener = (TutorialListener) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.tutorial, container);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		setCancelable(false);

		view.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				dismissAllowingStateLoss();
				listener.onTutorialSwipe();
				return true;
			}
		});
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

}
