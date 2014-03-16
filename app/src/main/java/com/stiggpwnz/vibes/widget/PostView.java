package com.stiggpwnz.vibes.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.stiggpwnz.vibes.Dagger;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.vk.models.Post;
import com.stiggpwnz.vibes.vk.models.Unit;

import org.jetbrains.annotations.NotNull;

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

    public PostView(Context context, AttributeSet attrs) { super(context, attrs); }

    @Override protected void onFinishInflate() {
        Context context = getContext();
        if (!isInEditMode()) {
            Dagger.inject(this);
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


    void drawPostTextOn(@NotNull TextView textView) {
        if (post.hasOriginalText()) {
            textView.setText(post.getText());
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
                upperText.setText(post.getCopyText());
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
