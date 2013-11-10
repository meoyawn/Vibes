package com.stiggpwnz.vibes.adapters;

import android.text.SpannableString;
import android.util.Patterns;

import com.stiggpwnz.vibes.test.RobolectricGradleTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.regex.Matcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by adel on 11/8/13
 */
@RunWith(RobolectricGradleTestRunner.class)
public class PostsViewHolderTest {

    @Test
    public void testUrlRegex() {
        Matcher matcher = Patterns.WEB_URL.matcher("here I am at vk.com lol");
        assertTrue(matcher.find());
        assertEquals("vk.com", matcher.group());
    }

    @Test
    public void testHyperLinks() throws Exception {
        SpannableString spannableString = PostViewHolder.linkify("vk.com http://vk.com fuck me lol haha");
        assertEquals(2, spannableString.getSpans(0, spannableString.length(), Object.class).length);
    }
}
