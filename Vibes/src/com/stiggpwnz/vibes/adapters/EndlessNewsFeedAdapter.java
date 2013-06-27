package com.stiggpwnz.vibes.adapters;

import android.content.Context;
import android.widget.ListAdapter;

import com.commonsware.cwac.endless.EndlessAdapter;
import com.stiggpwnz.vibes.util.Log;
import com.stiggpwnz.vibes.util.Persistance;
import com.stiggpwnz.vibes.util.Singletons;
import com.stiggpwnz.vibes.vk.NewsFeed.Result;

public class EndlessNewsFeedAdapter extends EndlessAdapter {

	private Result result;

	public EndlessNewsFeedAdapter(Context context, ListAdapter wrapped, int pendingResource) {
		super(context, wrapped, pendingResource);
	}

	@Override
	public NewsFeedAdapter getWrappedAdapter() {
		return (NewsFeedAdapter) super.getWrappedAdapter();
	}

	@Override
	protected void appendCachedData() {
		if (result != null && result.response != null) {
			getWrappedAdapter().append(result.response);
		} else {
			// TODO handle the fucking errors
		}
	}

	@Override
	protected boolean cacheInBackground() throws Exception {
		try {
			int offset = getWrappedAdapter().getNewsFeed().new_offset;
			Log.d("loading for " + offset);
			Persistance.ensureAuth();
			result = Singletons.vkontakte.getNewsFeed(offset, Persistance.getAccessToken());
			return true;
		} catch (Exception e) {
			Log.e(e);
			return false;
		}
	}
}
