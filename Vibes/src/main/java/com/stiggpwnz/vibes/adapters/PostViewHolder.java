package com.stiggpwnz.vibes.adapters;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.text.HashTagSpan;
import com.stiggpwnz.vibes.text.ReplaceTextSpan;
import com.stiggpwnz.vibes.text.VKLinkSpan;
import com.stiggpwnz.vibes.util.Dagger;
import com.stiggpwnz.vibes.util.LazyVal;
import com.stiggpwnz.vibes.vk.models.Photo;
import com.stiggpwnz.vibes.vk.models.Post;
import com.stiggpwnz.vibes.widget.AudioView;
import com.stiggpwnz.vibes.widget.PhotoView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import dagger.Lazy;

import static com.stiggpwnz.vibes.adapters.InflaterAdapter.setVisibility;

/**
 * Created by adel on 11/9/13
 */
public class PostViewHolder {

    static final LazyVal<Pattern> HASH_TAG = new LazyVal<Pattern>() {
        @Override
        protected Pattern create() {
            return Pattern.compile("#[a-zA-Z][\\w@-]*");
        }
    };
    static final LazyVal<Pattern> VK_UNIT  = new LazyVal<Pattern>() {
        @Override
        protected Pattern create() {
            return Pattern.compile("\\[([^\\[\\|]+?)\\|([^\\]]+?)\\]");
        }
    };

    @Inject Lazy<Picasso> picassoLazy;

    @InjectView(R.id.user_icon)  ImageView profilePic;
    @InjectView(R.id.user)       TextView  user;
    @InjectView(R.id.time)       TextView  time;
    @InjectView(R.id.text)       TextView  text;
    @InjectView(R.id.image_item) PhotoView image;

    AudioView[] audioViews = new AudioView[10];

    Post post;

    PostViewHolder(LinearLayout convertView) {
        Dagger.inject(convertView.getContext(), this);
        ButterKnife.inject(this, convertView);

        for (int i = 0; i < audioViews.length; i++) {
            AudioView audioView = new AudioView(convertView.getContext());
            convertView.addView(audioView);
            audioViews[i] = audioView;
        }
    }

    @OnClick({R.id.user_icon, R.id.user})
    void showUser() {
        // TODO FUCK
    }

    static SpannableString linkify(Context context, String string) {
        SpannableString text = new SpannableString(string);

        Matcher m = HASH_TAG.get().matcher(text);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            text.setSpan(new HashTagSpan(m.group()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        m = VK_UNIT.get().matcher(text);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            text.setSpan(new VKLinkSpan(m.group(1)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new ReplaceTextSpan(context, m.group(2)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return text;
    }

    void setPost(Post post) {
        this.post = post;

        picassoLazy.get().load(post.unit.getProfilePic())
                .placeholder(R.drawable.ic_user_placeholder)
                .into(profilePic);

        user.setText(post.unit.getName());
        time.setText(post.relativeTimeString());

        if (post.hasText()) {
            text.setText(linkify(text.getContext(), post.text));
            setVisibility(text, View.VISIBLE);
        } else {
            setVisibility(text, View.GONE);
        }

        if (post.hasPhotos()) {
            Photo photo = post.photos[0];
            image.setPhoto(photo);
        } else {
            image.setPhoto(null);
        }

        for (int i = 0; i < post.audios.length; i++) {
            audioViews[i].setAudio(post.audios[i]);
        }
        for (int i = post.audios.length; i < audioViews.length; i++) {
            setVisibility(audioViews[i], View.GONE);
        }
    }
}
