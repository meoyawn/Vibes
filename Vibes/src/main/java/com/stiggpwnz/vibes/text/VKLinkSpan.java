package com.stiggpwnz.vibes.text;

import android.text.style.ClickableSpan;
import android.view.View;

import timber.log.Timber;

/**
* Created by adel on 1/28/14
*/
public class VKLinkSpan extends ClickableSpan {

    String path;

    public VKLinkSpan(String group) {
        path = group;
    }

    @Override
    public void onClick(View widget) {
        Timber.d("clicked on %s group", path);
    }
}
