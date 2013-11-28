package com.stiggpwnz.vibes.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.vk.models.Feed;
import com.stiggpwnz.vibes.vk.models.Post;

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

}
