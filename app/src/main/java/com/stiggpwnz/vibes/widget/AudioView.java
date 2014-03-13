package com.stiggpwnz.vibes.widget;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.vk.models.Audio;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import mortar.Mortar;

public class AudioView extends LinearLayout implements MediaPlayer.OnBufferingUpdateListener {
//    @Inject Lazy<Player> playerLazy;

    @InjectView(R.id.imageButtonPlay) ImageView play;
    @InjectView(R.id.seekBarAudio)    SeekBar   seekBar;
    @InjectView(R.id.artist_audio)    TextView  artist;
    @InjectView(R.id.title_audio)     TextView  title;

    @Nullable Audio audio;

    public AudioView(Context context, AttributeSet attrs) { super(context, attrs); }

    @Override protected void onFinishInflate() {
        if (!isInEditMode()) {
            Mortar.inject(getContext(), this);
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

        if (getResources() != null) {
            artist.setTextColor(getResources().getColor(R.color.text_white));
            title.setTextColor(getResources().getColor(R.color.text_white));
        }
    }

    @Override public void onBufferingUpdate(MediaPlayer mp, int percent) {
        seekBar.setSecondaryProgress(percent * seekBar.getMax());
    }
}
