package com.stiggpwnz.vibes;

import static com.stiggpwnz.vibes.Singletons.CACHE_SIZE;

import java.io.File;
import java.io.IOException;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.nostra13.universalimageloader.cache.disc.impl.TotalSizeLimitedDiscCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class VibesApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Persistance.init(this);
		try {
			Singletons.init(this);
		} catch (IOException e) {
			throw new AssertionError(e);
		}

		File cacheDir = getCacheDir(this, "image");
		ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(this).tasksProcessingOrder(QueueProcessingType.LIFO)
				.discCache(new TotalSizeLimitedDiscCache(cacheDir, CACHE_SIZE)).threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory()
				.build();
		ImageLoader.getInstance().init(configuration);
	}

	public static File getCacheDir(Context context, String name) {
		File cacheDir = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ? context.getExternalCacheDir() : context.getCacheDir();
		File target = new File(cacheDir, name);
		if (!target.exists()) {
			target.mkdirs();
		}
		return target;
	}
}
