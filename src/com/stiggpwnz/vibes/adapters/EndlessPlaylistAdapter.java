package com.stiggpwnz.vibes.adapters;

import android.content.Context;
import android.widget.ListAdapter;

public class EndlessPlaylistAdapter extends EndlessAdapter {

	public EndlessPlaylistAdapter(Context context, ListAdapter wrapped, int pendingResource) {
		super(context, wrapped, pendingResource);
	}

	@Override
	protected boolean cacheInBackground() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void appendCachedData() {
		// TODO Auto-generated method stub

	}

}
