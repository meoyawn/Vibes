package com.stiggpwnz.vibes.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.stiggpwnz.vibes.R;

public class SleepingTimerFragment extends SherlockDialogFragment {

	public static interface SleepingTimerListener {
		public void setTimer(int minutes);
	}

	private SleepingTimerListener listener;

	public SleepingTimerFragment() {

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		listener = (SleepingTimerListener) activity;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View view = getSherlockActivity().getLayoutInflater().inflate(R.layout.timer, null);
		final TextView shutdown = (TextView) view.findViewById(R.id.textShutdown);
		final TextView counter = (TextView) view.findViewById(R.id.textHoursCount);
		final SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBarHours);
		seekBar.setMax(24);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (progress == 0) {
					shutdown.setText(R.string.dont_shutdown);
					counter.setText(null);
				} else {
					int total = progress * 15;
					int intMinutes = total % 60;
					String minutes = intMinutes > 0 ? String.valueOf(intMinutes) : "00";
					shutdown.setText(R.string.shutdown_after);
					counter.setText(total / 60 + ":" + minutes + " " + getString(R.string.hours));
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

		});

		AlertDialog dialog = new AlertDialog.Builder(getSherlockActivity()).setTitle(R.string.sleep_timer).setView(view).setPositiveButton(R.string.save, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				listener.setTimer(seekBar.getProgress() * 15);
			}
		}).setNegativeButton(R.string.discard, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		}).create();
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

}
