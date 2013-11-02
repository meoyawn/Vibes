package com.stiggpwnz.vibes.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.vk.models.Photo;

public class PhotoView extends ImageView {

    private static final DisplayImageOptions OPTIONS = new DisplayImageOptions.Builder().showStubImage(R.drawable.placeholder)
            .showImageForEmptyUri(R.drawable.placeholder).showImageOnFail(R.drawable.placeholder).cacheInMemory().cacheOnDisc().build();

    public Photo photo;

    public PhotoView(Context context) {
        super(context);
    }

    public PhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void setHeight(int w) {
        getLayoutParams().height = (int) (photo.getRatio() * w);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (oldw == 0 && w > 0) {
            setHeight(w);
        }
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;

        if (photo != null) {
            setVisibility(View.VISIBLE);

            int w = getWidth();
            if (w > 0) {
                setHeight(w);
            }

            ImageLoader.getInstance().displayImage(photo.getUrl(w), this, OPTIONS);
        } else {
            setVisibility(View.GONE);
        }
    }
}
