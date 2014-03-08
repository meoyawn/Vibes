package com.stiggpwnz.vibes.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.etsy.android.grid.util.DynamicHeightImageView;
import com.squareup.picasso.Picasso;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.dagger.Dagger;
import com.stiggpwnz.vibes.vk.models.Photo;

import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

public class PhotoView extends DynamicHeightImageView {
    @Inject   Picasso picasso;
    @Nullable Photo   photo;

    public PhotoView(Context context) { super(context); }

    public PhotoView(Context context, AttributeSet attrs) { super(context, attrs); }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();
        if (!isInEditMode()) {
            Dagger.inject(this);
        }
    }

    public void setPhoto(@Nullable Photo photo) {
        this.photo = photo;
        if (photo != null) {
            setHeightRatio(photo.getRatio());
            picasso.load(photo.getUrl(getWidth()))
                    .placeholder(R.drawable.placeholder)
                    .into(this);
            setVisibility(View.VISIBLE);
        } else {
            setVisibility(View.GONE);
        }
    }
}
