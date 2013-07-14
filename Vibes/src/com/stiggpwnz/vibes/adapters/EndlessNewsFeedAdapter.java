package com.stiggpwnz.vibes.adapters;

import retrofit.RetrofitError;
import android.content.Context;
import android.widget.ListAdapter;

import com.commonsware.cwac.endless.EndlessAdapter;
import com.stiggpwnz.vibes.util.Log;
import com.stiggpwnz.vibes.util.Persistance;
import com.stiggpwnz.vibes.vk.VKontakte;
import com.stiggpwnz.vibes.vk.models.NewsFeed;

public class EndlessNewsFeedAdapter extends EndlessAdapter {

	private NewsFeed.Result result;

	public EndlessNewsFeedAdapter(Context context, ListAdapter wrapped, int pendingResource) {
		super(context, wrapped, pendingResource);
	}

	@Override
	public NewsFeedAdapter getWrappedAdapter() {
		return (NewsFeedAdapter) super.getWrappedAdapter();
	}

	@Override
	protected void appendCachedData() {
		if (result != null && result.isResponse()) {
			getWrappedAdapter().append(result.response);
		}
	}

	@Override
	protected boolean cacheInBackground() throws Exception {
		int offset = getWrappedAdapter().getNewsFeed().new_offset;
		return getData(offset);

	}

	private boolean getData(int offset) throws Exception {
		try {
			result = VKontakte.get().getNewsFeed(offset);
			if (result.isResponse()) {
				return true;
			} else {
				if (result.error.isAuthError()) {
					Persistance.resetAuth();
					return getData(offset);
				} else {
					return false;
				}
			}
		} catch (RetrofitError e) {
			Log.e(e);
			return false;
		}
	}
}
