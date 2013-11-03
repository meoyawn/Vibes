package com.stiggpwnz.vibes.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.devspark.robototextview.widget.RobotoTextView;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.Vibes;
import com.stiggpwnz.vibes.events.UnitClickedEvent;
import com.stiggpwnz.vibes.vk.models.Feed;
import com.stiggpwnz.vibes.vk.models.Photo;
import com.stiggpwnz.vibes.vk.models.Post;
import com.stiggpwnz.vibes.widget.AudioView;
import com.stiggpwnz.vibes.widget.PhotoView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Views;
import dagger.Lazy;

public class FeedAdapter extends InflaterAdapter {

    Feed feed;

    public FeedAdapter(Context context, Feed feed) {
        super(context);
        this.feed = feed;
    }

    @Override
    public int getCount() {
        if (feed != null && feed.items != null) {
            return feed.items.size();
        }
        return 0;
    }

    @Override
    public Post getItem(int arg0) {
        return feed.items.get(arg0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.post, parent, false);
            convertView.setTag(new PostViewHolder((LinearLayout) convertView));
        }

        PostViewHolder holder = (PostViewHolder) convertView.getTag();
        Post post = getItem(position);
        holder.setPost(post);

        return convertView;
    }

    public static class PostViewHolder {

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
            Vibes.from(convertView.getContext()).inject(this);
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

        void setPost(Post post) {
            this.post = post;
            picassoLazy.get().load(post.unit.getProfilePic())
                    .placeholder(R.drawable.ic_user_placeholder)
                    .into(profilePic);

            user.setText(post.unit.getName());
            time.setText(DateUtils.getRelativeTimeSpanString(post.date * 1000));

            if (!TextUtils.isEmpty(post.text)) {
                text.setText(post.text);
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
}
