package com.stiggpwnz.vibes.adapters;

import static com.googlecode.cqengine.query.QueryFactory.equal;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cuubonandroid.sugaredlistanimations.GPlusListAdapter;
import com.cuubonandroid.sugaredlistanimations.SpeedScrollListener;
import com.googlecode.cqengine.CQEngine;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.index.unique.UniqueIndex;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.vk.models.Audio;
import com.stiggpwnz.vibes.vk.models.NewsFeed;
import com.stiggpwnz.vibes.vk.models.Photo;
import com.stiggpwnz.vibes.vk.models.Post;
import com.stiggpwnz.vibes.vk.models.Unit;

public class NewsFeedAdapter extends GPlusListAdapter {

	private NewsFeed newsFeed;
	private final LayoutInflater inflater;
	private final IndexedCollection<Unit> units = CQEngine.newInstance();

	public NewsFeedAdapter(Context context, NewsFeed newsFeed, SpeedScrollListener scrollListener) {
		super(context, scrollListener);

		inflater = LayoutInflater.from(context);
		this.newsFeed = newsFeed;

		units.addIndex(UniqueIndex.onAttribute(Unit.ID));

		filter(newsFeed);
	}

	private void filter(NewsFeed newsFeed) {
		List<Post> posts = new ArrayList<Post>();
		for (Post post : newsFeed.items) {
			if (post.hasAudios()) {
				posts.add(post);
			}
		}
		newsFeed.items = posts;

		units.addAll(newsFeed.profiles);
		units.addAll(newsFeed.groups);
	}

	public void append(NewsFeed newsFeed) {
		filter(newsFeed);

		this.newsFeed.new_from = newsFeed.new_from;
		this.newsFeed.new_offset = newsFeed.new_offset;

		this.newsFeed.items.addAll(newsFeed.items);
		this.newsFeed.profiles.addAll(newsFeed.profiles);
		this.newsFeed.groups.addAll(newsFeed.groups);
	}

	@Override
	public int getCount() {
		if (newsFeed != null && newsFeed.items != null) {
			return newsFeed.items.size();
		}
		return 0;
	}

	@Override
	public Post getItem(int arg0) {
		return newsFeed.items.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private final DisplayImageOptions userOptions = new DisplayImageOptions.Builder().showStubImage(R.drawable.ic_user_placeholder)
			.showImageForEmptyUri(R.drawable.ic_user_placeholder).cacheInMemory().cacheOnDisc().build();

	@Override
	protected View getRowView(int position, View convertView, ViewGroup parent) {
		Post post = getItem(position);
		List<Audio> audios = post.audios;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.post, parent, false);
			convertView.setTag(new PostViewHolder(convertView));
		}

		final PostViewHolder holder = (PostViewHolder) convertView.getTag();

		Unit unit = units.retrieve(equal(Unit.ID, post.source_id)).uniqueResult();

		ImageLoader.getInstance().displayImage(unit.getProfilePic(), holder.profilePic, userOptions);

		holder.user.setText(unit.getName());
		holder.time.setText(DateUtils.getRelativeTimeSpanString(post.date * 1000));

		String text = post.shortText;
		if (!TextUtils.isEmpty(text)) {
			holder.text.setText(text);
			holder.text.setVisibility(View.VISIBLE);
		} else {
			holder.text.setVisibility(View.GONE);
		}

		if (post.photos.size() > 0) {
			final Photo photo = post.photos.get(0);
			holder.image.setPhoto(photo);
		} else {
			holder.image.setPhoto(null);
		}

		holder.setAudios(audios);

		return convertView;
	}

	public NewsFeed getNewsFeed() {
		return newsFeed;
	}

}
