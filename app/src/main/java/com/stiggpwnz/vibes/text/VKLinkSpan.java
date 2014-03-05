package com.stiggpwnz.vibes.text;

import android.text.style.ClickableSpan;
import android.view.View;

import lombok.AllArgsConstructor;
import timber.log.Timber;

/**
 * Created by adel on 1/28/14
 */
@AllArgsConstructor(suppressConstructorProperties = true)
public class VKLinkSpan extends ClickableSpan {
    String path;

    @Override
    public void onClick(View widget) {
        Timber.d("clicked on %s group", path);
    }
}
