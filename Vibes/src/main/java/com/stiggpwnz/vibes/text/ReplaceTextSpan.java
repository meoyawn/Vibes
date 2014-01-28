package com.stiggpwnz.vibes.text;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;

/**
 * Created by adel on 1/28/14
 */
public class ReplaceTextSpan extends ReplacementSpan {

    Context context;
    String  replacement;

    public ReplaceTextSpan(Context context, String replacement) {
        this.context = context;
        this.replacement = replacement;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return Math.round(paint.measureText(replacement));
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        // TODO get from context
        paint.setColor(Color.parseColor("#33b5e5"));
        paint.setUnderlineText(true);
        canvas.drawText(replacement, x, y, paint);
    }
}
