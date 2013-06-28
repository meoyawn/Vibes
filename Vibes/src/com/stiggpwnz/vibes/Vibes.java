package com.stiggpwnz.vibes;

import java.io.File;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.nostra13.universalimageloader.cache.disc.impl.TotalSizeLimitedDiscCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class Vibes extends Application {

	private static final int CACHE_SIZE = 20 * 1024 * 1024;

	private static Context staticContext;

	@Override
	public void onCreate() {
		staticContext = this;
		super.onCreate();

		ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(this).tasksProcessingOrder(QueueProcessingType.LIFO)
				.discCache(new TotalSizeLimitedDiscCache(getCacheDir("image"), CACHE_SIZE)).threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory().build();
		ImageLoader.getInstance().init(configuration);
	}

	public static File getCacheDir(String name) {
		File cacheDir = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ? staticContext.getExternalCacheDir() : staticContext
				.getCacheDir();
		File target = new File(cacheDir, name);
		if (!target.exists()) {
			target.mkdirs();
		}
		return target;
	}

	public static Context getContext() {
		return staticContext;
	}
}
