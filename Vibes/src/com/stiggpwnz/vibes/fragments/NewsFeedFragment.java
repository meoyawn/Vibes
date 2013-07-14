package com.stiggpwnz.vibes.fragments;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import butterknife.InjectView;

import com.android.ex.widget.StaggeredGridView;
import com.cuubonandroid.sugaredlistanimations.SpeedScrollListener;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.adapters.EndlessNewsFeedAdapter;
import com.stiggpwnz.vibes.adapters.NewsFeedAdapter;
import com.stiggpwnz.vibes.fragments.base.RetainedProgressFragment;
import com.stiggpwnz.vibes.util.Persistance;
import com.stiggpwnz.vibes.vk.VKontakte;
import com.stiggpwnz.vibes.vk.models.NewsFeed;
import com.stiggpwnz.vibes.vk.models.NewsFeed.Result;

public class NewsFeedFragment extends RetainedProgressFragment {

	@InjectView(R.id.list) ListView listView;
	@InjectView(R.id.grid) StaggeredGridView gridView;

	private SpeedScrollListener scrollListener;
	private NewsFeed.Result result;

	@Override
	protected void onCreateView(Bundle savedInstanceState) {
		setContentView(R.layout.newsfeed);
	}

	@Override
	protected void onViewCreated(Bundle savedInstanceState) {
		scrollListener = new SpeedScrollListener();

		if (listView != null) {
			listView.setOnScrollListener(scrollListener);
		}

		if (gridView != null) {
			gridView.setItemMargin((int) getResources().getDimension(R.dimen.padding_list));
			gridView.setColumnCount(getResources().getInteger(R.integer.num_columns));
		}

		if (result != null) {
			postResult(result);
		}
	}

	@Override
	public void onFirstCreated(View view) {
		makeRequest();
	}

	private void makeRequest() {
		VKontakte.get().getNewsFeed(new Callback<Result>() {

			@Override
			public void success(Result result, Response arg1) {
				if (result.isResponse()) {
					postResult(result);
				} else {
					if (result.error.isAuthError()) {
						Persistance.resetAuth();
						makeRequest();
					} else {
						onError();
					}
				}
			}

			@Override
			public void failure(RetrofitError arg0) {
				onError();
			}
		});
	}

	protected void postResult(NewsFeed.Result result) {
		this.result = result;
		setListAdapter(new EndlessNewsFeedAdapter(getSherlockActivity(), new NewsFeedAdapter(getSherlockActivity(), result.response, scrollListener),
				R.layout.loading_footer));
	}

	private void setListAdapter(EndlessNewsFeedAdapter endlessNewsFeedAdapter) {
		if (listView != null) {
			listView.setAdapter(endlessNewsFeedAdapter);
		} else {
			gridView.setAdapter(endlessNewsFeedAdapter);
		}
		setContentShown(true);
	}

	@Override
	protected void onRetryClick() {
		setContentEmpty(false);
		setContentShown(false);
		makeRequest();
	}

}
