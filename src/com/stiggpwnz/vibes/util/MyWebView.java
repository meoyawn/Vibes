package com.stiggpwnz.vibes.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class MyWebView extends WebView {

	public MyWebView(Context context) {
		super(context);
	}

	public MyWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@SuppressLint("NewApi")
	public MyWebView(Context context, AttributeSet attrs, int defStyle, boolean privateBrowsing) {
		super(context, attrs, defStyle, privateBrowsing);
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		try {
			super.onWindowFocusChanged(hasWindowFocus);
		} catch (NullPointerException e) {
			// Catch null pointer exception
		}
	}
}