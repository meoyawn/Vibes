package com.stiggpwnz.vibes.fragments;

import java.io.IOException;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.cuubonandroid.sugaredlistanimations.SpeedScrollListener;
import com.origamilabs.library.views.StaggeredGridView;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.adapters.EndlessNewsFeedAdapter;
import com.stiggpwnz.vibes.adapters.NewsFeedAdapter;
import com.stiggpwnz.vibes.util.Persistance;
import com.stiggpwnz.vibes.util.Rest;
import com.stiggpwnz.vibes.vk.AuthException;
import com.stiggpwnz.vibes.vk.NewsFeed.Result;

public class NewsFeedFragment extends VibesProgressFragment {

	private SpeedScrollListener scrollListener;
	private ListView listView;
	private StaggeredGridView gridView;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		setContentView(R.layout.newsfeed);
		View view = getView();

		scrollListener = new SpeedScrollListener();

		listView = (ListView) view.findViewById(R.id.list);
		gridView = (StaggeredGridView) view.findViewById(R.id.grid);

		if (listView != null) {
			listView.setOnScrollListener(scrollListener);
		}

		if (result != null) {
			postResult(result);
		}

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		listView = null;
		gridView = null;
	}

	@Override
	public void onFirstCreated(View view) {
		runOnBackgroundThread(new Runnable() {

			@Override
			public void run() {
				makeRequest();
			}
		});
	}

	private Result result;

	// UI thread
	private void postResult(final Result result) {
		this.result = result;
		if (result.response != null) {
			setListAdapter(new EndlessNewsFeedAdapter(getSherlockActivity(), new NewsFeedAdapter(getSherlockActivity(), result.response, scrollListener),
					R.layout.loading_footer));
		} else {
			// TODO handle the fucking error
		}
	}

	private void setListAdapter(EndlessNewsFeedAdapter endlessNewsFeedAdapter) {
		if (listView != null) {
			listView.setAdapter(endlessNewsFeedAdapter);
		} else {
			gridView.setAdapter(endlessNewsFeedAdapter);
			endlessNewsFeedAdapter.notifyDataSetChanged();
		}
		setContentShown(true);
	}

	// background thread
	private void makeRequest() {
		try {
			Persistance.ensureAuth();
			final Result result = Rest.vkontakte().getNewsFeed(0, Persistance.getAccessToken());
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					postResult(result);
				}
			});
		} catch (IOException e) {
			// TODO handle the fucking error
		} catch (AuthException e) {
			// TODO handle the fucking error
		}
	}
}
