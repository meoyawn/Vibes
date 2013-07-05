package com.stiggpwnz.vibes.adapters;

import java.util.List;

import android.view.View;
import android.widget.ImageView;
import butterknife.InjectView;
import butterknife.Views;

import com.devspark.robototextview.widget.RobotoTextView;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.util.Log;
import com.stiggpwnz.vibes.vk.Audio;
import com.stiggpwnz.vibes.widget.AudioView;
import com.stiggpwnz.vibes.widget.PhotoView;

public class PostViewHolder {

	@InjectView(R.id.user_icon) ImageView profilePic;
	@InjectView(R.id.user) RobotoTextView user;
	@InjectView(R.id.time) RobotoTextView time;
	@InjectView(R.id.text) RobotoTextView text;
	@InjectView(R.id.image_item) PhotoView image;
	@InjectView(R.id.post_audio_0) AudioView audio0;
	@InjectView(R.id.post_audio_1) AudioView audio1;
	@InjectView(R.id.post_audio_2) AudioView audio2;
	@InjectView(R.id.post_audio_3) AudioView audio3;
	@InjectView(R.id.post_audio_4) AudioView audio4;
	@InjectView(R.id.post_audio_5) AudioView audio5;
	@InjectView(R.id.post_audio_6) AudioView audio6;
	@InjectView(R.id.post_audio_7) AudioView audio7;
	@InjectView(R.id.post_audio_8) AudioView audio8;
	@InjectView(R.id.post_audio_9) AudioView audio9;

	private final AudioView[] audioViews;

	public PostViewHolder(View convertView) {
		Views.inject(this, convertView);

		audioViews = new AudioView[10];
		audioViews[0] = audio0;
		audioViews[1] = audio1;
		audioViews[2] = audio2;
		audioViews[3] = audio3;
		audioViews[4] = audio4;
		audioViews[5] = audio5;
		audioViews[6] = audio6;
		audioViews[7] = audio7;
		audioViews[8] = audio8;
		audioViews[9] = audio9;
	}

	public void setAudios(List<Audio> audios) {
		if (audios.size() > 10) {
			Log.e("fucking A mate");
		}
		for (int i = 0; i < audios.size(); i++) {
			audioViews[i].setAudio(audios.get(i));
		}
		hideExtraAudios(audios.size());
	}

	private void hideExtraAudios(int lastAudio) {
		for (int i = lastAudio; i < audioViews.length; i++) {
			audioViews[i].setVisibility(View.GONE);
		}
	}
}