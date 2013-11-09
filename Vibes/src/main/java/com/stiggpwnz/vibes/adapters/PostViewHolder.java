package com.stiggpwnz.vibes.adapters;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.devspark.robototextview.widget.RobotoTextView;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.VibesApplication;
import com.stiggpwnz.vibes.events.UnitClickedEvent;
import com.stiggpwnz.vibes.vk.models.Photo;
import com.stiggpwnz.vibes.vk.models.Post;
import com.stiggpwnz.vibes.widget.AudioView;
import com.stiggpwnz.vibes.widget.PhotoView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Views;
import dagger.Lazy;
import timber.log.Timber;

/**
 * Created by adel on 11/9/13
 */
public class PostViewHolder {

    static final Pattern HASH_TAGS_PATTERN   = Pattern.compile("(#[a-zA-Z][\\w-]*)");
    static final Pattern HYPER_LINKS_PATTERN = Pattern.compile("^(((?i:https?)://)?[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)+.*)$");

    @Inject Lazy<Picasso> picassoLazy;
    @Inject Lazy<Bus>     busLazy;

    @InjectView(R.id.user_icon)  ImageView      profilePic;
    @InjectView(R.id.user)       RobotoTextView user;
    @InjectView(R.id.time)       RobotoTextView time;
    @InjectView(R.id.text)       RobotoTextView text;
    @InjectView(R.id.image_item) PhotoView      image;

    final AudioView[] audioViews = new AudioView[10];

    Post post;

    PostViewHolder(LinearLayout convertView) {
        VibesApplication.from(convertView.getContext()).inject(this);
        Views.inject(this, convertView);

        for (int i = 0; i < audioViews.length; i++) {
            AudioView audioView = new AudioView(convertView.getContext());
            convertView.addView(audioView);
            audioViews[i] = audioView;
        }
    }

    @OnClick({R.id.user_icon, R.id.user})
    void showUser() {
        busLazy.get().post(new UnitClickedEvent(post.source_id));
    }

    void linkify(SpannableString text) {
        Matcher m = HASH_TAGS_PATTERN.matcher(text);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            Timber.d("matched %s", m.group());
            text.setSpan(new URLSpan("http://vk.com"), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        m = HYPER_LINKS_PATTERN.matcher(text);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            Timber.d("matched %s", m.group());
            text.setSpan(new URLSpan(m.group()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    void setPost(Post post) {
        this.post = post;
        picassoLazy.get().load(post.unit.getProfilePic())
                .placeholder(R.drawable.ic_user_placeholder)
                .into(profilePic);

        user.setText(post.unit.getName());
        time.setText(DateUtils.getRelativeTimeSpanString(post.date * 1000));

        if (!TextUtils.isEmpty(post.text)) {
            SpannableString string = new SpannableString(post.text);
            linkify(string);
            text.setText(string);
            InflaterAdapter.setVisibility(text, View.VISIBLE);
        } else {
            InflaterAdapter.setVisibility(text, View.GONE);
        }

        if (post.photos.size() > 0) {
            final Photo photo = post.photos.get(0);
            image.setPhoto(photo);
        } else {
            image.setPhoto(null);
        }

        for (int i = 0; i < post.audios.size(); i++) {
            audioViews[i].setAudio(post.audios.get(i));
        }
        for (int i = post.audios.size(); i < audioViews.length; i++) {
            audioViews[i].setVisibility(View.GONE);
        }
    }
}
