package com.stiggpwnz.vibes.widget;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.stiggpwnz.vibes.R;
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
import mortar.Mortar;
import rx.subjects.PublishSubject;

/**
 * Created by adelnizamutdinov on 28/01/2014
 */
public class PostView extends LinearLayout {
    static final Pattern HASH_TAG = Pattern.compile("#[a-zA-Z][\\w@-]*");
    static final Pattern VK_UNIT  = Pattern.compile("\\[([^\\[\\|]+?)\\|([^\\]]+?)\\]");

    @Inject Lazy<Picasso>        picassoLazy;
    @Inject PublishSubject<Unit> unitClicks;

    @InjectView(R.id.original_user_icon)  ImageView profilePic;
    @InjectView(R.id.original_user_name)  TextView  user;
    @InjectView(R.id.original_time)       TextView  time;
    @InjectView(R.id.original_text)       TextView  upperText;
    @InjectView(R.id.image_item)          PhotoView image;
    @InjectView(R.id.repost_user_icon)    ImageView repostProfilePic;
    @InjectView(R.id.repost_user_name)    TextView  repostUserName;
    @InjectView(R.id.repost_time)         TextView  repostTime;
    @InjectView(R.id.repost_text)         TextView  lowerText;
    @InjectView(R.id.copy_user_container) View      copyContainer;

    final AudioView[] audioViews = new AudioView[10];

    @NotNull Post post;

    public PostView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override protected void onFinishInflate() {
        Context context = getContext();
        if (!isInEditMode()) {
            Mortar.inject(context, this);
            ButterKnife.inject(this);
        }

        if (context != null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            for (int i = 0; i < audioViews.length; i++) {
                audioViews[i] = (AudioView) inflater.inflate(R.layout.audio, this, false);
                addView(audioViews[i]);
            }
        }
    }

    @OnClick({R.id.original_user_icon, R.id.original_user_name}) void showUser() {
        unitClicks.onNext(post.getOriginalUnit());
    }

    @OnClick({R.id.repost_user_icon, R.id.repost_user_name}) void showRepostUser() {
        unitClicks.onNext(post.getRepostedUnit());
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
        while (units.find()) {
            int start = units.start();
            int end = units.end();
            text.setSpan(new VKLinkSpan(units.group(1)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new ReplaceTextSpan(context, units.group(2)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return text;
    }

    void drawPostTextOn(@NotNull TextView textView) {
        if (post.hasOriginalText()) {
            textView.setText(linkify(textView.getContext(), post.getText()));
            textView.setVisibility(VISIBLE);
        } else {
            textView.setVisibility(GONE);
        }
    }

    public void draw(@NotNull Post post) {
        this.post = post;

        picassoLazy.get().load(post.getOriginalUnit().getProfilePic())
                .placeholder(R.drawable.ic_user_placeholder)
                .into(profilePic);

        user.setText(post.getOriginalUnit().getName());
        time.setText(post.relativeTimeString());

        if (post.getRepostedUnit() != null) {
            if (post.hasRepostedText()) {
                upperText.setText(linkify(upperText.getContext(), post.getCopyText()));
                upperText.setVisibility(VISIBLE);
            } else {
                upperText.setVisibility(GONE);
            }

            picassoLazy.get().load(post.getRepostedUnit().getProfilePic())
                    .placeholder(R.drawable.ic_user_placeholder)
                    .into(repostProfilePic);
            repostUserName.setText(post.getRepostedUnit().getName());
            repostTime.setText(post.repostRelativeTimeString());

            drawPostTextOn(lowerText);

            copyContainer.setVisibility(VISIBLE);
        } else {
            copyContainer.setVisibility(GONE);

            drawPostTextOn(upperText);
        }

        image.setPhoto(post.getFirstPhoto());

        for (int i = 0; i < post.getAudios().length; i++) {
            audioViews[i].draw(post.getAudios()[i]);
        }
        for (int i = post.getAudios().length; i < audioViews.length; i++) {
            audioViews[i].setVisibility(GONE);
        }
    }
}
