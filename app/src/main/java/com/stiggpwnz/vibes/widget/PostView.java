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
import com.stiggpwnz.vibes.qualifiers.UnitClick;
import com.stiggpwnz.vibes.text.HashTagSpan;
import com.stiggpwnz.vibes.text.ReplaceTextSpan;
import com.stiggpwnz.vibes.text.VKLinkSpan;
import com.stiggpwnz.vibes.vk.models.Post;
import com.stiggpwnz.vibes.vk.models.Unit;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import dagger.Lazy;
import rx.subjects.PublishSubject;

/**
 * Created by adelnizamutdinov on 28/01/2014
 */
public class PostView extends LinearLayout {
    static final Pattern HASH_TAG = Pattern.compile("#[a-zA-Z][\\w@-]*");
    static final Pattern VK_UNIT  = Pattern.compile("\\[([^\\[\\|]+?)\\|([^\\]]+?)\\]");

    @Inject            Lazy<Picasso>        picassoLazy;
    @Inject @UnitClick PublishSubject<Unit> unitClicks;

    @InjectView(R.id.user_icon)  ImageView profilePic;
    @InjectView(R.id.user)       TextView  user;
    @InjectView(R.id.time)       TextView  time;
    @InjectView(R.id.text)       TextView  text;
    @InjectView(R.id.image_item) PhotoView image;

    final AudioView[] audioViews = new AudioView[10];

    @NotNull Post post;

    @SuppressWarnings("unused") public PostView(Context context) { super(context); }

    @SuppressWarnings("unused")
    public PostView(Context context, AttributeSet attrs) { super(context, attrs); }

    @SuppressWarnings("unused")
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
        unitClicks.onNext(post.getUnit());
    }

    @SuppressWarnings("ObjectAllocationInLoop")
    static SpannableString linkify(Context context, String string) {
        SpannableString text = new SpannableString(string);

        Matcher hasTags = HASH_TAG.matcher(text);
        while (hasTags.find()) {
            int start = hasTags.start();
            int end = hasTags.end();
            text.setSpan(new HashTagSpan(hasTags.group()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        Matcher units = VK_UNIT.matcher(text);
        while (hasTags.find()) {
            int start = units.start();
            int end = units.end();
            text.setSpan(new VKLinkSpan(units.group(1)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new ReplaceTextSpan(context, units.group(2)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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

        image.setPhoto(post.getFirstPhoto());

        for (int i = 0; i < post.getAudios().length; i++) {
            audioViews[i].setAudio(post.getAudios()[i]);
        }
        for (int i = post.getAudios().length; i < audioViews.length; i++) {
            audioViews[i].setVisibility(GONE);
        }
    }
}
