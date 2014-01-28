package com.stiggpwnz.vibes.text;

import android.text.style.ClickableSpan;
import android.view.View;

/**
* Created by adel on 1/28/14
*/
public class HashTagSpan extends ClickableSpan {

    String hashTag;

    public HashTagSpan(String hashTag) {
        this.hashTag = hashTag;
    }

    @Override
    public void onClick(View widget) {

    }
}
