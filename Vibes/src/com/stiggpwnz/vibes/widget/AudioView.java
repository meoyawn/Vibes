package com.stiggpwnz.vibes.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.devspark.robototextview.widget.RobotoTextView;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.vk.Audio;

public class AudioView extends LinearLayout {

	public final ImageButton play;
	public final SeekBar seekBar;
	public final RobotoTextView artist;
	public final RobotoTextView title;

	private final Animation click;

	private Audio audio;

	public AudioView(Context context) {
		this(context, null);
	}

	private final OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			v.startAnimation(click);
			play.setImageResource(R.drawable.pause);

			seekBar.setVisibility(View.VISIBLE);
			seekBar.setProgress(20);
			seekBar.setSecondaryProgress(40);

			artist.setTextColor(getResources().getColor(R.color.text_white));
			title.setTextColor(getResources().getColor(R.color.text_white));
		}
	};

	public AudioView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.post_audio, this, true);

		play = (ImageButton) findViewById(R.id.imageButtonPlay);
		play.setOnClickListener(onClickListener);

		seekBar = (SeekBar) findViewById(R.id.seekBarAudio);
		artist = (RobotoTextView) findViewById(R.id.artist_audio);
		title = (RobotoTextView) findViewById(R.id.title_audio);

		click = AnimationUtils.loadAnimation(context, R.anim.click);
	}

	public void setAudio(Audio audio) {
		setVisibility(View.VISIBLE);

		artist.setText(audio.performer);
		title.setText(audio.title);

		this.audio = audio;
	}
}
