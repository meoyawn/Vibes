package com.stiggpwnz.vibes.widget;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.dagger.Dagger;
import com.stiggpwnz.vibes.text.HashTagSpan;
import com.stiggpwnz.vibes.text.ReplaceTextSpan;
import com.stiggpwnz.vibes.text.VKLinkSpan;
import com.stiggpwnz.vibes.util.LazyVal;
import com.stiggpwnz.vibes.vk.models.Photo;
import com.stiggpwnz.vibes.vk.models.Post;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import dagger.Lazy;

/**
 * Created by adelnizamutdinov on 28/01/2014
 */
public class PostView extends LinearLayout {
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

    final AudioView[] audioViews = new AudioView[10];

    @Nullable Post post;

    public PostView(Context context) { super(context); }

    public PostView(Context context, AttributeSet attrs) { super(context, attrs); }

    public PostView(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    @Override protected void onFinishInflate() {
        if (!isInEditMode()) {
            Dagger.inject(this);
            ButterKnife.inject(this);
        }

        if (getContext() != null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            for (int i = 0; i < audioViews.length; i++) {
                audioViews[i] = (AudioView) inflater.inflate(R.layout.audio, this, false);
                addView(audioViews[i]);
            }
        }
    }

    @OnClick({R.id.user_icon, R.id.user}) void showUser() {
        // TODO FUCK
    }

    @SuppressWarnings("ObjectAllocationInLoop")
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

    public void setPost(@NotNull Post post) {
        this.post = post;

        String profilePic1 = post.getUnit().getProfilePic();
        picassoLazy.get().load(profilePic1)
                .placeholder(R.drawable.ic_user_placeholder)
                .into(profilePic);

        user.setText(post.getUnit().getName());
        time.setText(post.relativeTimeString());

        if (post.hasText()) {
            text.setText(linkify(text.getContext(), post.getText()));
            text.setVisibility(VISIBLE);
        } else {
            text.setVisibility(GONE);
        }

        if (post.hasPhotos()) {
            Photo photo = post.getPhotos()[0];
            image.setPhoto(photo);
        } else {
            image.setPhoto(null);
        }

        for (int i = 0; i < post.getAudios().length; i++) {
            audioViews[i].setAudio(post.getAudios()[i]);
        }
        for (int i = post.getAudios().length; i < audioViews.length; i++) {
            audioViews[i].setVisibility(GONE);
        }
    }
}
