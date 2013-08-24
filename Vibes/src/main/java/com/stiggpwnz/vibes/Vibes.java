package com.stiggpwnz.vibes;

import android.app.Application;
import android.content.Context;
import android.webkit.CookieSyncManager;

import com.nostra13.universalimageloader.cache.disc.impl.TotalSizeLimitedDiscCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.stiggpwnz.vibes.util.Utils;

public class Vibes extends Application {

	private static final int CACHE_SIZE = 20 * 1024 * 1024;

	private static Context staticContext;

	@Override
	public void onCreate() {
		staticContext = this;
		super.onCreate();

		ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(this).tasksProcessingOrder(QueueProcessingType.LIFO)
				.discCache(new TotalSizeLimitedDiscCache(Utils.getCacheDir("image"), CACHE_SIZE)).threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory().build();
		ImageLoader.getInstance().init(configuration);

		CookieSyncManager.createInstance(this);
	}

	public static Context getContext() {
		return staticContext;
	}
}
