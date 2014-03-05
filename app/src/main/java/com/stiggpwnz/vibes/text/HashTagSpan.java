package com.stiggpwnz.vibes.text;

import android.text.style.ClickableSpan;
import android.view.View;

import lombok.AllArgsConstructor;

/**
 * Created by adel on 1/28/14
 */
@AllArgsConstructor(suppressConstructorProperties = true)
public class HashTagSpan extends ClickableSpan {
    String hashTag;

    @Override public void onClick(View widget) { }
}
