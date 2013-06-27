package com.stiggpwnz.vibes.adapters;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.devspark.robototextview.widget.RobotoTextView;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.util.Log;
import com.stiggpwnz.vibes.vk.Audio;
import com.stiggpwnz.vibes.widget.AudioView;
import com.stiggpwnz.vibes.widget.PhotoView;

public class PostViewHolder {

	public final ImageView profilePic;
	public final RobotoTextView user;
	public final RobotoTextView time;
	public final RobotoTextView text;
	public final PhotoView image;
	public final LinearLayout audiosHolder;

	private final List<AudioView> audioViews;

	public PostViewHolder(View convertView, int neededAudios) {
		profilePic = (ImageView) convertView.findViewById(R.id.user_icon);
		user = (RobotoTextView) convertView.findViewById(R.id.user);
		time = (RobotoTextView) convertView.findViewById(R.id.time);
		text = (RobotoTextView) convertView.findViewById(R.id.text);
		image = (PhotoView) convertView.findViewById(R.id.image_item);
		audiosHolder = (LinearLayout) convertView.findViewById(R.id.holder_audios);

		audioViews = new ArrayList<AudioView>(neededAudios);
		for (int i = 0; i < neededAudios; i++) {
			AudioView audioView = new AudioView(convertView.getContext());
			audioViews.add(audioView);
			audiosHolder.addView(audioView);
		}
	}

	public void setAudios(List<Audio> audios) {
		if (audios.size() > 10) {
			Log.e("fucking A mate");
		}
		for (int i = 0; i < audios.size(); i++) {
			getAudioView(i).setAudio(audios.get(i));
		}
		hideExtraAudios(audios.size());
	}

	private AudioView getAudioView(int i) {
		if (i < audioViews.size()) {
			return audioViews.get(i);
		} else {
			AudioView audioView = new AudioView(profilePic.getContext());
			audioViews.add(audioView);
			audiosHolder.addView(audioView);
			return audioView;
		}
	}

	private void hideExtraAudios(int lastAudio) {
		for (int i = lastAudio; i < audioViews.size(); i++) {
			audioViews.get(i).setVisibility(View.GONE);
		}
	}
}