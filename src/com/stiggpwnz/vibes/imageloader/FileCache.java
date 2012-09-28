package com.stiggpwnz.vibes.imageloader;

import java.io.File;

import android.content.Context;

public class FileCache {

	private File cacheDir;

	public FileCache(Context context) {
		cacheDir = context.getCacheDir();
		if (!cacheDir.exists())
			cacheDir.mkdirs();
	}

	public File getFile(String url) {
		String filename = url.substring(url.lastIndexOf('/') + 1, url.length());
		return new File(cacheDir, filename);
	}

	public void clear() {
		File[] files = cacheDir.listFiles();
		if (files == null)
			return;
		for (File f : files)
			f.delete();
	}

	public boolean isEmpty() {
		return !(cacheDir.listFiles().length > 0);
	}

}