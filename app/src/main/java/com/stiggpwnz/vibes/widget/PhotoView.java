package com.stiggpwnz.vibes.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;

import com.etsy.android.grid.util.DynamicHeightImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;
import com.stiggpwnz.vibes.Dagger;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.vk.models.Photo;

import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

import rx.subjects.PublishSubject;
import timber.log.Timber;

public class PhotoView extends DynamicHeightImageView {
    @Inject   Picasso                picasso;
    @Inject   LruCache               lruCache;
    @Inject   PublishSubject<Bitmap> loadedBitmaps;
    @Nullable Photo                  photo;

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
            String url = photo.getUrl(getWidth());
            picasso.load(url)
                    .placeholder(R.drawable.placeholder)
                    .into(this, new Success() {
                        @Override public void onSuccess() {
                            int[] location = new int[2];
                            getLocationInWindow(location);
                            Timber.d("location: %d:%d", location[0], location[1]);
                            Bitmap v = lruCache.get(url + '\n');
                            loadedBitmaps.onNext(v);
                        }
                    });
            setVisibility(View.VISIBLE);
        } else {
            setVisibility(View.GONE);
        }
    }

    static abstract class Success implements Callback {
        @Override public void onError() {}
    }
}
