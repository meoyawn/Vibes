package com.cuubonandroid.sugaredlistanimations;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

/**
 * 
 * @author <a href="http://www.hugofernandes.pt">Hugo Fernandes</a>
 * 
 */
public abstract class GNowListAdapter extends GenericBaseAdapter {

	public GNowListAdapter(Context context, SpeedScrollListener scrollListener) {
		super(context, scrollListener);
	}

	@Override
	protected void defineInterpolator() {
		interpolator = new DecelerateInterpolator();
	}

	@Override
	protected View getAnimatedView(int position, View convertView, ViewGroup parent) {
		v = getRowView(position, convertView, parent);

		if (v != null && !positionsMapper.get(position) && position > previousPostition) {

			speed = scrollListener.getSpeed();

			animDuration = (((int) speed) == 0) ? ANIM_DEFAULT_SPEED : (long) (1 / speed * 15000);

			if (animDuration > ANIM_DEFAULT_SPEED)
				animDuration = ANIM_DEFAULT_SPEED;

			previousPostition = position;

			ViewHelper.setTranslationY(v, height);
			ViewHelper.setPivotX(v, width / 2);
			ViewHelper.setPivotY(v, height / 2);
			ViewHelper.setAlpha(v, 0.0F);

			if (position % 2 == 0) {
				ViewHelper.setTranslationX(v, -(width / 1.2F));
				ViewHelper.setRotation(v, 50);
			} else {
				ViewHelper.setTranslationX(v, (width / 1.2F));
				ViewHelper.setRotation(v, -50);
			}

			ViewPropertyAnimator localViewPropertyAnimator = ViewPropertyAnimator.animate(v).rotation(0.0F).translationX(0).translationY(0)
					.setDuration(animDuration).alpha(1.0F).setInterpolator(interpolator);

			localViewPropertyAnimator.setStartDelay(500).start();

			positionsMapper.put(position, true);
		}

		return v;
	}

	/**
	 * Get a View that displays the data at the specified position in the data set. You can either create a View manually or inflate it from an XML layout file.
	 * When the View is inflated, the parent View (GridView, ListView...) will apply default layout parameters unless you use
	 * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)} to specify a root view and to prevent attachment to the root.
	 * 
	 * @param position
	 *            The position of the item within the adapter's data set of the item whose view we want.
	 * @param convertView
	 *            The old view to reuse, if possible. Note: You should check that this view is non-null and of an appropriate type before using. If it is not
	 *            possible to convert this view to display the correct data, this method can create a new view.
	 * @param parent
	 *            The parent that this view will eventually be attached to
	 * @return A View corresponding to the data at the specified position.
	 */
	protected abstract View getRowView(int position, View convertView, ViewGroup parent);
}
