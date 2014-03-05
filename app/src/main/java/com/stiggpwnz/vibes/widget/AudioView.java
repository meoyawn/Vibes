package com.stiggpwnz.vibes.widget;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.media.Player;
import com.stiggpwnz.vibes.util.Dagger;
import com.stiggpwnz.vibes.vk.models.Audio;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import dagger.Lazy;

// FIXME inflating linear with linear
public class AudioView extends LinearLayout implements MediaPlayer.OnBufferingUpdateListener {

    @Inject Lazy<Player> playerLazy;

    @InjectView(R.id.imageButtonPlay) ImageView play;
    @InjectView(R.id.seekBarAudio)    SeekBar   seekBar;
    @InjectView(R.id.artist_audio)    TextView  artist;
    @InjectView(R.id.title_audio)     TextView  title;

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
        Dagger.inject(this);
        LayoutInflater.from(context).inflate(R.layout.audio, this, true);
        ButterKnife.inject(this);
    }

    public void setAudio(Audio audio) {
        this.audio = audio;

        artist.setText(audio.getArtist());
        title.setText(audio.getTitle());
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
