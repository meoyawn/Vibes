package com.stiggpwnz.vibes.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.android.ex.widget.StaggeredGridView;
import com.cuubonandroid.sugaredlistanimations.SpeedScrollListener;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.adapters.EndlessNewsFeedAdapter;
import com.stiggpwnz.vibes.adapters.NewsFeedAdapter;
import com.stiggpwnz.vibes.events.RefreshButtonVisibility;
import com.stiggpwnz.vibes.fragments.base.RetainedProgressFragment;
import com.stiggpwnz.vibes.util.BusProvider;
import com.stiggpwnz.vibes.vk.VKontakte;
import com.stiggpwnz.vibes.vk.models.NewsFeed.Result;

import butterknife.InjectView;
import retrofit.client.Response;

public class NewsFeedFragment extends RetainedProgressFragment {

    @InjectView(R.id.list)
    ListView listView;
    @InjectView(R.id.grid)
    StaggeredGridView gridView;

    private SpeedScrollListener scrollListener;
    private Result result;
    private int firstVisiblePosition;

    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.newsfeed);
    }

    @Override
    public void setContentShown(boolean shown) {
        super.setContentShown(shown);
        if (shown && !isContentEmpty()) {
            BusProvider.post(RefreshButtonVisibility.VISIBLE);
        } else {
            BusProvider.post(RefreshButtonVisibility.INVISIBLE);
        }
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
    }

    @Override
    public void onFirstCreated(View view) {
        new Runnable() {

            @Override
            public void run() {
                setContentShown(false);
                VKontakte.get().getNewsFeed(new VKCallback<Result>(this) {

                    @Override
                    public void onSuccess(Result object, Response response) {
                        postResult(object);
                    }
                });
            }
        }.run();
    }

    @Override
    public void onReCreated(View view) {
        if (result != null) {
            postResult(result);
            if (listView != null) {
                listView.setSelection(firstVisiblePosition);
            } else {
                gridView.setFirstPosition(firstVisiblePosition);
            }
        } else {
            setContentEmpty(true);
        }
        setContentShownNoAnimation(true);
    }

    @Override
    public void onDestroyView() {
        firstVisiblePosition = listView != null ? listView.getFirstVisiblePosition() : gridView.getFirstPosition();
        super.onDestroyView();
    }

    protected void postResult(Result result) {
        this.result = result;
        setListAdapter(new EndlessNewsFeedAdapter(getSherlockActivity(), new NewsFeedAdapter(getSherlockActivity(), result.response, scrollListener),
                R.layout.loading_footer));
    }

    private void setListAdapter(ListAdapter endlessNewsFeedAdapter) {
        if (listView != null) {
            listView.setAdapter(endlessNewsFeedAdapter);
        } else {
            gridView.setAdapter(endlessNewsFeedAdapter);
        }
    }

    @Override
    protected void onRetryClick() {
        onFirstCreated(getContentView());
    }
}
