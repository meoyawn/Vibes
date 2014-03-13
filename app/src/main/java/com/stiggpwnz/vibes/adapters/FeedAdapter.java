package com.stiggpwnz.vibes.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.vk.models.Post;
import com.stiggpwnz.vibes.widget.PostView;

import org.jetbrains.annotations.NotNull;

public class FeedAdapter extends InflaterAdapter {
    final @NotNull Post[] posts;

    public FeedAdapter(LayoutInflater layoutInflater, @NotNull Post[] posts) {
        super(layoutInflater);
        this.posts = posts;
    }

    @Override public int getCount() { return posts.length; }

    @Override public Post getItem(int position) { return posts[position]; }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.post, parent, false);
        }

        @NotNull PostView postView = (PostView) convertView;
        postView.draw(getItem(position));

        return convertView;
    }
}
