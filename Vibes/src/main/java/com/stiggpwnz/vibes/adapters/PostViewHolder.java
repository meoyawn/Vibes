package com.stiggpwnz.vibes.adapters;

import android.view.View;
import android.widget.ImageView;

import com.devspark.robototextview.widget.RobotoTextView;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.vk.models.Audio;
import com.stiggpwnz.vibes.widget.AudioView;
import com.stiggpwnz.vibes.widget.PhotoView;

import java.util.List;

import butterknife.InjectView;
import butterknife.Views;

public class PostViewHolder {

    @InjectView(R.id.user_icon)  ImageView      profilePic;
    @InjectView(R.id.user)       RobotoTextView user;
    @InjectView(R.id.time)       RobotoTextView time;
    @InjectView(R.id.text)       RobotoTextView text;
    @InjectView(R.id.image_item) PhotoView      image;

    final AudioView[] audioViews = new AudioView[10];

    public PostViewHolder(View convertView) {
        Views.inject(this, convertView);

        for (int i = 0; i < audioViews.length; i++) {
            audioViews[i] = new AudioView(convertView.getContext());
        }
    }

    public void setAudios(List<Audio> audios) {
        if (audios.size() > 10) {
            throw new IndexOutOfBoundsException("too much audios");
        }
        for (int i = 0; i < audios.size(); i++) {
            audioViews[i].setAudio(audios.get(i));
        }
        hideExtraAudios(audios.size());
    }

    void hideExtraAudios(int lastAudio) {
        for (int i = lastAudio; i < audioViews.length; i++) {
            audioViews[i].setVisibility(View.GONE);
        }
    }
}