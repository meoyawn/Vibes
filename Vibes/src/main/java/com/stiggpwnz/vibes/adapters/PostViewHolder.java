package com.stiggpwnz.vibes.adapters;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.ClickableSpan;
import android.text.style.ReplacementSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.devspark.robototextview.widget.RobotoTextView;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.events.UnitClickedEvent;
import com.stiggpwnz.vibes.util.Injector;
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
import timber.log.Timber;

import static com.stiggpwnz.vibes.adapters.InflaterAdapter.setVisibility;

/**
 * Created by adel on 11/9/13
 */
public class PostViewHolder {

    static final Pattern HASH_TAGS_PATTERN = Pattern.compile("#[a-zA-Z][\\w@-]*");
    static final Pattern VK_LINK           = Pattern.compile("\\[([^\\[\\|]+?)\\|([^\\]]+?)\\]");

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
        Injector.inject(convertView.getContext(), this);
        ButterKnife.inject(this, convertView);

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

    static class HashTagSpan extends ClickableSpan {

        String hashtag;

        public HashTagSpan(String hashtag) {
            this.hashtag = hashtag;
        }

        @Override
        public void onClick(View widget) {

        }
    }

    static class VKLinkSpan extends ClickableSpan {

        String path;

        public VKLinkSpan(String group) {
            path = group;
        }

        @Override
        public void onClick(View widget) {
            Timber.d("clicked on %s group", path);
        }
    }

    static class ReplaceTextSpan extends ReplacementSpan {

        String replacement;

        public ReplaceTextSpan(String replacement) {
            this.replacement = replacement;
        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
            return Math.round(paint.measureText(replacement));
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            paint.setColor(Color.parseColor("#33b5e5"));
            paint.setUnderlineText(true);
            canvas.drawText(replacement, x, y, paint);
        }
    }

    static SpannableString linkify(String string) {
        SpannableString text = new SpannableString(string);

        Matcher m = HASH_TAGS_PATTERN.matcher(text);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            text.setSpan(new HashTagSpan(m.group()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        m = VK_LINK.matcher(text);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            text.setSpan(new VKLinkSpan(m.group(1)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new ReplaceTextSpan(m.group(2)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return text;
    }

    void setPost(Post post) {
        this.post = post;
        picassoLazy.get().load(post.unit.getProfilePic())
                .placeholder(R.drawable.ic_user_placeholder)
                .into(profilePic);

        user.setText(post.unit.getName());
        time.setText(DateUtils.getRelativeTimeSpanString(post.date * 1000));

        if (!TextUtils.isEmpty(post.text)) {
            text.setText(linkify(post.text));
            setVisibility(text, View.VISIBLE);
        } else {
            setVisibility(text, View.GONE);
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
