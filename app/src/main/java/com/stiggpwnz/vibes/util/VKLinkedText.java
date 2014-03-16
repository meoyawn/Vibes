package com.stiggpwnz.vibes.util;

import android.content.Context;
import android.content.ContextWrapper;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.stiggpwnz.vibes.text.HashTagSpan;
import com.stiggpwnz.vibes.text.ReplaceTextSpan;
import com.stiggpwnz.vibes.text.VKLinkSpan;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by adel on 15/03/14
 */
public class VKLinkedText extends JsonDeserializer<Spannable> {
    static final Pattern HASH_TAG = Pattern.compile("#[a-zA-Z][\\w@-]*");
    static final Pattern VK_UNIT  = Pattern.compile("\\[([^\\[\\|]+?)\\|([^\\]]+?)\\]");

    static Spannable linkify(Context context, Spannable spannable) {
        Matcher hasTags = HASH_TAG.matcher(spannable);
        while (hasTags.find()) {
            int start = hasTags.start();
            int end = hasTags.end();
            spannable.setSpan(new HashTagSpan(hasTags.group()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        Matcher units = VK_UNIT.matcher(spannable);
        while (units.find()) {
            int start = units.start();
            int end = units.end();
            spannable.setSpan(new VKLinkSpan(units.group(1)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new ReplaceTextSpan(context, units.group(2)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannable;
    }

    @Override
    public Spannable deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        Spannable spannable = (Spannable) Html.fromHtml(jp.getValueAsString());
        return linkify(new ContextWrapper(null), spannable);
    }
}
