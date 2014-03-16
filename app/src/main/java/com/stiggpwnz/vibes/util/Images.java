package com.stiggpwnz.vibes.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;

import com.stiggpwnz.vibes.vk.VKAuth;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by adel on 16/03/14
 */
public class Images {
    @NotNull public static Bitmap blur(@NotNull Context context, @NotNull Bitmap inputBitmap) {
        VKAuth.assertBgThread();

        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap.getHeight(), inputBitmap.getWidth(),
                Bitmap.Config.ARGB_8888);

        RenderScript renderScript = RenderScript.create(context);
        ScriptIntrinsicBlur intrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));

        Allocation tmpIn = Allocation.createFromBitmap(renderScript, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap);
        intrinsicBlur.setRadius(25.f);
        intrinsicBlur.setInput(tmpIn);
        intrinsicBlur.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);
        return outputBitmap;
    }

    @NotNull
    public static TransitionDrawable transition(@Nullable Drawable drawable, @NotNull Bitmap bitmap, @NotNull Resources resources) {
        VKAuth.assertBgThread();

        Drawable from = drawable instanceof TransitionDrawable ?
                ((TransitionDrawable) drawable).getDrawable(1) :
                new ColorDrawable(Color.TRANSPARENT);
        Drawable to = new BitmapDrawable(resources, bitmap);
//        to.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);

        return new TransitionDrawable(new Drawable[]{from, to});
    }
}
