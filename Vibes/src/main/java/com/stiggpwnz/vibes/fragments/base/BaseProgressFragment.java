package com.stiggpwnz.vibes.fragments.base;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.devspark.progressfragment.ProgressFragment;
import com.roadtrippers.R;
import com.roadtrippers.RoadTrippersApp;
import com.squareup.otto.Bus;

import java.io.File;
import java.io.FileInputStream;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Views;
import dagger.Lazy;
import icepick.annotation.Icicle;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

import static icepick.bundle.Bundles.restoreInstanceState;
import static icepick.bundle.Bundles.saveInstanceState;

public abstract class BaseProgressFragment extends ProgressFragment {

    @Inject Lazy<Bus> busLazy;

    @InjectView(R.id.textErrorMessage) TextView errorMessage;

    @Icicle boolean contentIsShown;

    AnimationDrawable progressDrawable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoadTrippersApp.from(getActivity()).inject(this);
        restoreInstanceState(this, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView byId = Views.findById(view, R.id.imageProgress);
        progressDrawable = (AnimationDrawable) byId.getDrawable();
    }

    @Override
    public void setContentShown(boolean shown) {
        contentIsShown = shown;
        super.setContentShown(shown);
        setAnimationShown(shown);
    }

    @Override
    public void setContentShownNoAnimation(boolean shown) {
        contentIsShown = shown;
        super.setContentShownNoAnimation(shown);
        setAnimationShown(shown);
    }

    private void setAnimationShown(boolean shown) {
        if (shown) {
            progressDrawable.stop();
        } else if (!progressDrawable.isRunning()) {
            progressDrawable.start();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onCreateView(savedInstanceState);
        Views.inject(this, getView());
        if (savedInstanceState != null) {
            setContentEmpty(savedInstanceState.getBoolean("empty", false));
            setContentShownNoAnimation(savedInstanceState.getBoolean("shown", true));
        }
        onViewCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveInstanceState(this, outState);
    }

    protected boolean isInPortrait() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    protected void setErrorMessage(CharSequence errorMessage) {
        this.errorMessage.setText(errorMessage);
    }

    protected void setErrorMessage(int resource) {
        this.errorMessage.setText(resource);
    }

    protected abstract void onCreateView(Bundle savedInstanceState);

    protected abstract void onViewCreated(Bundle savedInstanceState);

    protected abstract void onRetryButtonClick();

    @OnClick(R.id.buttonRetry)
    public void onRetryClick() {
        onRetryButtonClick();
    }

    @Override
    public void onResume() {
        super.onResume();
        busLazy.get().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        busLazy.get().unregister(this);
    }

    @Override
    public void onDestroyView() {
        Views.reset(this);
        super.onDestroyView();
    }

    @Override
    public ViewGroup getContentView() {
        return (ViewGroup) super.getContentView();
    }

    public static void setColorFilter(View view, int color) {
        StateListDrawable drawableWithColorFilter = drawableWithColorFilter(view.getContext(), (BitmapDrawable) view.getBackground(), color);
        view.setBackgroundDrawable(drawableWithColorFilter);
    }

    public static void setColorFilter(ImageView imageView, int color) {
        StateListDrawable drawableWithColorFilter = drawableWithColorFilter(imageView.getContext(), (BitmapDrawable) imageView.getDrawable(), color);
        imageView.setImageDrawable(drawableWithColorFilter);
    }

    private static StateListDrawable drawableWithColorFilter(Context context, BitmapDrawable drawable, int color) {
        Bitmap original = drawable.getBitmap();
        Bitmap copy = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);

        Paint paint = new Paint();
        paint.setColorFilter(new LightingColorFilter(color, 1));
        new Canvas(copy).drawBitmap(original, 0, 0, paint);

        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, new BitmapDrawable(context.getResources(), copy));
        states.addState(new int[]{}, drawable);
        return states;
    }

    public static Observable<String> base64Observable(final File avatar) {
        return Observable.create(new Observable.OnSubscribeFunc<String>() {

            @Override
            public Subscription onSubscribe(Observer<? super String> observer) {
                try {
                    byte[] result = new byte[(int) avatar.length()];

                    FileInputStream fileInputStream = new FileInputStream(avatar);
                    fileInputStream.read(result);
                    fileInputStream.close();

                    Base64.encodeToString(result, Base64.CRLF);

                    observer.onNext(Base64.encodeToString(result, Base64.CRLF));
                } catch (Exception e) {
                    observer.onError(e);
                }
                return Subscriptions.empty();
            }
        });
    }
}
