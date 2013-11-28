package com.stiggpwnz.vibes.widget;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.devspark.robototextview.widget.RobotoTextView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.adapters.InflaterAdapter;
import com.stiggpwnz.vibes.events.BufferingUpdatedEvent;
import com.stiggpwnz.vibes.events.ProgressUpdatedEvent;
import com.stiggpwnz.vibes.media.Player;
import com.stiggpwnz.vibes.util.Injector;
import com.stiggpwnz.vibes.vk.models.Audio;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import dagger.Lazy;

public class AudioView extends LinearLayout implements MediaPlayer.OnBufferingUpdateListener {

    @Inject Lazy<Player> playerLazy;
    @Inject Lazy<Bus>    busLazy;

    @InjectView(R.id.imageButtonPlay) ImageView      play;
    @InjectView(R.id.seekBarAudio)    SeekBar        seekBar;
    @InjectView(R.id.artist_audio)    RobotoTextView artist;
    @InjectView(R.id.title_audio)     RobotoTextView title;

    private Audio audio;

    public AudioView(Context context) {
        super(context);
        init(context);
    }

    public AudioView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AudioView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        Injector.inject(this);
        LayoutInflater.from(context).inflate(R.layout.audio, this, true);
        ButterKnife.inject(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        busLazy.get().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        busLazy.get().unregister(this);
    }

    @Subscribe
    public void onBufferingUpdated(BufferingUpdatedEvent event) {
        if (audio == playerLazy.get().audio) {
            InflaterAdapter.setVisibility(seekBar, View.VISIBLE);
            seekBar.setSecondaryProgress(event.percent * seekBar.getMax());
        } else {
            InflaterAdapter.setVisibility(seekBar, View.GONE);
        }
    }

    @Subscribe
    public void onProgressUpdated(ProgressUpdatedEvent event) {
        if (audio == playerLazy.get().audio) {
            InflaterAdapter.setVisibility(seekBar, View.VISIBLE);
            seekBar.setMax(event.max);
            seekBar.setProgress(event.position);
        } else {
            InflaterAdapter.setVisibility(seekBar, View.GONE);
        }
    }

    public void setAudio(Audio audio) {
        this.audio = audio;

        artist.setText(audio.artist);
        title.setText(audio.title);
    }

    @OnClick(R.id.imageButtonPlay)
    void play() {
        play.setImageResource(R.drawable.btn_play_active);

        playerLazy.get().play(audio);

        artist.setTextColor(getResources().getColor(R.color.text_white));
        title.setTextColor(getResources().getColor(R.color.text_white));
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        seekBar.setSecondaryProgress(percent * seekBar.getMax());
    }
}
