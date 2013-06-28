package com.stiggpwnz.vibes.util;

import android.util.DisplayMetrics;

import com.stiggpwnz.vibes.Vibes;

public class Converter {

	private static class Holder {
		private static final DisplayMetrics INSTANCE = Vibes.getContext().getResources().getDisplayMetrics();
	}

	public static int dpToPx(float dp) {
		return (int) ((dp * Holder.INSTANCE.density) + 0.5);
	}
}
