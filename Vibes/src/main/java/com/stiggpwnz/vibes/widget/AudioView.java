package com.stiggpwnz.vibes.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.devspark.robototextview.widget.RobotoTextView;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.vk.models.Audio;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class AudioView extends LinearLayout {

    @InjectView(R.id.imageButtonPlay) ImageView      play;
    @InjectView(R.id.seekBarAudio)    SeekBar        seekBar;
    @InjectView(R.id.artist_audio)    RobotoTextView artist;
    @InjectView(R.id.title_audio)     RobotoTextView title;

    private Audio audio;

    public AudioView(Context context) {
        this(context, null);
    }

    public AudioView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.audio, this, true);
        ButterKnife.inject(this);
    }

    public void setAudio(Audio audio) {
        this.audio = audio;

        setVisibility(View.VISIBLE);

        artist.setText(audio.artist);
        title.setText(audio.title);
    }

    @OnClick(R.id.imageButtonPlay)
    void play() {
        play.setImageResource(R.drawable.btn_play_active);

        seekBar.setVisibility(View.VISIBLE);
        seekBar.setProgress(20);
        seekBar.setSecondaryProgress(40);

        artist.setTextColor(getResources().getColor(R.color.text_white));
        title.setTextColor(getResources().getColor(R.color.text_white));
    }
}
