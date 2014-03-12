package com.stiggpwnz.vibes.widget;

import android.text.SpannableString;

import com.stiggpwnz.vibes.text.VKLinkSpan;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by adel on 08/03/14
 */
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class PostViewTest {
    @Test public void testOnFinishInflate() throws Exception {

    }

    @Test public void testShowUser() throws Exception {

    }

    @Test public void testLinkify() throws Exception {
        SpannableString linkify = PostView.linkify(Robolectric.application, "[club48302822|Oomkah Dee] – житель Санкт-Петербурга, коллекционер, ценитель и исследователь музыки, известный своей уникальной серией миксов Collage. В миксе для Full of Nothing собраны “дорогие сердцу современные композиторы, имена которых, к сожалению, довольно редко встречаются в программках”.<br>Скачать/трэклист: http://fullofnothing.net/mix-by-oomkah-dee/");
        Assert.assertEquals(1, linkify.getSpans(0, linkify.length(), VKLinkSpan.class).length);
    }

    @Test public void testSetPost() throws Exception {

    }
}
