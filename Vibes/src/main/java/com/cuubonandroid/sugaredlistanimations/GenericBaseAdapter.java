package com.cuubonandroid.sugaredlistanimations;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.BaseAdapter;

/**
 * 
 * @author <a href="http://www.hugofernandes.pt">Hugo Fernandes</a>
 * 
 */
public abstract class GenericBaseAdapter extends BaseAdapter {

	protected static final long ANIM_DEFAULT_SPEED = 1000L;

	protected Interpolator interpolator;

	protected SparseBooleanArray positionsMapper;
	protected int height, width, previousPostition;
	protected SpeedScrollListener scrollListener;
	protected double speed;
	protected long animDuration;
	protected View v;
	protected Context context;

	@SuppressWarnings("deprecation")
	protected GenericBaseAdapter(Context context, SpeedScrollListener scrollListener) {
		this.context = context;
		this.scrollListener = scrollListener;

		previousPostition = -1;
		positionsMapper = new SparseBooleanArray();
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		width = windowManager.getDefaultDisplay().getWidth();
		height = windowManager.getDefaultDisplay().getHeight();

		defineInterpolator();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View animatedView = getAnimatedView(position, convertView, parent);
		return animatedView;
	}

	protected abstract View getAnimatedView(int position, View convertView, ViewGroup parent);

	protected abstract void defineInterpolator();
}
