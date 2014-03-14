package com.stiggpwnz.vibes.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neenbedankt.bundles.annotation.Argument;
import com.squareup.okhttp.OkHttpClient;
import com.stiggpwnz.vibes.MainActivity;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.adapters.FeedAdapter;
import com.stiggpwnz.vibes.qualifiers.IOThreadPool;
import com.stiggpwnz.vibes.vk.VKApi;
import com.stiggpwnz.vibes.vk.VKAuth;
import com.stiggpwnz.vibes.vk.VKontakte;
import com.stiggpwnz.vibes.vk.models.Feed;
import com.stiggpwnz.vibes.vk.models.Unit;
import com.stiggpwnz.vibes.widget.PostView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;

import butterknife.InjectView;
import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import mortar.Blueprint;
import mortar.Mortar;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.Converter;
import retrofit.converter.JacksonConverter;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.subjects.PublishSubject;
import timber.log.Timber;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

public class FeedFragment extends BaseFragment {
    @Inject               Lazy<VKontakte>      vkontakte;
    @Inject @IOThreadPool Scheduler            ioThreadPool;
    @Inject               PublishSubject<Unit> unitClicks;

    @Argument int ownerId;

    @InjectView(R.id.ptr_layout) @NotNull PullToRefreshLayout pullToRefreshLayout;
    @InjectView(R.id.grid) @NotNull       AbsListView         staggeredGridView;

    @Nullable Feed         lastResult;
    @Nullable Subscription subscription;
    @NotNull  Subscription unitClickSubscription;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FeedFragmentBuilder.injectArguments(this);
        Mortar.inject(getMortarContext(), this);
    }

    @Override protected Blueprint getBluePrint() {
        return new Blueprint() {
            @Override public String getMortarScopeName() { return FeedFragment.class.getName(); }

            @Override public Object getDaggerModule() { return new DaggerModule(); }
        };
    }

    @Module(addsTo = MainActivity.DaggerModule.class,
            injects = {
                    FeedFragment.class,
                    PostView.class
            })
    static class DaggerModule {
        @Provides @Singleton Converter provideJacksonConverter(ObjectMapper objectMapper) {
            return new JacksonConverter(objectMapper);
        }

        @Provides @Singleton
        VKApi provideVkApi(OkHttpClient okHttpClient, Converter converter, VKAuth vkAuth) {
            Timber.d("creating vkapi");
            return new RestAdapter.Builder()
                    .setEndpoint(VKApi.SERVER)
                    .setClient(new OkClient(okHttpClient))
                    .setConverter(converter)
                    .setRequestInterceptor(vkAuth)
                    .setLog(Timber::d)
                    .setLogLevel(RestAdapter.LogLevel.BASIC)
                    .build()
                    .create(VKApi.class);
        }

        @Provides @Singleton VKontakte provideVKontakte(VKApi vkApi, VKAuth vkAuth) {
            return new VKontakte(vkApi, vkAuth);
        }

        @Provides @Singleton PublishSubject<Unit> provideUnitClicks() {
            return PublishSubject.create();
        }
    }

    @Override protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.newsfeed, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ActionBarPullToRefresh.from(getActivity())
                .listener(view1 -> makeRequest())
                .setup(pullToRefreshLayout);

        if (subscription == null) {
            if (lastResult == null && savedInstanceState != null && savedInstanceState.containsKey("feed")) {
                lastResult = (Feed) savedInstanceState.get("feed");
            }
            if (lastResult == null) {
                makeRequest();
            } else {
                updateView();
            }
        } else {
            pullToRefreshLayout.setRefreshing(true);
        }

        unitClickSubscription = unitClicks.subscribe(unit -> {
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, FeedFragmentBuilder.newFeedFragment(unit.getId()))
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("feed", lastResult);
    }

    @Override public void onDestroyView() {
        unitClickSubscription.unsubscribe();
        super.onDestroyView();
    }

    void updateView() {
        if (getView() != null && lastResult != null && getActivity() != null) {
            pullToRefreshLayout.setRefreshComplete();
            staggeredGridView.setAdapter(new FeedAdapter(getMortarInflater(), lastResult.getItems()));
        }
    }

    void makeRequest() {
        pullToRefreshLayout.setRefreshing(true);
        Observable<Feed> feedObservable = ownerId == 0 ?
                vkontakte.get().getNewsFeed(0) :
                vkontakte.get().getWall(ownerId, null, 0);
        subscription = AndroidObservable.fromFragment(this, feedObservable.subscribeOn(ioThreadPool))
                .doOnEach(notification -> subscription = null)
                .subscribe(feed -> {
                    lastResult = feed;
                    updateView();
                }, e -> {
                    Timber.e(e, "error getting feed");
                    pullToRefreshLayout.setRefreshComplete();
                });
    }

    @Override public void onDestroy() {
        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
        super.onDestroy();
    }
}
