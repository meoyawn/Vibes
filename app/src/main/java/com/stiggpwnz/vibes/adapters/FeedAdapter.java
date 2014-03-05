package com.stiggpwnz.vibes.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.vk.models.Post;

public class FeedAdapter extends InflaterAdapter {

    Post[] posts;

    public FeedAdapter(Context context, Post... posts) {
        super(context);
        this.posts = posts;
    }

    @Override
    public int getCount() {
        return posts != null ? posts.length : 0;
    }

    @Override
    public Post getItem(int position) {
        return posts[position];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.post, parent, false);
            convertView.setTag(new PostViewHolder((LinearLayout) convertView));
        }

        Post post = getItem(position);
        PostViewHolder holder = (PostViewHolder) convertView.getTag();
        holder.setPost(post);

        return convertView;
    }

}
