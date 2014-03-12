package com.stiggpwnz.vibes.widget;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.dagger.Dagger;
import com.stiggpwnz.vibes.player.Player;
import com.stiggpwnz.vibes.vk.models.Audio;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import dagger.Lazy;

public class AudioView extends LinearLayout implements MediaPlayer.OnBufferingUpdateListener {
    @Inject Lazy<Player> playerLazy;

    @InjectView(R.id.imageButtonPlay) ImageView play;
    @InjectView(R.id.seekBarAudio)    SeekBar   seekBar;
    @InjectView(R.id.artist_audio)    TextView  artist;
    @InjectView(R.id.title_audio)     TextView  title;

    @Nullable Audio audio;

    public AudioView(Context context) { super(context); }

    public AudioView(Context context, AttributeSet attrs) { super(context, attrs); }

    public AudioView(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    @Override protected void onFinishInflate() {
        if (!isInEditMode()) {
            Dagger.inject(this);
            ButterKnife.inject(this);
        }
    }

    public void draw(@NotNull Audio audio) {
        this.audio = audio;

        artist.setText(audio.getArtist());
        title.setText(audio.getTitle());
    }

    @OnClick(R.id.imageButtonPlay) void play() {
        play.setImageResource(R.drawable.btn_play_active);

        playerLazy.get().play(audio);

        if (getResources() != null) {
            artist.setTextColor(getResources().getColor(R.color.text_white));
            title.setTextColor(getResources().getColor(R.color.text_white));
        }
    }

    @Override public void onBufferingUpdate(MediaPlayer mp, int percent) {
        seekBar.setSecondaryProgress(percent * seekBar.getMax());
    }
}
